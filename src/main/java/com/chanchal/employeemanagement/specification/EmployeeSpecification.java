package com.chanchal.employeemanagement.specification;

import com.chanchal.employeemanagement.employee.Employee;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> filterEmployees(
            Long id,
            String name,
            String department,
            String email,
            Long minSalary,
            Long maxSalary,
            String keyword) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("id"), id)
                );
            }

            if (name != null && !name.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.trim().toLowerCase() + "%"
                        )
                );
            }

            if (department != null && !department.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("department")),
                                department.trim().toLowerCase()
                        )
                );
            }

            if (email != null && !email.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("email")),
                                email.trim().toLowerCase()
                        )
                );
            }

            if (minSalary != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("salary"),
                                minSalary
                        )
                );
            }

            if (maxSalary != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("salary"),
                                maxSalary
                        )
                );
            }

            // Search employees by keyword across name, department and email.
            if (keyword != null && !keyword.isBlank()) {

                String searchPattern = "%"
                        + keyword.trim().toLowerCase(java.util.Locale.ROOT)
                        + "%";

                Predicate searchPredicate = criteriaBuilder.or(

                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                searchPattern
                        ),

                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("department")),
                                searchPattern
                        ),

                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                searchPattern
                        )

                );

                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
