package com.example.crud.service;

import com.example.crud.exception.DuplicateEmailException;
import com.example.crud.model.Employee;
import com.example.crud.model.EmployeeStatus;
import com.example.crud.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(
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
    }

    @Test
    void search_withoutFilters_returnsAll() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.search(" ", null, null, null);

        assertThat(result).containsExactly(employee);
        verify(employeeRepository).findAll();
    }

    @Test
    void search_withFilters_delegatesToRepository() {
        when(employeeRepository.search("Aisha", "Engineering", "Software", EmployeeStatus.ACTIVE))
                .thenReturn(List.of(employee));

        List<Employee> result = employeeService.search("Aisha", "Engineering", "Software", EmployeeStatus.ACTIVE);

        assertThat(result).containsExactly(employee);
    }

    @Test
    void create_savesNormalizedEmployee() {
        Employee toCreate = new Employee("Aisha", "Khan", "Aisha.Khan@Example.com",
                "Engineering", "Software Engineer", 98000.00);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.create(toCreate);

        assertThat(result.getEmail()).isEqualTo("aisha.khan@example.com");
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        verify(employeeRepository).save(toCreate);
    }

    @Test
    void create_withDuplicateEmail_throwsConflict() {
        Employee toCreate = new Employee("Aisha", "Khan", "aisha.khan@example.com",
                "Engineering", "Software Engineer", 98000.00);
        when(employeeRepository.existsByEmailIgnoreCase("aisha.khan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(toCreate))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("aisha.khan@example.com");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_whenExists_replacesFields() {
        Employee updated = new Employee(
                "Aisha",
                "Khan",
                "aisha.khan@example.com",
                "+1-202-555-0100",
                "Engineering",
                "Senior Software Engineer",
                115000.00,
                LocalDate.of(2021, 3, 15),
                "Boston",
                EmployeeStatus.ACTIVE);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Employee> result = employeeService.update(1L, updated);

        assertThat(result).isPresent();
        assertThat(result.get().getJobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(result.get().getLocation()).isEqualTo("Boston");
        assertThat(result.get().getSalary()).isEqualTo(115000.00);
    }

    @Test
    void delete_whenMissing_returnsFalse() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThat(employeeService.delete(99L)).isFalse();
        verify(employeeRepository, never()).deleteById(any());
    }
}
