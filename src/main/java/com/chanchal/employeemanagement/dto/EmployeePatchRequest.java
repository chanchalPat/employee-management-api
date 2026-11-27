package com.chanchal.employeemanagement.dto;

import com.chanchal.employeemanagement.employee.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;


public record EmployeePatchRequest(

        @Schema(
                description = "Employee's full name. Omit this property to leave the existing name unchanged.",
                type = "string",
                example = "John Smith",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = false
        )
        JsonNode name,

        @Schema(
                description = "Employee's department. Omit this property to leave the existing department unchanged.",
                type = "string",
                example = "Engineering",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = false
        )
        JsonNode department,

        @Schema(
                description = "Employee's email address. Omit this property to leave the existing email unchanged.",
                type = "string",
                format = "email",
                example = "john.smith@example.com",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = false
        )
        JsonNode email,

        @Schema(
                description = "Employee's salary. Must be a non-negative integer. Omit this property to leave the existing salary unchanged.",
                type = "integer",
                format = "int64",
                example = "45000",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = false
        )
        JsonNode salary

) {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "name",
            "department",
            "email",
            "salary"
    );

    public static EmployeePatchRequest from(ObjectNode body) {

        var fields = body.fieldNames();

        while (fields.hasNext()) {

            String field = fields.next();

            if (!ALLOWED_FIELDS.contains(field)) {
                throw new IllegalArgumentException(
                        "Unsupported field: " + field
                );
            }
        }

        return new EmployeePatchRequest(
                body.get("name"),
                body.get("department"),
                body.get("email"),
                body.get("salary")
        );
    }

    public void validate() {

        if (name == null &&
                department == null &&
                email == null &&
                salary == null) {

            throw new IllegalArgumentException(
                    "At least one field must be provided for update"
            );
        }

        validateField("name", name);
        validateField("department", department);
        validateField("email", email);
        validateField("salary", salary);
    }

    private void validateField(String field, JsonNode value) {

        // Field was omitted from the request.
        if (value == null) {
            return;
        }

        // Explicit JSON null is not allowed.
        if (value.isNull()) {
            throw new IllegalArgumentException(
                    field + " cannot be null"
            );
        }

        switch (field) {

            case "name", "department" -> {

                if (!value.isTextual() ||
                        value.asText().isBlank()) {

                    throw new IllegalArgumentException(
                            field + " must not be blank"
                    );
                }
            }

            case "email" -> {

                if (!value.isTextual() ||
                        !value.asText().matches(
                                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

                    throw new IllegalArgumentException(
                            "Email must be valid"
                    );
                }
            }

            case "salary" -> {

                if (!value.isIntegralNumber() ||
                        !value.canConvertToLong() ||
                        value.longValue() < 0) {

                    throw new IllegalArgumentException(
                            "Salary must be a non-negative integer"
                    );
                }
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported field: " + field
            );
        }
    }

    public void applyTo(Employee employee) {

        if (name != null) {
            employee.setName(name.asText());
        }

        if (department != null) {
            employee.setDepartment(department.asText());
        }

        if (email != null) {
            employee.setEmail(email.asText());
        }

        if (salary != null) {
            employee.setSalary(salary.longValue());
        }
    }
}
