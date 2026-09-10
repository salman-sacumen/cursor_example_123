package com.example.crud.controller;

import com.example.crud.exception.DuplicateEmailException;
import com.example.crud.model.Employee;
import com.example.crud.model.EmployeeStatus;
import com.example.crud.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getEmployees_returnsOk() throws Exception {
        Employee employee = sampleEmployee();
        when(employeeService.search(isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Aisha"))
                .andExpect(jsonPath("$[0].email").value("aisha.khan@example.com"));
    }

    @Test
    void getEmployees_withFilters_returnsMatches() throws Exception {
        Employee employee = sampleEmployee();
        when(employeeService.search(eq("Aisha"), eq("Engineering"), eq("Software"), eq(EmployeeStatus.ACTIVE)))
                .thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees")
                        .param("name", "Aisha")
                        .param("department", "Engineering")
                        .param("jobTitle", "Software")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Engineering"));
    }

    @Test
    void getEmployeeById_whenExists_returnsOk() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(sampleEmployee()));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Khan"));
    }

    @Test
    void getEmployeeByEmail_whenExists_returnsOk() throws Exception {
        when(employeeService.findByEmail("aisha.khan@example.com")).thenReturn(Optional.of(sampleEmployee()));

        mockMvc.perform(get("/api/employees/email/aisha.khan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Software Engineer"));
    }

    @Test
    void createEmployee_returnsCreated() throws Exception {
        Employee request = sampleEmployee();
        request.setId(null);
        Employee created = sampleEmployee();
        when(employeeService.create(any(Employee.class))).thenReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.phone").value("+1-202-555-0147"))
                .andExpect(jsonPath("$.location").value("New York"))
                .andExpect(jsonPath("$.hireDate").value("2021-03-15"));
    }

    @Test
    void createEmployee_withDuplicateEmail_returnsConflict() throws Exception {
        Employee request = sampleEmployee();
        request.setId(null);
        when(employeeService.create(any(Employee.class)))
                .thenThrow(new DuplicateEmailException("aisha.khan@example.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An employee with email aisha.khan@example.com already exists"));
    }

    @Test
    void createEmployee_withInvalidBody_returnsBadRequest() throws Exception {
        Employee invalid = new Employee("", "", "not-an-email", "Engineering", "Engineer", -10.0);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEmployee_whenExists_returnsOk() throws Exception {
        Employee request = sampleEmployee();
        request.setJobTitle("Senior Software Engineer");
        when(employeeService.update(eq(1L), any(Employee.class))).thenReturn(Optional.of(request));

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Senior Software Engineer"));
    }

    @Test
    void deleteEmployee_whenExists_returnsNoContent() throws Exception {
        when(employeeService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    private static Employee sampleEmployee() {
        Employee employee = new Employee(
                "Aisha",
                "Khan",
                "aisha.khan@example.com",
                "+1-202-555-0147",
                "Engineering",
                "Software Engineer",
                98000.00,
                LocalDate.of(2021, 3, 15),
                "New York",
                EmployeeStatus.ACTIVE);
        employee.setId(1L);
        return employee;
    }
}
