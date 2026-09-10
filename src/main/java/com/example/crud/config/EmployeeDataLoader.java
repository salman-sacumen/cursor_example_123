package com.example.crud.config;

import com.example.crud.model.Employee;
import com.example.crud.model.EmployeeStatus;
import com.example.crud.repository.EmployeeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EmployeeDataLoader implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;

    public EmployeeDataLoader(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (employeeRepository.count() > 0) {
            return;
        }
        employeeRepository.saveAll(List.of(
                new Employee("Aisha", "Khan", "aisha.khan@example.com", "+1-202-555-0147",
                        "Engineering", "Software Engineer", 98000.00, LocalDate.of(2021, 3, 15),
                        "New York", EmployeeStatus.ACTIVE),
                new Employee("Diego", "Ramirez", "diego.ramirez@example.com", "+1-202-555-0172",
                        "Engineering", "Staff Engineer", 145000.00, LocalDate.of(2018, 7, 9),
                        "Austin", EmployeeStatus.ACTIVE),
                new Employee("Mei", "Chen", "mei.chen@example.com", "+1-202-555-0198",
                        "Product", "Product Manager", 120000.00, LocalDate.of(2020, 1, 6),
                        "Seattle", EmployeeStatus.ACTIVE),
                new Employee("Jonah", "Brooks", "jonah.brooks@example.com", "+1-202-555-0113",
                        "Design", "UX Designer", 92000.00, LocalDate.of(2022, 11, 21),
                        "Chicago", EmployeeStatus.ON_LEAVE),
                new Employee("Priya", "Nair", "priya.nair@example.com", "+1-202-555-0164",
                        "People", "HR Business Partner", 88000.00, LocalDate.of(2019, 5, 2),
                        "Boston", EmployeeStatus.ACTIVE),
                new Employee("Liam", "Okafor", "liam.okafor@example.com", "+1-202-555-0139",
                        "Finance", "Financial Analyst", 81000.00, LocalDate.of(2023, 2, 13),
                        "Remote", EmployeeStatus.ACTIVE),
                new Employee("Sofia", "Rossi", "sofia.rossi@example.com", "+1-202-555-0188",
                        "Engineering", "QA Engineer", 79000.00, LocalDate.of(2024, 8, 1),
                        "London", EmployeeStatus.INACTIVE)
        ));
    }
}
