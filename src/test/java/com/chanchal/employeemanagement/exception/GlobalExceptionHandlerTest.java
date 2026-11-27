package com.chanchal.employeemanagement.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn500WhenUnexpectedExceptionOccurs()
            throws Exception {

        mockMvc.perform(
                        get("/test/internal-server-error")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(
                        "An unexpected server error occurred"
                ));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/internal-server-error")
        public String generateError() {

            throw new RuntimeException(
                    "Sensitive internal exception details"
            );
        }

        @GetMapping("/test/gateway-timeout")
        public String generateGatewayTimeout() {

            throw new GatewayTimeoutException(
                    "External employee verification service timed out"
            );
        }
    }

    @Test
    void handleServiceUnavailableShouldReturn503AndRetryAfterHeader() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ServiceUnavailableException exception =
                new ServiceUnavailableException(
                        "Service is temporarily unavailable. Please try again later.",
                        120
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleServiceUnavailable(exception);

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertEquals(
                "120",
                response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)
        );

        assertNotNull(response.getBody());

        assertEquals(
                503,
                response.getBody().getStatus()
        );

        assertEquals(
                "Service is temporarily unavailable. Please try again later.",
                response.getBody().getMessage()
        );
    }


    @Test
    void handleBadGatewayShouldReturn502() {

        GlobalExceptionHandler handler =
                new GlobalExceptionHandler();

        BadGatewayException exception =
                new BadGatewayException(
                        "Upstream server returned an invalid response"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleBadGateway(exception);

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                502,
                response.getBody().getStatus()
        );

        assertEquals(
                "Invalid response received from upstream server",
                response.getBody().getMessage()
        );

        assertNull(response.getBody().getErrors());
    }

    @Test
    void shouldReturn504WhenUpstreamServiceTimesOut()
            throws Exception {

        mockMvc.perform(
                        get("/test/gateway-timeout")
                )
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.message").value(
                        "Upstream service did not respond within the allowed time"
                ));
    }
}
