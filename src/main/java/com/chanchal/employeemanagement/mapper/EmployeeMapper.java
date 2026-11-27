package com.chanchal.employeemanagement.mapper;

import com.chanchal.employeemanagement.dto.EmployeeRequest;
import com.chanchal.employeemanagement.dto.EmployeeResponse;
import com.chanchal.employeemanagement.employee.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();

        employee.setName(request.name());
        employee.setDepartment(request.department());
        employee.setEmail(request.email());
        employee.setSalary(request.salary());

        return employee;
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getDepartment(),
                employee.getEmail(),
                employee.getSalary()
        );
    }

//    public void updateEntity(
//            Employee employee,
//            EmployeeRequest request
//    ) {
//        employee.setName(request.name());
//        employee.setDepartment(request.department());
//        employee.setEmail(request.email());
//        employee.setSalary(request.salary());
//    }
}
