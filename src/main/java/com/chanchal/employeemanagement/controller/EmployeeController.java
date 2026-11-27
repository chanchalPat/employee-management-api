package com.chanchal.employeemanagement.controller;

import com.chanchal.employeemanagement.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.chanchal.employeemanagement.dto.EmployeeRequest;
import com.chanchal.employeemanagement.dto.EmployeeResponse;
import com.chanchal.employeemanagement.employee.Employee;
import com.chanchal.employeemanagement.mapper.EmployeeMapper;
import com.chanchal.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.chanchal.employeemanagement.dto.EmployeePatchRequest;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.chanchal.employeemanagement.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.headers.Header;

import java.net.URI;

import java.util.List;

@RestController
@RequestMapping("/employees")
@Tag(
        name = "Employee Management",
        description = "APIs for creating, retrieving, updating and deleting employee records"
)
public class EmployeeController {
//    @GetMapping("/hello")
//    public String hello() {
//        return "Hello from Employee Management API";
//    }

//    @GetMapping("/employee")
//    public Employee getEmployee() {
//
//        return new Employee(
//                1L,
//                "Chanchal",
//                "Engineering",
//                "employee1@example.com"
//        );
//    }

//    @GetMapping("/welcome")
//    public String welcome() {
//        return "Welcome to Employee Management API";
//    }

//    @GetMapping("/manager")
//    public Employee getManager() {
//
//        return new Employee(
//                2L,
//                "John",
//                "Engineering",
//                "employee2@example.com"
//        );
//    }

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    public EmployeeController(EmployeeService employeeService, EmployeeMapper employeeMapper){

        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;

    }

//    @GetMapping
//    public List<EmployeeResponse> getEmployees() {
//        return employeeService.getEmployees()
//                .stream()
//                .map(employeeMapper::toResponse)
//                .toList();
//    }

//    @GetMapping
//    public PageResponse<EmployeeResponse> getEmployees(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//
//        Page<Employee> employeePage =
//                employeeService.getEmployees(page, size);
//
//        List<EmployeeResponse> employees =
//                employeePage.getContent()
//                        .stream()
//                        .map(employeeMapper::toResponse)
//                        .toList();
//
//        return new PageResponse<>(
//                employees,
//                employeePage.getNumber(),
//                employeePage.getSize(),
//                employeePage.getTotalElements(),
//                employeePage.getTotalPages(),
//                employeePage.isFirst(),
//                employeePage.isLast()
//        );
//    }

//    @GetMapping
//    public PageResponse<EmployeeResponse> getEmployees(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) List<String> sort
//    ) {
//
//        Page<Employee> employeePage;
//
//        if (sort == null || sort.isEmpty()) {
//            employeePage = employeeService.getEmployees(page, size);
//        } else {
//            employeePage = employeeService.getEmployees(
//                    page,
//                    size,
//                    sort
//            );
//        }
//
//        List<EmployeeResponse> employees =
//                employeePage.getContent()
//                        .stream()
//                        .map(employeeMapper::toResponse)
//                        .toList();
//
//        return new PageResponse<>(
//                employees,
//                employeePage.getNumber(),
//                employeePage.getSize(),
//                employeePage.getTotalElements(),
//                employeePage.getTotalPages(),
//                employeePage.isFirst(),
//                employeePage.isLast()
//        );
//    }

    @Operation(
            summary = "Get all employees",
            description = "Retrieves employees with pagination, sorting and filtering options"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employees retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = PageResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination, filtering, sorting or request parameter value",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @Parameter(
            name = "sort",
            description = "Sorting criteria. Supports multiple values, for example sort=name,asc&sort=salary,desc",
            in = ParameterIn.QUERY,
            array = @ArraySchema(
                    schema = @Schema(type = "string")
            ),
            example = "name,asc"
    )
    @GetMapping
    public PageResponse<EmployeeResponse> getEmployees(

            // Pagination parameters
            @Parameter(
                    name = "page",
                    description = "Zero-based page number",
                    in = ParameterIn.QUERY,
                    example = "0",
                    required = false,
                    schema = @Schema(
                            type = "integer",
                            format = "int32",
                            defaultValue = "0"
                    )
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    name = "size",
                    description = "Number of employees per page",
                    in = ParameterIn.QUERY,
                    example = "10",
                    required = false,
                    schema = @Schema(
                            type = "integer",
                            format = "int32",
                            defaultValue = "10"
                    )
            )
            @RequestParam(defaultValue = "10") int size,

            // Filtering parameters
            @Parameter(
                    name = "id",
                    description = "Filter employees by employee ID",
                    in = ParameterIn.QUERY,
                    example = "5",
                    required = false,
                    schema = @Schema(type = "integer", format = "int64")
            )
            @RequestParam(required = false) Long id,

            @Parameter(
                    name = "name",
                    description = "Filter employees by name",
                    in = ParameterIn.QUERY,
                    example = "Amit",
                    required = false,
                    schema = @Schema(type = "string")
            )
            @RequestParam(required = false) String name,

            @Parameter(
                    name = "department",
                    description = "Filter employees by department",
                    in = ParameterIn.QUERY,
                    example = "Engineering",
                    required = false,
                    schema = @Schema(type = "string")
            )
            @RequestParam(required = false) String department,

            @Parameter(
                    name = "email",
                    description = "Filter employees by email address",
                    in = ParameterIn.QUERY,
                    example = "employee3@example.com",
                    required = false,
                    schema = @Schema(type = "string")
            )
            @RequestParam(required = false) String email,

            @Parameter(
                    name = "minSalary",
                    description = "Filter employees with salary greater than or equal to this value",
                    in = ParameterIn.QUERY,
                    example = "30000",
                    required = false,
                    schema = @Schema(type = "integer", format = "int64")
            )
            @RequestParam(required = false) Long minSalary,

            @Parameter(
                    name = "maxSalary",
                    description = "Filter employees with salary less than or equal to this value",
                    in = ParameterIn.QUERY,
                    example = "80000",
                    required = false,
                    schema = @Schema(type = "integer", format = "int64")
            )
            @RequestParam(required = false) Long maxSalary,

            @Parameter(
                    name = "keyword",
                    description = "Search employees using the supported keyword search functionality",
                    in = ParameterIn.QUERY,
                    example = "Amit",
                    required = false,
                    schema = @Schema(type = "string")
            )
            @RequestParam(required = false) String keyword,

            HttpServletRequest request
    ) {

        // Retrieve sorting parameters.
        String[] sortValues = request.getParameterValues("sort");

        List<String> sortParameters = sortValues == null
                ? null
                : Arrays.asList(sortValues);

        // Retrieve employees using filtering, pagination and sorting.
        Page<Employee> employeePage = employeeService.getEmployees(
                page,
                size,
                sortParameters,
                id,
                name,
                department,
                email,
                minSalary,
                maxSalary,
                keyword
        );

        // Convert Employee entities into EmployeeResponse DTOs.
        List<EmployeeResponse> employees =
                employeePage.getContent()
                        .stream()
                        .map(employeeMapper::toResponse)
                        .toList();

        // Construct the existing paginated response.
        return new PageResponse<>(
                employees,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isFirst(),
                employeePage.isLast()
        );
    }

    @Operation(
            summary = "Get employee by ID",
            description = "Retrieves the details of a specific employee using their employee ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = EmployeeResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid employee ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(
            @PathVariable Long id
    ) {
        Employee employee = employeeService.getEmployeeById(id);

        return employeeMapper.toResponse(employee);
    }

    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee record and returns the created employee details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee created successfully",
                    headers = {
                            @Header(
                                    name = "Location",
                                    description = "URI of the newly created employee resource",
                                    schema = @Schema(
                                            type = "string",
                                            format = "uri"
                                    )
                            )
                    },
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = EmployeeResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request
    ) {
        Employee employee = employeeMapper.toEntity(request);

        Employee savedEmployee =
                employeeService.createEmployee(employee);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedEmployee.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(employeeMapper.toResponse(savedEmployee));
    }

    @Operation(
            summary = "Delete employee",
            description = "Deletes an existing employee record using the employee ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Employee deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Replace employee details",
            description = "Updates an existing employee record using the complete employee request"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = EmployeeResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{id}")
    public EmployeeResponse updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        Employee employee = employeeMapper.toEntity(request);

        Employee updatedEmployee =
                employeeService.updateEmployee(id, employee);

        return employeeMapper.toResponse(updatedEmployee);
    }

//    @GetMapping("/me")
//    public String getCurrentUser(Authentication authentication) {
//
//        System.out.println("Name: " + authentication.getName());
//        System.out.println("Principal: " + authentication.getPrincipal());
//        System.out.println("Authorities: " + authentication.getAuthorities());
//        System.out.println("Authenticated: " + authentication.isAuthenticated());
//
//        return authentication.getName();
//    }

//    @GetMapping("/me")
//    public String getCurrentUser() {
//
//        Authentication authentication =
//                SecurityContextHolder.getContext().getAuthentication();
//
//        System.out.println("Name: " + authentication.getName());
//
//        return authentication.getName();
//    }

    @Operation(
            summary = "Get current logged-in user",
            description = "Retrieves the username of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current authenticated username retrieved successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/me")
    public String getCurrentUser() {
        return employeeService.getCurrentLoggedInUser();
    }


    @Operation(
            summary = "Partially update employee",
            description = "Updates only the supplied employee fields while preserving fields that are not provided"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee partially updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = EmployeeResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid PATCH request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the supplied token is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{id}")
    public EmployeeResponse patchEmployee(
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee fields to partially update. Only supplied fields are updated.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = EmployeePatchRequest.class
                            )
                    )
            )
            @RequestBody ObjectNode body
    ) {
        EmployeePatchRequest request =
                EmployeePatchRequest.from(body);

        Employee updatedEmployee =
                employeeService.patchEmployee(id, request);

        return employeeMapper.toResponse(updatedEmployee);
    }

//    @PatchMapping("/{id}")
//    public ResponseEntity<EmployeeResponse> patchEmployee(
//            @PathVariable Long id,
//            @RequestBody ObjectNode body
//    ) {
//
//        if (body.isEmpty()) {
//            throw new IllegalArgumentException(
//                    "PATCH request must contain at least one field"
//            );
//        }
//
//        if (body.size() > 4 ||
//                !body.fieldNames().hasNext()) {
//            throw new IllegalArgumentException(
//                    "Invalid PATCH request"
//            );
//        }
//
//        body.fieldNames().forEachRemaining(field -> {
//
//            if (!java.util.Set.of(
//                    "name", "department", "email", "salary"
//            ).contains(field)) {
//
//                throw new IllegalArgumentException(
//                        "Unsupported field: " + field
//                );
//            }
//        });
//
//        EmployeePatchRequest request =
//                EmployeePatchRequest.from(body);
//
//        request.validate();
//
//        Employee updatedEmployee =
//                employeeService.patchEmployee(id, request);
//
//        return ResponseEntity.ok(
//                employeeMapper.toResponse(updatedEmployee)
//        );
//    }
}
