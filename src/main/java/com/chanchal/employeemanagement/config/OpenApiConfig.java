package com.chanchal.employeemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Management API",
                description = "REST API for employee management, authentication and authorization",
                version = "1.0.0"
        )
)
public class OpenApiConfig {

}

