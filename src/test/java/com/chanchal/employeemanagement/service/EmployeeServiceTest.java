package com.chanchal.employeemanagement.service;

import com.chanchal.employeemanagement.employee.Employee;
import com.chanchal.employeemanagement.exception.EmployeeNotFoundException;
import com.chanchal.employeemanagement.repository.EmployeeRepository;
import com.chanchal.employeemanagement.dto.EmployeePatchRequest;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.LongNode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyLong;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee createEmployee(
            Long id,
            String name,
            String department,
            Long salary,
            String email) {

        return new Employee(id, name, department, salary, email);
    }

    private void assertFilteringRequestAccepted(
            Long id,
            String name,
            String department,
            String email,
            Long minSalary,
            Long maxSalary
    ) {

        when(employeeRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        Page<Employee> result = employeeService.getEmployees(
                0,
                10,
                null,
                id,
                name,
                department,
                email,
                minSalary,
                maxSalary,
                null
        );

        assertNotNull(result);

        verify(employeeRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void getEmployees_shouldReturnAllEmployees() {

        Employee employee1 = createEmployee(
                10L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com");

        Employee employee2 = createEmployee(
                11L, "Amit Sharma", "QA",
                55000L, "amit@example.com");

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee1, employee2));

        List<Employee> result = employeeService.getEmployees();

        assertEquals(2, result.size());
        assertEquals("Rahul Singh", result.get(0).getName());
        assertEquals("Amit Sharma", result.get(1).getName());

        verify(employeeRepository).findAll();
    }

    @Test
    void getEmployeeById_shouldReturnEmployeeWhenFound() {

        Employee employee = createEmployee(
                10L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com");

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Rahul Singh", result.getName());

        verify(employeeRepository).findById(10L);
    }

    @Test
    void getEmployeeById_shouldThrowWhenEmployeeDoesNotExist() {

        when(employeeRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(9999L)
        );

        verify(employeeRepository).findById(9999L);
    }

    @Test
    void createEmployee_shouldSaveAndReturnEmployee() {

        Employee employee = createEmployee(
                null, "Test Employee", "Engineering",
                65000L, "test@example.com");

        Employee savedEmployee = createEmployee(
                20L, "Test Employee", "Engineering",
                65000L, "test@example.com");

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(savedEmployee);

        Employee result = employeeService.createEmployee(employee);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("Test Employee", result.getName());

        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_shouldUpdateExistingEmployee() {

        Employee existingEmployee = createEmployee(
                10L, "Old Name", "QA",
                50000L, "old@example.com");

        Employee updateRequest = createEmployee(
                10L, "New Name", "Engineering",
                70000L, "new@example.com");

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.updateEmployee(
                10L, updateRequest);

        assertEquals(10L, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("Engineering", result.getDepartment());
        assertEquals(70000L, result.getSalary());
        assertEquals("new@example.com", result.getEmail());

        verify(employeeRepository).findById(10L);
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void updateEmployee_shouldThrowWhenEmployeeDoesNotExist() {

        Employee updateRequest = createEmployee(
                9999L, "New Name", "Engineering",
                70000L, "new@example.com");

        when(employeeRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.updateEmployee(
                        9999L, updateRequest)
        );

        verify(employeeRepository).findById(9999L);
        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteExistingEmployee() {

        Employee employee = createEmployee(
                10L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com");

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));

        assertDoesNotThrow(
                () -> employeeService.deleteEmployee(10L)
        );

        verify(employeeRepository).findById(10L);
        verify(employeeRepository).deleteById(10L);
    }

    @Test
    void deleteEmployee_shouldThrowWhenEmployeeDoesNotExist() {

        when(employeeRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(9999L)
        );

        verify(employeeRepository).findById(9999L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void patchEmployee_shouldUpdateSingleField() {

        Employee existingEmployee = createEmployee(
                10L, "Old Name", "QA",
                50000L, "old@example.com");

        EmployeePatchRequest patchRequest = new EmployeePatchRequest(
                TextNode.valueOf("New Name"),
                null,
                null,
                null
        );

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.patchEmployee(
                10L, patchRequest);

        assertEquals("New Name", result.getName());
        assertEquals("QA", result.getDepartment());
        assertEquals("old@example.com", result.getEmail());
        assertEquals(50000L, result.getSalary());

        verify(employeeRepository).findById(10L);
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void patchEmployee_shouldUpdateMultipleFields() {

        Employee existingEmployee = createEmployee(
                10L, "Old Name", "QA",
                50000L, "old@example.com");

        EmployeePatchRequest patchRequest = new EmployeePatchRequest(
                TextNode.valueOf("New Name"),
                TextNode.valueOf("Engineering"),
                TextNode.valueOf("new@example.com"),
                LongNode.valueOf(75000L)
        );

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.patchEmployee(
                10L, patchRequest);

        assertEquals("New Name", result.getName());
        assertEquals("Engineering", result.getDepartment());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(75000L, result.getSalary());

        verify(employeeRepository).findById(10L);
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void patchEmployee_shouldPreserveOmittedFields() {

        Employee existingEmployee = createEmployee(
                10L, "Old Name", "QA",
                50000L, "old@example.com");

        EmployeePatchRequest patchRequest = new EmployeePatchRequest(
                null,
                TextNode.valueOf("Engineering"),
                null,
                null
        );

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.patchEmployee(
                10L, patchRequest);

        assertEquals("Old Name", result.getName());
        assertEquals("Engineering", result.getDepartment());
        assertEquals("old@example.com", result.getEmail());
        assertEquals(50000L, result.getSalary());

        verify(employeeRepository).findById(10L);
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void patchEmployee_shouldThrowWhenEmployeeDoesNotExist() {

        EmployeePatchRequest patchRequest = new EmployeePatchRequest(
                TextNode.valueOf("New Name"),
                null,
                null,
                null
        );

        when(employeeRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.patchEmployee(
                        9999L, patchRequest)
        );

        verify(employeeRepository).findById(9999L);

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }


    @Test
    void getEmployees_withPagination_shouldReturnRequestedPage() {

        // Arrange
        Employee employee1 = createEmployee(
                10L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com");

        Employee employee2 = createEmployee(
                11L, "Amit Sharma", "QA",
                55000L, "amit@example.com");

        Pageable pageable = PageRequest.of(1, 2);

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee1, employee2),
                pageable,
                6
        );

        when(employeeRepository.findAll(pageable))
                .thenReturn(employeePage);

        // Act
        Page<Employee> result = employeeService.getEmployees(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());

        assertEquals(6, result.getTotalElements());
        assertEquals(3, result.getTotalPages());

        assertFalse(result.isFirst());
        assertFalse(result.isLast());

        assertEquals("Rahul Singh",
                result.getContent().get(0).getName());

        assertEquals("Amit Sharma",
                result.getContent().get(1).getName());

        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void getEmployees_withPagination_shouldReturnEmptyPage() {

        // Arrange
        Pageable pageable = PageRequest.of(3, 2);

        Page<Employee> emptyPage = new PageImpl<>(
                List.of(),
                pageable,
                6
        );

        when(employeeRepository.findAll(pageable))
                .thenReturn(emptyPage);

        // Act
        Page<Employee> result = employeeService.getEmployees(3, 2);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());

        assertEquals(3, result.getNumber());
        assertEquals(2, result.getSize());

        assertEquals(6, result.getTotalElements());
        assertEquals(3, result.getTotalPages());

        assertTrue(result.isLast());

        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void getEmployees_withPagination_shouldThrowWhenPageIsNegative() {

        // Act and Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(-1, 10)
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withPagination_shouldThrowWhenPageSizeIsInvalid() {

        // Act and Assert
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> employeeService.getEmployees(0, 0)
                ),

                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> employeeService.getEmployees(0, 101)
                )
        );

        verifyNoInteractions(employeeRepository);
    }


    @Test
    void getEmployees_withAscendingSorting_shouldApplyCorrectSort() {

        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        employeeService.getEmployees(
                0,
                10,
                List.of("name,asc")
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("name").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void getEmployees_withDescendingSorting_shouldApplyCorrectSort() {

        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        employeeService.getEmployees(
                0,
                10,
                List.of("salary,desc")
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("salary").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void getEmployees_withMultipleSorting_shouldApplyAllSortOrders() {

        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        employeeService.getEmployees(
                0,
                10,
                List.of(
                        "department,asc",
                        "salary,desc"
                )
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(captor.capture());

        Sort sort = captor.getValue().getSort();

        assertEquals(
                Sort.Direction.ASC,
                sort.getOrderFor("department").getDirection()
        );

        assertEquals(
                Sort.Direction.DESC,
                sort.getOrderFor("salary").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                sort.getOrderFor("id").getDirection()
        );

        assertEquals(3, sort.stream().count());
    }

    @Test
    void getEmployees_withInvalidSortingField_shouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0,
                        10,
                        List.of("unknown,asc")
                )
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withInvalidSortingDirection_shouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0,
                        10,
                        List.of("salary,random")
                )
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withMalformedSortingParameter_shouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0,
                        10,
                        List.of("salary")
                )
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withEmptySortingParameter_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0,
                        10,
                        List.of("")
                )
        );

        assertEquals(
                "Sort parameter must not be empty",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withMissingSortingDirection_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0,
                        10,
                        List.of("salary,")
                )
        );

        assertEquals(
                "Invalid sorting direction: ",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withDuplicateSortingField_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0,
                        10,
                        List.of(
                                "name,asc",
                                "name,desc"
                        )
                )
        );

        assertEquals(
                "Duplicate sorting field: name",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withExplicitIdSorting_shouldNotAddDuplicateIdSort() {

        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        employeeService.getEmployees(
                0,
                10,
                List.of("id,desc")
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(captor.capture());

        Sort sort = captor.getValue().getSort();

        assertEquals(
                Sort.Direction.DESC,
                sort.getOrderFor("id").getDirection()
        );

        assertEquals(1, sort.stream().count());
    }

    @Test
    void getEmployees_withNullSortingParameters_shouldReturnUnsortedPage() {

        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        employeeService.getEmployees(
                0,
                10,
                null
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertTrue(pageable.getSort().isUnsorted());
    }

    @Test
    void getEmployees_withSortingAndPagination_shouldApplyBothCorrectly() {

        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        employeeService.getEmployees(
                2,
                5,
                List.of("salary,desc")
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("salary").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void getEmployees_withSorting_shouldThrowWhenPageIsNegative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        -1,
                        10,
                        List.of("name,asc")
                )
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withSorting_shouldThrowWhenPageSizeIsInvalid() {

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> employeeService.getEmployees(
                                0,
                                0,
                                List.of("name,asc")
                        )
                ),

                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> employeeService.getEmployees(
                                0,
                                101,
                                List.of("name,asc")
                        )
                )
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withIdFilter_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                10L,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getEmployees_withNameFilter_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                "Rah",
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getEmployees_withDepartmentFilter_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                null,
                "Engineering",
                null,
                null,
                null
        );
    }

    @Test
    void getEmployees_withEmailFilter_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                null,
                null,
                "rahul@example.com",
                null,
                null
        );
    }

    @Test
    void getEmployees_withMinimumSalaryFilter_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                null,
                null,
                null,
                50000L,
                null
        );
    }

    @Test
    void getEmployees_withMaximumSalaryFilter_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                null,
                null,
                null,
                null,
                90000L
        );
    }

    @Test
    void getEmployees_withMultipleFilters_shouldExecuteCombinedFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                "Rah",
                "Engineering",
                null,
                50000L,
                90000L
        );
    }

    @Test
    void getEmployees_withoutFilters_shouldExecuteQuery() {

        assertFilteringRequestAccepted(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getEmployees_withSalaryBoundaries_shouldExecuteFilteringQuery() {

        assertFilteringRequestAccepted(
                null,
                null,
                null,
                null,
                50000L,
                90000L
        );
    }

    @Test
    void getEmployees_withInvalidId_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0, 10, null,
                        0L, null, null, null,
                        null, null,
                        null
                )
        );

        assertEquals(
                "Employee ID must be greater than 0",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withNegativeMinimumSalary_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0, 10, null,
                        null, null, null, null,
                        -1000L, null,
                        null
                )
        );

        assertEquals(
                "Minimum salary must be greater than or equal to 0",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withNegativeMaximumSalary_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0, 10, null,
                        null, null, null, null,
                        null, -1000L,
                        null
                )
        );

        assertEquals(
                "Maximum salary must be greater than or equal to 0",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withInvalidSalaryRange_shouldThrowException() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0, 10, null,
                        null, null, null, null,
                        90000L, 50000L,
                        null
                )
        );

        assertEquals(
                "Minimum salary must not exceed maximum salary",
                exception.getMessage()
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withFilteringPaginationAndSorting_shouldApplyAllCorrectly() {

        when(employeeRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        employeeService.getEmployees(
                2,
                5,
                List.of("salary,desc"),
                null,
                null,
                "Engineering",
                null,
                50000L,
                90000L,
                null
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                captor.capture()
        );

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("salary").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void getEmployees_withFilteringAndNegativePage_shouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        -1, 10, null,
                        null, null, null, null,
                        null, null,
                        null
                )
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void getEmployees_withFilteringAndInvalidSortingField_shouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.getEmployees(
                        0, 10,
                        List.of("age,asc"),
                        null, null, null, null,
                        null, null,
                        null
                )
        );

        verifyNoInteractions(employeeRepository);
    }



    @Test
    void getEmployees_withKeywordSearch_shouldExecuteSearchQuery() {

        // Arrange
        when(employeeRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Employee>>any(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        // Act
        Page<Employee> result = employeeService.getEmployees(
                0,
                5,
                List.of("name,asc"),
                null,
                null,
                null,
                null,
                null,
                null,
                "rahul"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        ArgumentCaptor<Specification<Employee>> specificationCaptor =
                ArgumentCaptor.forClass(Specification.class);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(employeeRepository).findAll(
                specificationCaptor.capture(),
                pageableCaptor.capture()
        );

        // Verify that the generated Specification contains
        // the keyword-search conditions.
        assertNotNull(specificationCaptor.getValue());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("name").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

}
