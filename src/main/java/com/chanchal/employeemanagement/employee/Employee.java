package com.chanchal.employeemanagement.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank
    private String name;

    @Column(name = "department", nullable = false, length = 100)
    @NotBlank
    private String department;

    @Column(name = "salary")
    @NotNull
    @Min(1)
    private Long salary;

    @Column(name = "email", nullable = false, unique = true, length = 254)
    @NotBlank
    @Email
    private String email;

    public Employee() {
    }

    public Employee(Long id, String name, String department, Long salary, String email) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public Long getSalary() {
        return salary;
    }

    public String getEmail() {
        return email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
