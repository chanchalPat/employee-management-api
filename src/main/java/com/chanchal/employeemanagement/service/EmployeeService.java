package com.chanchal.employeemanagement.service;

import com.chanchal.employeemanagement.employee.Employee;
import com.chanchal.employeemanagement.exception.EmployeeNotFoundException;
import com.chanchal.employeemanagement.repository.EmployeeRepository;
import com.chanchal.employeemanagement.specification.EmployeeSpecification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.chanchal.employeemanagement.dto.EmployeePatchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Service
public class EmployeeService {
//    public List<Employee> getEmployees(){
//
//        List<Employee> employees = new ArrayList<>();
//
//        employees.add(new Employee(
//                101L,
//                "Rahul",
//                "Developer",
//                70000L,
//                "employee1@example.com"));
//
//        employees.add(new Employee(
//                102L,
//                "John",
//                "QA",
//                60000L,
//                "employee2@example.com"));
//
//        employees.add(new Employee(
//                103L,
//                "Amit",
//                "Manager",
//                90000L,
//                "employee3@example.com"));
//
//        return employees;
//    }

    private final EmployeeRepository employeeRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "name",
            "department",
            "email",
            "salary"
    );

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<Employee> getEmployees(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return employeeRepository.findAll(pageable);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<Employee> getEmployees(
            int page,
            int size,
            List<String> sortParameters
    ) {
        Pageable pageable = createPageable(page, size, sortParameters);

        return employeeRepository.findAll(pageable);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with id " + id + " not found"
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployee(Long id) {
        employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with id " + id + " not found"
                        )
                );

        employeeRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Employee updateEmployee(Long id, Employee employee) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with id " + id + " not found"
                        )
                );

        existingEmployee.setName(employee.getName());
        existingEmployee.setDepartment(employee.getDepartment());
        existingEmployee.setSalary(employee.getSalary());
        existingEmployee.setEmail(employee.getEmail());

        return employeeRepository.save(existingEmployee);
    }

//    public Employee updateEmployee(Long id, Employee updatedEmployee) {
//
//        Employee existingEmployee = employeeRepository.findById(id)
//                .orElseThrow(() -> new EmployeeNotFoundException(
//                        "Employee with id " + id + " not found"
//                ));
//
//        existingEmployee.setName(updatedEmployee.getName());
//        existingEmployee.setDepartment(updatedEmployee.getDepartment());
//        existingEmployee.setSalary(updatedEmployee.getSalary());
//        existingEmployee.setEmail(updatedEmployee.getEmail());
//
//        return employeeRepository.save(existingEmployee);
//    }

    public String getCurrentLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }


    @PreAuthorize("hasRole('ADMIN')")
    public Employee patchEmployee(
            Long id,
            EmployeePatchRequest request
    ) {

        // Validate the PATCH request first.
        request.validate();

        // Retrieve the existing employee.
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with id " + id + " not found"
                        )
                );

        // Update only the supplied fields.
        request.applyTo(existingEmployee);

        // Persist the updated employee.
        return employeeRepository.save(existingEmployee);
    }


    private Pageable createPageable(
            int page,
            int size,
            List<String> sortParameters
    ) {

        // Validate pagination.
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        // Build sorting orders.
        List<Sort.Order> orders = new ArrayList<>();
        Set<String> processedFields = new HashSet<>();

        if (sortParameters != null) {

            for (String parameter : sortParameters) {

                if (parameter == null || parameter.isBlank()) {
                    throw new IllegalArgumentException(
                            "Sort parameter must not be empty"
                    );
                }

                String[] parts = parameter.split(",", -1);

                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                            "Invalid sort format. Use field,direction"
                    );
                }

                String field = parts[0].trim();
                String direction = parts[1].trim();

                if (!ALLOWED_SORT_FIELDS.contains(field)) {
                    throw new IllegalArgumentException(
                            "Invalid sorting field: " + field
                    );
                }

                if (!processedFields.add(field)) {
                    throw new IllegalArgumentException(
                            "Duplicate sorting field: " + field
                    );
                }

                Sort.Direction sortDirection;

                if ("asc".equalsIgnoreCase(direction)) {
                    sortDirection = Sort.Direction.ASC;
                } else if ("desc".equalsIgnoreCase(direction)) {
                    sortDirection = Sort.Direction.DESC;
                } else {
                    throw new IllegalArgumentException(
                            "Invalid sorting direction: " + direction
                    );
                }

                orders.add(new Sort.Order(sortDirection, field));
            }
        }

        // Add ID as secondary sorting field when necessary.
        if (!orders.isEmpty() && !processedFields.contains("id")) {
            orders.add(Sort.Order.asc("id"));
        }

        // Create sorting configuration.
        Sort sort = orders.isEmpty()
                ? Sort.unsorted()
                : Sort.by(orders);

        // Combine pagination and sorting.
        return PageRequest.of(page, size, sort);
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<Employee> getEmployees(
            int page,
            int size,
            List<String> sortParameters,
            Long id,
            String name,
            String department,
            String email,
            Long minSalary,
            Long maxSalary,
            String keyword
    ) {

        // Validate employee ID.
        if (id != null && id <= 0) {
            throw new IllegalArgumentException(
                    "Employee ID must be greater than 0"
            );
        }

        // Validate minimum salary.
        if (minSalary != null && minSalary < 0) {
            throw new IllegalArgumentException(
                    "Minimum salary must be greater than or equal to 0"
            );
        }

        // Validate maximum salary.
        if (maxSalary != null && maxSalary < 0) {
            throw new IllegalArgumentException(
                    "Maximum salary must be greater than or equal to 0"
            );
        }

        // Validate salary range.
        if (minSalary != null
                && maxSalary != null
                && minSalary > maxSalary) {

            throw new IllegalArgumentException(
                    "Minimum salary must not exceed maximum salary"
            );
        }

        // Build dynamic filtering conditions.
        var specification = EmployeeSpecification.filterEmployees(
                id,
                name,
                department,
                email,
                minSalary,
                maxSalary,
                keyword
        );

        // Reuse existing pagination and sorting configuration.
        Pageable pageable = createPageable(
                page,
                size,
                sortParameters
        );

        // Execute filtering, pagination and sorting together.
        return employeeRepository.findAll(specification, pageable);
    }
}
