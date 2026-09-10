package com.example.crud.repository;

import com.example.crud.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(String isbn, Long id);

    @Query("""
            SELECT b FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
              AND (:genre IS NULL OR LOWER(b.genre) = LOWER(:genre))
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            """)
    List<Book> search(
            @Param("title") String title,
            @Param("author") String author,
            @Param("genre") String genre,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
}
