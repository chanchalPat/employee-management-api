package com.chanchal.employeemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(

        @Schema(
                description = "Username of the registered user",
                example = "rahul"
        )
        String username,

        @Schema(
                description = "Password of the registered user",
                example = "YourPassword123"
        )
        String password

) {
}
