package com.example.crud.service;

import com.example.crud.dto.BookPatchRequest;
import com.example.crud.exception.DuplicateIsbnException;
import com.example.crud.model.Book;
import com.example.crud.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> search(String title, String author, String genre, Double minPrice, Double maxPrice) {
        String titleFilter = blankToNull(title);
        String authorFilter = blankToNull(author);
        String genreFilter = blankToNull(genre);
        if (titleFilter == null && authorFilter == null && genreFilter == null
                && minPrice == null && maxPrice == null) {
            return bookRepository.findAll();
        }
        return bookRepository.search(titleFilter, authorFilter, genreFilter, minPrice, maxPrice);
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbnIgnoreCase(isbn);
    }

    public Book create(Book book) {
        book.setIsbn(normalizeIsbn(book.getIsbn()));
        assertIsbnAvailable(book.getIsbn(), null);
        return bookRepository.save(book);
    }

    public Optional<Book> update(Long id, Book updated) {
        return bookRepository.findById(id).map(existing -> {
            String isbn = normalizeIsbn(updated.getIsbn());
            assertIsbnAvailable(isbn, id);
            existing.setTitle(updated.getTitle());
            existing.setAuthor(updated.getAuthor());
            existing.setPrice(updated.getPrice());
            existing.setIsbn(isbn);
            existing.setGenre(updated.getGenre());
            existing.setPublishedYear(updated.getPublishedYear());
            return bookRepository.save(existing);
        });
    }

    public Optional<Book> patch(Long id, BookPatchRequest patch) {
        return bookRepository.findById(id).map(existing -> {
            if (patch.getTitle() != null) {
                existing.setTitle(patch.getTitle());
            }
            if (patch.getAuthor() != null) {
                existing.setAuthor(patch.getAuthor());
            }
            if (patch.getPrice() != null) {
                existing.setPrice(patch.getPrice());
            }
            if (patch.getIsbn() != null) {
                String isbn = normalizeIsbn(patch.getIsbn());
                assertIsbnAvailable(isbn, id);
                existing.setIsbn(isbn);
            }
            if (patch.getGenre() != null) {
                existing.setGenre(patch.getGenre());
            }
            if (patch.getPublishedYear() != null) {
                existing.setPublishedYear(patch.getPublishedYear());
            }
            return bookRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    private void assertIsbnAvailable(String isbn, Long currentId) {
        if (isbn == null) {
            return;
        }
        boolean taken = currentId == null
                ? bookRepository.existsByIsbnIgnoreCase(isbn)
                : bookRepository.existsByIsbnIgnoreCaseAndIdNot(isbn, currentId);
        if (taken) {
            throw new DuplicateIsbnException(isbn);
        }
    }

    private static String normalizeIsbn(String isbn) {
        return blankToNull(isbn);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
