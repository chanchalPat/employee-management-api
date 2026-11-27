package com.chanchal.employeemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.context.ActiveProfiles;
import com.chanchal.employeemanagement.employee.Employee;
import com.chanchal.employeemanagement.repository.EmployeeRepository;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("repository-test")
@Transactional
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateAndRetrieveEmployeeThroughRealApplicationLayers()
            throws Exception {

        String uniqueEmail = "integration-" + UUID.randomUUID() + "@example.com";

        String requestBody = """
            {
                "name": "Integration Test Employee",
                "department": "Engineering",
                "email": "%s",
                "salary": 75000
            }
            """.formatted(uniqueEmail);

        MvcResult createResult = mockMvc.perform(
                        post("/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name")
                        .value("Integration Test Employee"))
                .andExpect(jsonPath("$.department")
                        .value("Engineering"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode idNode = responseJson.get("id");

        assertNotNull(idNode, "Created employee response must contain an ID");
        assertEquals(false, idNode.isNull(),
                "Created employee ID must not be null");

        long employeeId = idNode.asLong();

        mockMvc.perform(get("/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name")
                        .value("Integration Test Employee"))
                .andExpect(jsonPath("$.department")
                        .value("Engineering"))
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withDefaultPagination_shouldReturnPaginatedResponse()
            throws Exception {

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.first").value(true));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withCustomPagination_shouldReturnRequestedPage()
            throws Exception {

        // Arrange: Insert two employees into the dedicated test database.

        String uniqueId = UUID.randomUUID().toString();

        employeeRepository.saveAllAndFlush(List.of(
                new Employee(
                        null,
                        "Pagination Employee A",
                        "PaginationTest",
                        50000L,
                        "pagination-a-" + uniqueId + "@example.com"
                ),
                new Employee(
                        null,
                        "Pagination Employee B",
                        "PaginationTest",
                        60000L,
                        "pagination-b-" + uniqueId + "@example.com"
                )
        ));

        // Act: Request the first page with a page size of two.

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.first").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withSecondPage_shouldReturnCorrectPaginationMetadata()
            throws Exception {

        mockMvc.perform(get("/employees")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.first").value(false));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withOutOfRangePage_shouldReturnEmptyContent()
            throws Exception {

        mockMvc.perform(get("/employees")
                        .param("page", "100")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.pageNumber").value(100))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withInvalidPageSize_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(get("/employees")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Page size must be between 1 and 100"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withSalaryDescending_shouldReturnSortedEmployees()
            throws Exception {

        String uniqueId = UUID.randomUUID().toString();

        employeeRepository.saveAll(List.of(
                new Employee(
                        null,
                        "Sorting Employee A",
                        "SortingTest",
                        987654321L,
                        "sorting-a-" + uniqueId + "@example.com"
                ),
                new Employee(
                        null,
                        "Sorting Employee B",
                        "SortingTest",
                        876543210L,
                        "sorting-b-" + uniqueId + "@example.com"
                ),
                new Employee(
                        null,
                        "Sorting Employee C",
                        "SortingTest",
                        765432100L,
                        "sorting-c-" + uniqueId + "@example.com"
                )
        ));

        MvcResult result = mockMvc.perform(
                        get("/employees")
                                .param("page", "0")
                                .param("size", "100")
                                .param("sort", "salary,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageSize").value(100))
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        JsonNode content = response.get("content");

        long previousSalary = Long.MAX_VALUE;

        for (JsonNode employee : content) {

            long currentSalary = employee.get("salary").asLong();

            org.junit.jupiter.api.Assertions.assertTrue(
                    currentSalary <= previousSalary,
                    "Employees must be sorted by salary descending"
            );

            previousSalary = currentSalary;
        }
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withSalaryAscending_shouldReturnSortedEmployees()
            throws Exception {

        String uniqueId = UUID.randomUUID().toString();

        String emailA = "sorting-asc-a-" + uniqueId + "@example.com";
        String emailB = "sorting-asc-b-" + uniqueId + "@example.com";
        String emailC = "sorting-asc-c-" + uniqueId + "@example.com";

        // Arrange: Insert controlled test data into the database.
        employeeRepository.saveAll(List.of(
                new Employee(
                        null,
                        "Sorting Employee A",
                        "SortingTest",
                        987654321L,
                        emailA
                ),
                new Employee(
                        null,
                        "Sorting Employee B",
                        "SortingTest",
                        876543210L,
                        emailB
                ),
                new Employee(
                        null,
                        "Sorting Employee C",
                        "SortingTest",
                        765432100L,
                        emailC
                )
        ));

        // Act: Request employees sorted by salary ascending.
        MvcResult result = mockMvc.perform(
                        get("/employees")
                                .param("page", "0")
                                .param("size", "100")
                                .param("sort", "salary,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageSize").value(100))
                .andReturn();

        // Assert: Verify the actual database-backed response ordering.
        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        JsonNode content = response.get("content");

        List<String> actualEmails = new java.util.ArrayList<>();

        for (JsonNode employee : content) {

            String email = employee.get("email").asText();

            if (email.equals(emailA)
                    || email.equals(emailB)
                    || email.equals(emailC)) {

                actualEmails.add(email);
            }
        }

        List<String> expectedEmails = List.of(
                emailC,
                emailB,
                emailA
        );

        assertEquals(
                expectedEmails,
                actualEmails,
                "Employees must be returned in ascending salary order"
        );
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withMultipleFieldSorting_shouldReturnCorrectOrder()
            throws Exception {

        String uniqueId = UUID.randomUUID().toString();

        String emailA = "multi-sort-a-" + uniqueId + "@example.com";
        String emailB = "multi-sort-b-" + uniqueId + "@example.com";
        String emailC = "multi-sort-c-" + uniqueId + "@example.com";
        String emailD = "multi-sort-d-" + uniqueId + "@example.com";

        // Arrange: Insert controlled test records.
        employeeRepository.saveAll(List.of(
                new Employee(
                        null, "Employee A", "AlphaSort",
                        300000L, emailA
                ),
                new Employee(
                        null, "Employee B", "AlphaSort",
                        100000L, emailB
                ),
                new Employee(
                        null, "Employee C", "BetaSort",
                        400000L, emailC
                ),
                new Employee(
                        null, "Employee D", "BetaSort",
                        200000L, emailD
                )
        ));

        // Act: Sort department ASC, then salary DESC.
        MvcResult result = mockMvc.perform(
                        get("/employees")
                                .param("page", "0")
                                .param("size", "100")
                                .param("sort",
                                        "department,asc",
                                        "salary,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageSize").value(100))
                .andReturn();

        // Assert: Verify the relative ordering of inserted employees.
        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        JsonNode content = response.get("content");

        List<String> actualEmails = new java.util.ArrayList<>();

        for (JsonNode employee : content) {

            String email = employee.get("email").asText();

            if (email.equals(emailA)
                    || email.equals(emailB)
                    || email.equals(emailC)
                    || email.equals(emailD)) {

                actualEmails.add(email);
            }
        }

        List<String> expectedEmails = List.of(
                emailA,
                emailB,
                emailC,
                emailD
        );

        assertEquals(
                expectedEmails,
                actualEmails,
                "Employees must be sorted by department ASC and salary DESC"
        );
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withSortingAndPagination_shouldReturnCorrectPage()
            throws Exception {

        String uniqueId = UUID.randomUUID().toString();

        // Arrange: Insert five employees with different salaries.
        employeeRepository.saveAll(List.of(
                new Employee(null, "Page Employee A", "PageSort",
                        900000L, "page-a-" + uniqueId + "@example.com"),
                new Employee(null, "Page Employee B", "PageSort",
                        800000L, "page-b-" + uniqueId + "@example.com"),
                new Employee(null, "Page Employee C", "PageSort",
                        700000L, "page-c-" + uniqueId + "@example.com"),
                new Employee(null, "Page Employee D", "PageSort",
                        600000L, "page-d-" + uniqueId + "@example.com"),
                new Employee(null, "Page Employee E", "PageSort",
                        500000L, "page-e-" + uniqueId + "@example.com")
        ));

        // Establish the expected database ordering.
        List<Employee> sortedEmployees = employeeRepository.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("salary"),
                        org.springframework.data.domain.Sort.Order.asc("id")
                )
        );

        // Page 1 with size 2 means records at index 2 and 3.
        List<Long> expectedIds = sortedEmployees.subList(2, 4)
                .stream()
                .map(Employee::getId)
                .toList();

        long totalElements = employeeRepository.count();

        // Act: Request the second page with descending salary sorting.
        MvcResult result = mockMvc.perform(
                        get("/employees")
                                .param("page", "1")
                                .param("size", "2")
                                .param("sort", "salary,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(totalElements))
                .andExpect(jsonPath("$.totalPages")
                        .value((int) Math.ceil(totalElements / 2.0)))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andReturn();

        // Assert: Compare the API response against the actual database page.
        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        JsonNode content = response.get("content");

        List<Long> actualIds = new java.util.ArrayList<>();

        for (JsonNode employee : content) {
            actualIds.add(employee.get("id").asLong());
        }

        assertEquals(
                expectedIds,
                actualIds,
                "API must return the correct database page after sorting"
        );
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withInvalidSortingField_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                        get("/employees")
                                .param("sort", "age,asc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sorting field: age"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withInvalidSortingDirection_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/employees")
                        .param("sort", "salary,invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sorting direction: invalid"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withMissingSortingDirection_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/employees")
                        .param("sort", "salary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sort format. Use field,direction"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withEmptySortingField_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/employees")
                        .param("sort", ",asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid sorting field: "));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withEmployeeIdFilter_shouldReturnMatchingEmployee()
            throws Exception {

        // Arrange: Create a unique employee in the test database.
        String uniqueEmail =
                "filter-id-" + UUID.randomUUID() + "@example.com";

        Employee savedEmployee = employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        "ID Filter Employee",
                        "FilteringTest",
                        75000L,
                        uniqueEmail
                )
        );

        Long employeeId = savedEmployee.getId();

        // Act: Request employees using the employee ID filter.
        mockMvc.perform(
                        get("/employees")
                                .param("id", employeeId.toString())
                )
                // Assert: Verify the API response.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(employeeId))
                .andExpect(jsonPath("$.content[0].name")
                        .value("ID Filter Employee"))
                .andExpect(jsonPath("$.content[0].email")
                        .value(uniqueEmail));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withEmployeeNameFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange: Create a unique employee.
        String uniqueEmail =
                "filter-name-" + UUID.randomUUID() + "@example.com";

        employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        "Senior Backend Engineer",
                        "FilteringTest",
                        85000L,
                        uniqueEmail
                )
        );

        // Act: Search using a partial name with different capitalization.
        mockMvc.perform(
                        get("/employees")
                                .param("name", "BACKEND")
                )
                // Assert: Verify the matching employee.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.content[0].email")
                        .value(uniqueEmail));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withDepartmentFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange: Create an employee with a unique email.
        String uniqueEmail =
                "filter-department-" + UUID.randomUUID() + "@example.com";

        employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        "Department Filter Employee",
                        "Quality Engineering",
                        90000L,
                        uniqueEmail
                )
        );

        // Act: Search using different capitalization.
        mockMvc.perform(
                        get("/employees")
                                .param("department", "quality engineering")
                )
                // Assert: Verify the matching department.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Department Filter Employee"))
                .andExpect(jsonPath("$.content[0].department")
                        .value("Quality Engineering"))
                .andExpect(jsonPath("$.content[0].email")
                        .value(uniqueEmail));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withEmailFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange: Create an employee with a unique email.
        String uniqueEmail =
                "filter-email-" + UUID.randomUUID() + "@example.com";

        employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        "Email Filter Employee",
                        "FilteringTest",
                        95000L,
                        uniqueEmail
                )
        );

        // Act: Search using uppercase email.
        mockMvc.perform(
                        get("/employees")
                                .param("email", uniqueEmail.toUpperCase())
                )
                // Assert: Verify the matching employee.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Email Filter Employee"))
                .andExpect(jsonPath("$.content[0].email")
                        .value(uniqueEmail));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getEmployees_withMinimumSalaryFilter_shouldReturnMatchingEmployees()
            throws Exception {

        // Arrange: Create employees with controlled salary values.
        String uniqueId = UUID.randomUUID().toString();

        employeeRepository.saveAllAndFlush(List.of(
                new Employee(
                        null,
                        "Minimum Salary Employee A",
                        "SalaryFilteringTest",
                        50000L,
                        "min-salary-a-" + uniqueId + "@example.com"
                ),
                new Employee(
                        null,
                        "Minimum Salary Employee B",
                        "SalaryFilteringTest",
                        60000L,
                        "min-salary-b-" + uniqueId + "@example.com"
                ),
                new Employee(
                        null,
                        "Minimum Salary Employee C",
                        "SalaryFilteringTest",
                        80000L,
                        "min-salary-c-" + uniqueId + "@example.com"
                )
        ));

        // Act: Request employees earning at least 60000.
        mockMvc.perform(
                        get("/employees")
                                .param("minSalary", "60000")
                )
                // Assert: Verify the filtered database results.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].salary").value(60000))
                .andExpect(jsonPath("$.content[1].salary").value(80000));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withMaximumSalaryFilter_shouldReturnMatchingEmployees() throws Exception {

        // Arrange: Create employees with different salary values

        Employee employee1 = new Employee();
        employee1.setName("Maximum Salary Test Employee A");
        employee1.setDepartment("Engineering");
        employee1.setEmail("maxsalary.a." + UUID.randomUUID() + "@example.com");
        employee1.setSalary(40000L);

        Employee employee2 = new Employee();
        employee2.setName("Maximum Salary Test Employee B");
        employee2.setDepartment("Engineering");
        employee2.setEmail("maxsalary.b." + UUID.randomUUID() + "@example.com");
        employee2.setSalary(60000L);

        Employee employee3 = new Employee();
        employee3.setName("Maximum Salary Test Employee C");
        employee3.setDepartment("Engineering");
        employee3.setEmail("maxsalary.c." + UUID.randomUUID() + "@example.com");
        employee3.setSalary(80000L);

        employeeRepository.saveAndFlush(employee1);
        employeeRepository.saveAndFlush(employee2);
        employeeRepository.saveAndFlush(employee3);

        // Act: Request employees with maximum salary of 60000

        mockMvc.perform(get("/employees")
                        .param("maxSalary", "60000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].salary",
                        containsInAnyOrder(40000, 60000)));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withCombinedFilters_shouldReturnMatchingEmployees() throws Exception {

        // Arrange: Create employees with different departments and salaries

        Employee employee1 = new Employee();
        employee1.setName("Combined Filter Employee A");
        employee1.setDepartment("Engineering");
        employee1.setEmail("combined.a." + UUID.randomUUID() + "@example.com");
        employee1.setSalary(50000L);

        Employee employee2 = new Employee();
        employee2.setName("Combined Filter Employee B");
        employee2.setDepartment("Engineering");
        employee2.setEmail("combined.b." + UUID.randomUUID() + "@example.com");
        employee2.setSalary(80000L);

        Employee employee3 = new Employee();
        employee3.setName("Combined Filter Employee C");
        employee3.setDepartment("Engineering");
        employee3.setEmail("combined.c." + UUID.randomUUID() + "@example.com");
        employee3.setSalary(90000L);

        Employee employee4 = new Employee();
        employee4.setName("Combined Filter Employee D");
        employee4.setDepartment("HR");
        employee4.setEmail("combined.d." + UUID.randomUUID() + "@example.com");
        employee4.setSalary(60000L);

        employeeRepository.saveAndFlush(employee1);
        employeeRepository.saveAndFlush(employee2);
        employeeRepository.saveAndFlush(employee3);
        employeeRepository.saveAndFlush(employee4);

        // Act: Apply department, minimum salary and maximum salary filters

        mockMvc.perform(get("/employees")
                        .param("department", "Engineering")
                        .param("minSalary", "50000")
                        .param("maxSalary", "80000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].salary",
                        containsInAnyOrder(50000, 80000)));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withFilteringAndPagination_shouldReturnMatchingPage() throws Exception {

        // Arrange: Create four employees in the Engineering department

        Employee employee1 = new Employee();
        employee1.setName("Filtering Pagination Employee A");
        employee1.setDepartment("Engineering");
        employee1.setEmail("filterpage.a." + UUID.randomUUID() + "@example.com");
        employee1.setSalary(40000L);

        Employee employee2 = new Employee();
        employee2.setName("Filtering Pagination Employee B");
        employee2.setDepartment("Engineering");
        employee2.setEmail("filterpage.b." + UUID.randomUUID() + "@example.com");
        employee2.setSalary(50000L);

        Employee employee3 = new Employee();
        employee3.setName("Filtering Pagination Employee C");
        employee3.setDepartment("Engineering");
        employee3.setEmail("filterpage.c." + UUID.randomUUID() + "@example.com");
        employee3.setSalary(60000L);

        Employee employee4 = new Employee();
        employee4.setName("Filtering Pagination Employee D");
        employee4.setDepartment("Engineering");
        employee4.setEmail("filterpage.d." + UUID.randomUUID() + "@example.com");
        employee4.setSalary(70000L);

        employeeRepository.saveAndFlush(employee1);
        employeeRepository.saveAndFlush(employee2);
        employeeRepository.saveAndFlush(employee3);
        employeeRepository.saveAndFlush(employee4);

        // Act: Filter employees by department and request the first page

        mockMvc.perform(get("/employees")
                        .param("department", "Engineering")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())

                // Verify returned employee records
                .andExpect(jsonPath("$.content.length()").value(2))

                // Verify pagination metadata
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withFilteringAndSorting_shouldReturnSortedMatchingEmployees() throws Exception {

        // Arrange: Create employees with different departments and salaries

        Employee employee1 = new Employee();
        employee1.setName("Filtering Sorting Employee A");
        employee1.setDepartment("Engineering");
        employee1.setEmail("filtersort.a." + UUID.randomUUID() + "@example.com");
        employee1.setSalary(40000L);

        Employee employee2 = new Employee();
        employee2.setName("Filtering Sorting Employee B");
        employee2.setDepartment("Engineering");
        employee2.setEmail("filtersort.b." + UUID.randomUUID() + "@example.com");
        employee2.setSalary(80000L);

        Employee employee3 = new Employee();
        employee3.setName("Filtering Sorting Employee C");
        employee3.setDepartment("Engineering");
        employee3.setEmail("filtersort.c." + UUID.randomUUID() + "@example.com");
        employee3.setSalary(60000L);

        Employee employee4 = new Employee();
        employee4.setName("Filtering Sorting Employee D");
        employee4.setDepartment("HR");
        employee4.setEmail("filtersort.d." + UUID.randomUUID() + "@example.com");
        employee4.setSalary(90000L);

        employeeRepository.saveAndFlush(employee1);
        employeeRepository.saveAndFlush(employee2);
        employeeRepository.saveAndFlush(employee3);
        employeeRepository.saveAndFlush(employee4);

        // Act: Filter by department and sort by salary descending

        mockMvc.perform(get("/employees")
                        .param("department", "Engineering")
                .param("sort", "salary,desc"))
                .andExpect(status().isOk())

                // Verify only matching employees are returned
                .andExpect(jsonPath("$.content.length()").value(3))

                // Verify descending salary order
                .andExpect(jsonPath("$.content[0].salary").value(80000))
                .andExpect(jsonPath("$.content[1].salary").value(60000))
                .andExpect(jsonPath("$.content[2].salary").value(40000));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withSalaryBoundaryValues_shouldIncludeBothBoundaries() throws Exception {

        // Arrange: Create employees around the salary boundaries

        Employee employee1 = new Employee();
        employee1.setName("Salary Boundary Employee A");
        employee1.setDepartment("Engineering");
        employee1.setEmail("salaryboundary.a." + UUID.randomUUID() + "@example.com");
        employee1.setSalary(49999L);

        Employee employee2 = new Employee();
        employee2.setName("Salary Boundary Employee B");
        employee2.setDepartment("Engineering");
        employee2.setEmail("salaryboundary.b." + UUID.randomUUID() + "@example.com");
        employee2.setSalary(50000L);

        Employee employee3 = new Employee();
        employee3.setName("Salary Boundary Employee C");
        employee3.setDepartment("Engineering");
        employee3.setEmail("salaryboundary.c." + UUID.randomUUID() + "@example.com");
        employee3.setSalary(70000L);

        Employee employee4 = new Employee();
        employee4.setName("Salary Boundary Employee D");
        employee4.setDepartment("Engineering");
        employee4.setEmail("salaryboundary.d." + UUID.randomUUID() + "@example.com");
        employee4.setSalary(70001L);

        employeeRepository.saveAndFlush(employee1);
        employeeRepository.saveAndFlush(employee2);
        employeeRepository.saveAndFlush(employee3);
        employeeRepository.saveAndFlush(employee4);

        // Act: Filter employees using minimum and maximum salary

        mockMvc.perform(get("/employees")
                        .param("minSalary", "50000")
                        .param("maxSalary", "70000"))
                .andExpect(status().isOk())

                // Verify exactly two employees match
                .andExpect(jsonPath("$.content.length()").value(2))

                // Verify both salary boundaries are included
                .andExpect(jsonPath("$.content[*].salary",
                        containsInAnyOrder(50000, 70000)));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withKeywordMatchingName_shouldReturnMatchingEmployee()
            throws Exception {

        // Arrange: Create a unique employee for keyword-search integration testing.

        String uniqueId = UUID.randomUUID().toString();

        String employeeName = "SearchName-" + uniqueId;

        String employeeDepartment = "IntegrationDepartment-" + uniqueId;

        String employeeEmail = "search-email-" + uniqueId + "@example.com";

        employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        employeeName,
                        employeeDepartment,
                        75000L,
                        employeeEmail
                )
        );

        // Act: Search using an uppercase partial keyword.

        String keyword = ("SEARCHNAME-" + uniqueId.substring(0, 8))
                .toUpperCase();

        mockMvc.perform(
                        get("/employees")
                                .param("keyword", keyword)
                )

                // Assert: Verify the actual database-backed search response.

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value(employeeName))
                .andExpect(jsonPath("$.content[0].department")
                        .value(employeeDepartment))
                .andExpect(jsonPath("$.content[0].email").value(employeeEmail));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withKeywordMatchingDepartment_shouldReturnMatchingEmployee()
            throws Exception {

        // Arrange: Create an employee with a unique department.

        String uniqueId = UUID.randomUUID().toString();

        String employeeName = "DepartmentSearch-" + uniqueId;

        String employeeDepartment = "QualityEngineering-" + uniqueId;

        String employeeEmail = "department-search-" + uniqueId + "@example.com";

        employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        employeeName,
                        employeeDepartment,
                        80000L,
                        employeeEmail
                )
        );

        // Act: Search using a partial, uppercase department keyword.

        String keyword = ("QUALITYENGINEERING-" + uniqueId.substring(0, 8))
                .toUpperCase();

        mockMvc.perform(
                        get("/employees")
                                .param("keyword", keyword)
                )

                // Assert: Verify the matching employee.

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value(employeeName))
                .andExpect(jsonPath("$.content[0].department")
                        .value(employeeDepartment))
                .andExpect(jsonPath("$.content[0].email").value(employeeEmail));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withKeywordMatchingEmail_shouldReturnMatchingEmployee()
            throws Exception {

        // Arrange: Create an employee with a unique email address.

        String uniqueId = UUID.randomUUID().toString();

        String employeeName = "EmailSearchEmployee-" + uniqueId;

        String employeeDepartment = "FinanceDepartment-" + uniqueId;

        String employeeEmail = "EmailSearch-" + uniqueId + "@example.com";

        employeeRepository.saveAndFlush(
                new Employee(
                        null,
                        employeeName,
                        employeeDepartment,
                        85000L,
                        employeeEmail
                )
        );

        // Act: Search using a partial, uppercase email keyword.

        String keyword = ("EMAILSEARCH-" + uniqueId.substring(0, 8))
                .toUpperCase();

        mockMvc.perform(
                        get("/employees")
                                .param("keyword", keyword)
                )

                // Assert: Verify that the employee is returned.

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value(employeeName))
                .andExpect(jsonPath("$.content[0].department")
                        .value(employeeDepartment))
                .andExpect(jsonPath("$.content[0].email").value(employeeEmail));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withKeywordAndDepartmentFilter_shouldReturnOnlyMatchingEmployees()
            throws Exception {

        // Arrange: Create a unique keyword for this integration test.

        String uniqueId = UUID.randomUUID().toString();

        String keyword = "SearchCombined-" + uniqueId.substring(0, 8);

        // Employee A: Matches keyword and department filter.

        Employee employeeA = new Employee(
                null,
                keyword + "-EmployeeA",
                "Engineering",
                70000L,
                "combined-a-" + uniqueId + "@example.com"
        );

        // Employee B: Matches keyword but not department filter.

        Employee employeeB = new Employee(
                null,
                keyword + "-EmployeeB",
                "HR",
                75000L,
                "combined-b-" + uniqueId + "@example.com"
        );

        // Employee C: Matches department filter but not keyword.

        Employee employeeC = new Employee(
                null,
                "UnrelatedEmployee-" + uniqueId,
                "Engineering",
                80000L,
                "unrelated-" + uniqueId + "@example.com"
        );

        employeeRepository.saveAll(
                List.of(employeeA, employeeB, employeeC)
        );

        employeeRepository.flush();

        // Act: Search using keyword and department filter together.

        mockMvc.perform(
                        get("/employees")
                                .param("keyword", keyword.toUpperCase())
                                .param("department", "Engineering")
                )

                // Assert: Only Employee A should be returned.

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value(employeeA.getName()))
                .andExpect(jsonPath("$.content[0].department")
                        .value("Engineering"))
                .andExpect(jsonPath("$.content[0].email")
                        .value(employeeA.getEmail()));
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getEmployees_withKeywordPaginationAndSorting_shouldReturnCorrectPage()
            throws Exception {

        // Arrange: Create three employees with the same unique keyword


        employeeRepository.saveAllAndFlush(List.of(
                new Employee(
                        null,
                        "SearchPageSort Alpha",
                        "Engineering",
                        45000L,
                        "search-pagesort-alpha@example.com"
                ),
                new Employee(
                        null,
                        "SearchPageSort Beta",
                        "Engineering",
                        75000L,
                        "search-pagesort-beta@example.com"
                ),
                new Employee(
                        null,
                        "SearchPageSort Gamma",
                        "Engineering",
                        95000L,
                        "search-pagesort-gamma@example.com"
                )
        ));

        // Act & Assert: Search, paginate, and sort in one request

        mockMvc.perform(get("/employees")
                        .param("keyword", "SearchPageSort")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "salary,desc"))
                .andExpect(status().isOk())

                // Verify returned records
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].salary").value(95000))
                .andExpect(jsonPath("$.content[1].salary").value(75000))

                // Verify pagination metadata
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))

                // Verify keyword matching
                .andExpect(jsonPath("$.content[0].name")
                        .value("SearchPageSort Gamma"))
                .andExpect(jsonPath("$.content[1].name")
                        .value("SearchPageSort Beta"));
    }

}
