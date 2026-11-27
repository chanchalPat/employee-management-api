package com.chanchal.employeemanagement.repository;

import com.chanchal.employeemanagement.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends
        JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    List<Employee> findByDepartmentIgnoreCase(String department);

    List<Employee> findBySalaryGreaterThanEqual(Long salary);

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT e
        FROM Employee e
        WHERE LOWER(e.department) = LOWER(:department)
          AND e.salary >= :minimumSalary
        """)
    List<Employee> findEmployeesByDepartmentAndMinimumSalary(
            @Param("department") String department,
            @Param("minimumSalary") Long minimumSalary
    );
}
