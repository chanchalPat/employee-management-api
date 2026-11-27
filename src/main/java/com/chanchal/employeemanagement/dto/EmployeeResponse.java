package com.chanchal.employeemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmployeeResponse(

        @Schema(
                description = "Unique identifier of the employee",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long id,

        @Schema(
                description = "Employee's full name",
                example = "John Smith",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Schema(
                description = "Department to which the employee belongs",
                example = "Quality Engineering",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String department,

        @Schema(
                description = "Employee's email address",
                example = "john.smith@example.com",
                format = "email",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "Employee's salary",
                example = "45000",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long salary

) {
}
