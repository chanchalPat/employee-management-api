package com.chanchal.employeemanagement.controller;

import com.chanchal.employeemanagement.employee.Employee;
import com.chanchal.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.chanchal.employeemanagement.mapper.EmployeeMapper;
import com.chanchal.employeemanagement.security.JwtService;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.ArgumentMatchers.anyInt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(EmployeeMapper.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtService jwtService;

//    @Test
//    void getEmployees_shouldReturnAllEmployees() throws Exception {
//        // Arrange
//        Employee employee = new Employee(
//                1L, "Rahul Singh", "IT", 60000L, "rahul@example.com"
//        );
//
//        when(employeeService.getEmployees())
//                .thenReturn(List.of(employee));
//
//        // Act and Assert
//        mockMvc.perform(get("/employees"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(
//                        org.springframework.http.MediaType.APPLICATION_JSON
//                ))
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[0].name").value("Rahul Singh"))
//                .andExpect(jsonPath("$[0].department").value("IT"))
//                .andExpect(jsonPath("$[0].email")
//                        .value("rahul@example.com"))
//                .andExpect(jsonPath("$[0].salary").value(60000));
//
//        verify(employeeService).getEmployees();
//    }


    @Test
    void getEmployees_shouldReturnDefaultPaginatedEmployees() throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        ))
                .thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        org.springframework.http.MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Rahul Singh"))
                .andExpect(jsonPath("$.content[0].department").value("IT"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("rahul@example.com"))
                .andExpect(jsonPath("$.content[0].salary").value(60000))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withCustomPagination_shouldReturnRequestedPage()
            throws Exception {

        // Arrange
        Employee employee1 = new Employee(
                10L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com"
        );

        Employee employee2 = new Employee(
                11L, "Amit Sharma", "QA",
                55000L, "amit@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee1, employee2),
                PageRequest.of(1, 2),
                6
        );

        when(employeeService.getEmployees(
                eq(1),
                eq(2),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        ))
                .thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.content[1].name")
                        .value("Amit Sharma"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        verify(employeeService).getEmployees(
                eq(1),
                eq(2),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withOutOfRangePage_shouldReturnEmptyContent()
            throws Exception {

        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(3, 2),
                6
        );

        when(employeeService.getEmployees(
                eq(3),
                eq(2),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        ))
                .thenReturn(emptyPage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("page", "3")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageNumber").value(3))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));

        verify(employeeService).getEmployees(
                eq(3),
                eq(2),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT", 60000L, "rahul@example.com"
        );

        when(employeeService.getEmployeeById(1L))
                .thenReturn(employee);

        // Act and Assert
        mockMvc.perform(get("/employees/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul Singh"));

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT", 60000L, "rahul@example.com"
        );

        when(employeeService.createEmployee(any(Employee.class)))
                .thenReturn(employee);

        String requestBody = """
            {
              "name": "Rahul Singh",
              "department": "IT",
              "email": "rahul@example.com",
              "salary": 60000
            }
            """;

        // Act and Assert
        mockMvc.perform(post("/employees")
                        .contentType(
                                org.springframework.http.MediaType.APPLICATION_JSON
                        )
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/employees/1"
                ))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul Singh"));

        verify(employeeService).createEmployee(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldReturnUpdatedEmployee() throws Exception {
        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Updated", "Engineering",
                75000L, "rahul@example.com"
        );

        when(employeeService.updateEmployee(eq(1L), any(Employee.class)))
                .thenReturn(employee);

        String requestBody = """
                {
                  "name": "Rahul Updated",
                  "department": "Engineering",
                  "email": "rahul@example.com",
                  "salary": 75000
                }
                """;

        // Act and Assert
        mockMvc.perform(put("/employees/{id}", 1L)
                        .contentType(
                                org.springframework.http.MediaType.APPLICATION_JSON
                        )
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rahul Updated"))
                .andExpect(jsonPath("$.salary").value(75000));

        verify(employeeService)
                .updateEmployee(eq(1L), any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldReturnNoContentStatus() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act and Assert
        mockMvc.perform(delete("/employees/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void getCurrentUser_shouldReturnCurrentUser() throws Exception {
        // Arrange
        when(employeeService.getCurrentLoggedInUser())
                .thenReturn("rahul");

        // Act and Assert
        mockMvc.perform(get("/employees/me"))
                .andExpect(status().isOk());

        verify(employeeService).getCurrentLoggedInUser();
    }

    @Test
    void getEmployees_withNegativePage_shouldReturnBadRequest() throws Exception {

        when(employeeService.getEmployees(
                eq(-1),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        ))
                .thenThrow(new IllegalArgumentException(
                        "Page number must be greater than or equal to 0"
                ));

        mockMvc.perform(get("/employees")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Page number must be greater than or equal to 0"));
    }

    @Test
    void getEmployees_withZeroPageSize_shouldReturnBadRequest() throws Exception {

        when(employeeService.getEmployees(
                eq(0),
                eq(0),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        ))
                .thenThrow(new IllegalArgumentException(
                        "Page size must be between 1 and 100"
                ));

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Page size must be between 1 and 100"));
    }

    @Test
    void getEmployees_withPageSizeGreaterThan100_shouldReturnBadRequest() throws Exception {

        when(employeeService.getEmployees(
                eq(0),
                eq(101),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        ))
                .thenThrow(new IllegalArgumentException(
                        "Page size must be between 1 and 100"
                ));

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Page size must be between 1 and 100"));
    }

    @Test
    void getEmployees_withNonNumericPage_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/employees")
                        .param("page", "abc")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'page'. Expected type: int"));
    }

    @Test
    void getEmployees_withNonNumericSize_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'size'. Expected type: int"));
    }

    @Test
    void getEmployees_withSorting_shouldReturnSortedEmployees() throws Exception {

        Employee employee = new Employee(
                1L,
                "Amit Sharma",
                "Engineering",
                65000L,
                "amit@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        mockMvc.perform(get("/employees")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("Amit Sharma"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of("name,asc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withMultipleSorting_shouldAcceptMultipleParameters()
            throws Exception {

        Employee employee = new Employee(
                1L,
                "Rahul Singh",
                "Engineering",
                75000L,
                "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 5),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(5),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "department,asc")
                        .param("sort", "salary,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].salary").value(75000))
                .andExpect(jsonPath("$.pageSize").value(5));

        verify(employeeService).getEmployees(
                eq(0),
                eq(5),
                eq(List.of("department,asc", "salary,desc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withDescendingSorting_shouldReturnSortedEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L,
                "Rahul Singh",
                "Engineering",
                75000L,
                "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of("name,desc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withInvalidSortingField_shouldReturnBadRequest()
            throws Exception {

        // Arrange
        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Invalid sorting field: age"
        ));

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("sort", "age,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sorting field: age"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of("age,asc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withInvalidSortingDirection_shouldReturnBadRequest()
            throws Exception {

        // Arrange
        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Invalid sorting direction: sideways"
        ));

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("sort", "name,sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sorting direction: sideways"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of("name,sideways")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withMissingSortingDirection_shouldReturnBadRequest()
            throws Exception {

        // Arrange
        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Invalid sort format. Use field,direction"
        ));

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("sort", "name"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sort format. Use field,direction"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of("name")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withEmptySortingField_shouldReturnBadRequest()
            throws Exception {

        // Arrange
        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Invalid sorting field: "
        ));

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("sort", ",asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sorting field: "));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of(",asc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withEmptySortingParameter_shouldReturnBadRequest()
            throws Exception {

        // Arrange
        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                anyList(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Sort parameter must not be empty"
        ));

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("sort", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Sort parameter must not be empty"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                eq(List.of("")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withIdFilter_shouldReturnMatchingEmployee()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                eq(1L),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withNameFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                eq("Rahul"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("name", "Rahul"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                eq("Rahul"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }


    @Test
    void getEmployees_withKeywordSearch_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L,
                "Rahul Singh",
                "Engineering",
                60000L,
                "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul")
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("keyword", "rahul"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.content[0].department")
                        .value("Engineering"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("rahul@example.com"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul")
        );
    }


    @Test
    void getEmployees_withKeywordAndDepartmentFilter_shouldPassBothParameters()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L,
                "Rahul Singh",
                "Engineering",
                60000L,
                "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                eq("Engineering"),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul")
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("keyword", "rahul")
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.content[0].department")
                        .value("Engineering"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("rahul@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                eq("Engineering"),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul")
        );
    }


    @Test
    void getEmployees_withKeywordSearchPaginationAndSorting_shouldPassAllParameters()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L,
                "Rahul Singh",
                "Engineering",
                60000L,
                "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(1, 5),
                6
        );

        when(employeeService.getEmployees(
                eq(1),
                eq(5),
                eq(List.of("name,asc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul")
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("keyword", "rahul")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));

        verify(employeeService).getEmployees(
                eq(1),
                eq(5),
                eq(List.of("name,asc")),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul")
        );
    }


    @Test
    void getEmployees_withDepartmentFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "Engineering",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                eq("Engineering"),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department")
                        .value("Engineering"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                eq("Engineering"),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withEmailFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul@example.com"),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("email", "rahul@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email")
                        .value("rahul@example.com"));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("rahul@example.com"),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withMinSalaryFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(50000L),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("minSalary", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].salary")
                        .value(60000));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(50000L),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withMaxSalaryFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(70000L),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("maxSalary", "70000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].salary")
                        .value(60000));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(70000L),
                isNull()
        );
    }

    @Test
    void getEmployees_withMultipleFilters_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                eq("Rahul"),
                eq("Engineering"),
                isNull(),
                eq(50000L),
                eq(80000L),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("name", "Rahul")
                        .param("department", "Engineering")
                        .param("minSalary", "50000")
                        .param("maxSalary", "80000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.content[0].department")
                        .value("Engineering"))
                .andExpect(jsonPath("$.content[0].salary")
                        .value(65000));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                eq("Rahul"),
                eq("Engineering"),
                isNull(),
                eq(50000L),
                eq(80000L),
                isNull()
        );
    }

    @Test
    void getEmployees_withoutFilteringParameters_shouldReturnAllEmployees()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "IT",
                60000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(0, 10),
                1
        );

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

        verify(employeeService).getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void getEmployees_withFilteringPaginationAndSorting_shouldPassAllParameters()
            throws Exception {

        // Arrange
        Employee employee = new Employee(
                1L, "Rahul Singh", "Engineering",
                65000L, "rahul@example.com"
        );

        Page<Employee> employeePage = new PageImpl<>(
                List.of(employee),
                PageRequest.of(1, 5),
                6
        );

        when(employeeService.getEmployees(
                eq(1),
                eq(5),
                anyList(),
                isNull(),
                eq("Rahul"),
                eq("Engineering"),
                isNull(),
                eq(50000L),
                eq(80000L),
                isNull()
        )).thenReturn(employeePage);

        // Act and Assert
        mockMvc.perform(get("/employees")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "salary,desc")
                        .param("name", "Rahul")
                        .param("department", "Engineering")
                        .param("minSalary", "50000")
                        .param("maxSalary", "80000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Rahul Singh"))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(6));

        verify(employeeService).getEmployees(
                eq(1),
                eq(5),
                eq(List.of("salary,desc")),
                isNull(),
                eq("Rahul"),
                eq("Engineering"),
                isNull(),
                eq(50000L),
                eq(80000L),
                isNull()
        );
    }

    @Test
    void getEmployees_withNonNumericId_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/employees")
                        .param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'id'. Expected type: Long"));

        verify(employeeService, never()).getEmployees(
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                isNull()
        );
    }

    @Test
    void getEmployees_withNonNumericMinSalary_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/employees")
                        .param("minSalary", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'minSalary'. Expected type: Long"));

        verify(employeeService, never()).getEmployees(
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                isNull()
        );
    }

    @Test
    void getEmployees_withNonNumericMaxSalary_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/employees")
                        .param("maxSalary", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'maxSalary'. Expected type: Long"));

        verify(employeeService, never()).getEmployees(
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                isNull()
        );
    }

    @Test
    void getEmployees_withZeroId_shouldReturnBadRequest()
            throws Exception {

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                eq(0L),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Employee ID must be greater than 0"
        ));

        mockMvc.perform(get("/employees")
                        .param("id", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Employee ID must be greater than 0"));
    }

    @Test
    void getEmployees_withNegativeMinSalary_shouldReturnBadRequest()
            throws Exception {

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(-1000L),
                isNull(),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Minimum salary must be greater than or equal to 0"
        ));

        mockMvc.perform(get("/employees")
                        .param("minSalary", "-1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Minimum salary must be greater than or equal to 0"));
    }

    @Test
    void getEmployees_withNegativeMaxSalary_shouldReturnBadRequest()
            throws Exception {

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(-1000L),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Maximum salary must be greater than or equal to 0"
        ));

        mockMvc.perform(get("/employees")
                        .param("maxSalary", "-1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Maximum salary must be greater than or equal to 0"));
    }

    @Test
    void getEmployees_withInvalidSalaryRange_shouldReturnBadRequest()
            throws Exception {

        when(employeeService.getEmployees(
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(90000L),
                eq(50000L),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "Minimum salary must not exceed maximum salary"
        ));

        mockMvc.perform(get("/employees")
                        .param("minSalary", "90000")
                        .param("maxSalary", "50000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Minimum salary must not exceed maximum salary"));
    }
}
