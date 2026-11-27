package com.chanchal.employeemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record EmployeeRequest(

        @Schema(
                description = "Employee's full name",
                example = "John Smith",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Name is required")
        String name,

        @Schema(
                description = "Employee's department",
                example = "Quality Engineering",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Department is required")
        String department,

        @Schema(
                description = "Employee's email address",
                example = "john.smith@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "email"
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(
                description = "Employee's salary",
                example = "45000",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0"
        )
        @NotNull(message = "Salary is required")
        @PositiveOrZero(message = "Salary cannot be negative")
        Long salary

) {
}
