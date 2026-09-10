package com.example.crud.repository;

import com.example.crud.model.Employee;
import com.example.crud.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:name IS NULL
                   OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                   OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:department IS NULL OR LOWER(e.department) = LOWER(:department))
              AND (:jobTitle IS NULL OR LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%')))
              AND (:status IS NULL OR e.status = :status)
            """)
    List<Employee> search(
            @Param("name") String name,
            @Param("department") String department,
            @Param("jobTitle") String jobTitle,
            @Param("status") EmployeeStatus status);
}
