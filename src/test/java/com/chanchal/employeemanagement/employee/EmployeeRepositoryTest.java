package com.chanchal.employeemanagement.employee;

import com.chanchal.employeemanagement.repository.EmployeeRepository;
import com.chanchal.employeemanagement.specification.EmployeeSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import com.chanchal.employeemanagement.employee.Employee;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(
        locations = "classpath:application-repository-test.properties"
)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldSaveAndRetrieveEmployee() {

        Employee employee = new Employee();
        employee.setName("Repository Test Employee");
        employee.setDepartment("Engineering");
        employee.setEmail("repository.test@example.com");
        employee.setSalary(75000L);

        Employee savedEmployee = employeeRepository.save(employee);

        assertThat(savedEmployee.getId()).isNotNull();

        Optional<Employee> result =
                employeeRepository.findById(savedEmployee.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("Repository Test Employee");
        assertThat(result.get().getEmail())
                .isEqualTo("repository.test@example.com");
    }

    @Test
    void shouldUpdateEmployee() {

        Employee employee = new Employee();
        employee.setName("Original Name");
        employee.setDepartment("Engineering");
        employee.setEmail("update.test@example.com");
        employee.setSalary(75000L);

        Employee savedEmployee = employeeRepository.saveAndFlush(employee);

        savedEmployee.setDepartment("Quality Engineering");
        employeeRepository.saveAndFlush(savedEmployee);

        Optional<Employee> result =
                employeeRepository.findById(savedEmployee.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDepartment())
                .isEqualTo("Quality Engineering");
    }

    @Test
    void shouldDeleteEmployee() {

        Employee employee = new Employee();
        employee.setName("Employee To Delete");
        employee.setDepartment("Testing");
        employee.setEmail("delete.test@example.com");
        employee.setSalary(60000L);

        Employee savedEmployee = employeeRepository.saveAndFlush(employee);
        Long employeeId = savedEmployee.getId();

        employeeRepository.deleteById(employeeId);
        employeeRepository.flush();

        assertThat(employeeRepository.findById(employeeId)).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenEmployeeDoesNotExist() {

        Optional<Employee> result =
                employeeRepository.findById(-1L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAllSavedEmployees() {

        Employee firstEmployee = new Employee();
        firstEmployee.setName("First Repository Employee");
        firstEmployee.setDepartment("Engineering");
        firstEmployee.setEmail("first.repository@example.com");
        firstEmployee.setSalary(70000L);

        Employee secondEmployee = new Employee();
        secondEmployee.setName("Second Repository Employee");
        secondEmployee.setDepartment("Finance");
        secondEmployee.setEmail("second.repository@example.com");
        secondEmployee.setSalary(80000L);

        employeeRepository.save(firstEmployee);
        employeeRepository.save(secondEmployee);

        assertThat(employeeRepository.findAll())
                .hasSize(2)
                .extracting(Employee::getEmail)
                .containsExactlyInAnyOrder(
                        "first.repository@example.com",
                        "second.repository@example.com"
                );
    }

    private Employee createEmployee(
            String name,
            String department,
            String email,
            Long salary) {

        Employee employee = new Employee();
        employee.setName(name);
        employee.setDepartment(department);
        employee.setEmail(email);
        employee.setSalary(salary);

        return employee;
    }

    @Test
    void testFindByDepartmentIgnoreCase() {
        employeeRepository.saveAndFlush(
                createEmployee("Rahul", "IT",
                        "r6.rahul@example.com", 50000L));

        employeeRepository.saveAndFlush(
                createEmployee("Priya", "HR",
                        "r6.priya@example.com", 40000L));

        List<Employee> result =
                employeeRepository.findByDepartmentIgnoreCase("IT");

        assertEquals(1, result.size());
        assertEquals("Rahul", result.get(0).getName());
        assertEquals("IT", result.get(0).getDepartment());
    }

    @Test
    void testDepartmentLookupIgnoresCase() {
        employeeRepository.saveAndFlush(
                createEmployee("Amit", "IT",
                        "r7.amit@example.com", 60000L));

        List<Employee> result =
                employeeRepository.findByDepartmentIgnoreCase("it");

        assertEquals(1, result.size());
        assertEquals("Amit", result.get(0).getName());
    }

    @Test
    void testFindBySalaryGreaterThanEqual() {
        employeeRepository.saveAndFlush(
                createEmployee("Neha", "IT",
                        "r8.neha@example.com", 40000L));

        employeeRepository.saveAndFlush(
                createEmployee("Karan", "IT",
                        "r8.karan@example.com", 60000L));

        List<Employee> result =
                employeeRepository.findBySalaryGreaterThanEqual(50000L);

        assertEquals(1, result.size());
        assertEquals("Karan", result.get(0).getName());
        assertTrue(result.get(0).getSalary() >= 50000L);
    }

    @Test
    void testFindByEmailIgnoreCase() {
        employeeRepository.saveAndFlush(
                createEmployee("Sneha", "Finance",
                        "r9.sneha@example.com", 55000L));

        Optional<Employee> result =
                employeeRepository.findByEmailIgnoreCase(
                        "R9.SNEHA@EXAMPLE.COM");

        assertTrue(result.isPresent());
        assertEquals("Sneha", result.get().getName());
    }

    @Test
    void testFindByEmailWhenEmployeeDoesNotExist() {
        Optional<Employee> result =
                employeeRepository.findByEmailIgnoreCase(
                        "nobody-r10@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindEmployeesByDepartmentAndMinimumSalary() {
        employeeRepository.saveAndFlush(
                createEmployee("Arjun", "IT",
                        "r11.arjun@example.com", 70000L));

        employeeRepository.saveAndFlush(
                createEmployee("Meera", "IT",
                        "r11.meera@example.com", 40000L));

        employeeRepository.saveAndFlush(
                createEmployee("Vijay", "HR",
                        "r11.vijay@example.com", 80000L));

        List<Employee> result =
                employeeRepository
                        .findEmployeesByDepartmentAndMinimumSalary(
                                "IT", 50000L);

        assertEquals(1, result.size());
        assertEquals("Arjun", result.get(0).getName());
    }

    @Test
    void testFindByDepartmentReturnsEmptyList() {
        List<Employee> result =
                employeeRepository.findByDepartmentIgnoreCase(
                        "NoSuchDepartmentR12");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDuplicateEmailIsRejected() {
        String email = "r13.duplicate@example.com";

        employeeRepository.saveAndFlush(
                createEmployee("Employee One", "IT", email, 50000L));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> employeeRepository.saveAndFlush(
                        createEmployee(
                                "Employee Two", "HR", email, 60000L))
        );
    }

    @Test
    void testNullEmailIsRejected() {
        Employee employee = createEmployee(
                "Employee Null Email",
                "IT",
                null,
                50000L
        );

        assertThrows(
                jakarta.validation.ConstraintViolationException.class,
                () -> employeeRepository.saveAndFlush(employee)
        );
    }

    @Test
    void testNegativeSalaryIsRejected() {
        Employee employee = createEmployee(
                "Employee Negative Salary",
                "IT",
                "r15.negative@example.com",
                -1000L
        );

        assertThrows(
                jakarta.validation.ConstraintViolationException.class,
                () -> employeeRepository.saveAndFlush(employee)
        );
    }


    @Test
    void testKeywordSearchByEmployeeName() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Rahul Sharma",
                        "Engineering",
                        "rahul.sharma@example.com",
                        75000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Priya Patel",
                        "Finance",
                        "priya.patel@example.com",
                        60000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "rahul"
                )
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Rahul Sharma", result.get(0).getName());
        assertEquals("Engineering", result.get(0).getDepartment());
    }


    @Test
    void testKeywordSearchByDepartment() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Amit Sharma",
                        "Quality Engineering",
                        "amit.sharma@example.com",
                        70000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Priya Patel",
                        "Finance",
                        "priya.patel@example.com",
                        60000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "quality"
                )
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Amit Sharma", result.get(0).getName());
        assertEquals("Quality Engineering", result.get(0).getDepartment());
    }


    @Test
    void testKeywordSearchByEmail() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Sneha Patil",
                        "Finance",
                        "sneha.patil@example.com",
                        65000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Amit Sharma",
                        "Engineering",
                        "amit.sharma@example.com",
                        70000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "sneha.patil"
                )
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Sneha Patil", result.get(0).getName());
        assertEquals("sneha.patil@example.com", result.get(0).getEmail());
    }


    @Test
    void testKeywordSearchIgnoresCase() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Rahul Sharma",
                        "Engineering",
                        "rahul.sharma@example.com",
                        75000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "RAHUL"
                )
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Rahul Sharma", result.get(0).getName());
    }


    @Test
    void testKeywordSearchSupportsPartialMatching() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Rahul Sharma",
                        "Engineering",
                        "rahul.sharma@example.com",
                        75000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Priya Patel",
                        "Finance",
                        "priya.patel@example.com",
                        60000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "rah"
                )
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Rahul Sharma", result.get(0).getName());
    }


    @Test
    void testKeywordSearchMatchesMultipleFields() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Rahul Sharma",
                        "Engineering",
                        "rahul@example.com",
                        75000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Engineering Specialist",
                        "Quality",
                        "specialist@example.com",
                        65000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Priya Patel",
                        "Finance",
                        "priya.engineering@example.com",
                        60000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "engineering"
                )
        );

        // Assert
        assertEquals(3, result.size());

        assertThat(result)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder(
                        "Rahul Sharma",
                        "Engineering Specialist",
                        "Priya Patel"
                );
    }


    @Test
    void testKeywordSearchReturnsEmptyListWhenNoMatchExists() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Rahul Sharma",
                        "Engineering",
                        "rahul@example.com",
                        75000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Priya Patel",
                        "Finance",
                        "priya@example.com",
                        60000L
                )
        );

        // Act
        List<Employee> result = employeeRepository.findAll(
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "NonExistingKeywordXYZ"
                )
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void testKeywordSearchWorksWithDepartmentFilter() {

        // Arrange
        employeeRepository.saveAndFlush(
                createEmployee(
                        "Rahul Sharma",
                        "Engineering",
                        "rahul.sharma@example.com",
                        75000L
                )
        );

        employeeRepository.saveAndFlush(
                createEmployee(
                        "Priya Patel",
                        "Finance",
                        "priya.patel@example.com",
                        60000L
                )
        );

        // Prepare combined search specification
        Specification<Employee> specification =
                EmployeeSpecification.filterEmployees(
                        null,
                        null,
                        "Engineering",
                        null,
                        null,
                        null,
                        "rahul"
                );

        // Act
        List<Employee> employees =
                employeeRepository.findAll(specification);

        // Assert
        assertThat(employees)
                .hasSize(1)
                .extracting(Employee::getName)
                .containsExactly("Rahul Sharma");

        assertThat(employees.get(0).getDepartment())
                .isEqualTo("Engineering");
    }

}
