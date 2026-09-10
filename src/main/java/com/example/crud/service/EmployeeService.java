package com.example.crud.service;

import com.example.crud.exception.DuplicateEmailException;
import com.example.crud.model.Employee;
import com.example.crud.model.EmployeeStatus;
import com.example.crud.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> search(String name, String department, String jobTitle, EmployeeStatus status) {
        String nameFilter = blankToNull(name);
        String departmentFilter = blankToNull(department);
        String jobTitleFilter = blankToNull(jobTitle);
        if (nameFilter == null && departmentFilter == null && jobTitleFilter == null && status == null) {
            return employeeRepository.findAll();
        }
        return employeeRepository.search(nameFilter, departmentFilter, jobTitleFilter, status);
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmailIgnoreCase(email);
    }

    public Employee create(Employee employee) {
        normalize(employee);
        assertEmailAvailable(employee.getEmail(), null);
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }
        return employeeRepository.save(employee);
    }

    public Optional<Employee> update(Long id, Employee updated) {
        return employeeRepository.findById(id).map(existing -> {
            normalize(updated);
            assertEmailAvailable(updated.getEmail(), id);
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setEmail(updated.getEmail());
            existing.setPhone(updated.getPhone());
            existing.setDepartment(updated.getDepartment());
            existing.setJobTitle(updated.getJobTitle());
            existing.setSalary(updated.getSalary());
            existing.setHireDate(updated.getHireDate());
            existing.setLocation(updated.getLocation());
            existing.setStatus(updated.getStatus() == null ? EmployeeStatus.ACTIVE : updated.getStatus());
            return employeeRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            return false;
        }
        employeeRepository.deleteById(id);
        return true;
    }

    private void assertEmailAvailable(String email, Long currentId) {
        if (email == null) {
            return;
        }
        boolean taken = currentId == null
                ? employeeRepository.existsByEmailIgnoreCase(email)
                : employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (taken) {
            throw new DuplicateEmailException(email);
        }
    }

    private static void normalize(Employee employee) {
        employee.setFirstName(blankToNull(employee.getFirstName()));
        employee.setLastName(blankToNull(employee.getLastName()));
        employee.setEmail(blankToNull(employee.getEmail()) == null ? null : employee.getEmail().trim().toLowerCase());
        employee.setPhone(blankToNull(employee.getPhone()));
        employee.setDepartment(blankToNull(employee.getDepartment()));
        employee.setJobTitle(blankToNull(employee.getJobTitle()));
        employee.setLocation(blankToNull(employee.getLocation()));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
