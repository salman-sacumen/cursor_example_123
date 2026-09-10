package com.example.crud;

import com.example.crud.config.EmployeeDataLoader;
import com.example.crud.model.Employee;
import com.example.crud.model.EmployeeStatus;
import com.example.crud.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void createEmployeeWithFullPayloadThenReadUpdateAndDelete() throws Exception {
        Employee request = new Employee(
                "Noah",
                "Patel",
                "noah.patel@example.com",
                "+1-202-555-0121",
                "Engineering",
                "Backend Engineer",
                105000.00,
                LocalDate.of(2025, 1, 8),
                "Denver",
                EmployeeStatus.ACTIVE);

        String createResponse = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Noah"))
                .andExpect(jsonPath("$.email").value("noah.patel@example.com"))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Engineer"))
                .andExpect(jsonPath("$.salary").value(105000.00))
                .andExpect(jsonPath("$.hireDate").value("2025-01-08"))
                .andExpect(jsonPath("$.location").value("Denver"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/employees").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Patel"));

        mockMvc.perform(get("/api/employees/email/noah.patel@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+1-202-555-0121"));

        request.setJobTitle("Senior Backend Engineer");
        request.setSalary(118000.00);
        mockMvc.perform(put("/api/employees/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.salary").value(118000.00));

        Employee duplicate = new Employee(
                "Other",
                "Person",
                "noah.patel@example.com",
                "Sales",
                "Account Executive",
                70000.00);
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/employees/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void sampleEmployeesAreLoadedWhenTableIsEmpty() throws Exception {
        new EmployeeDataLoader(employeeRepository).run(null);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].email").exists());

        mockMvc.perform(get("/api/employees").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
