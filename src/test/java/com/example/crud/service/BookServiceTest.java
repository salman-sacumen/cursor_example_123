package com.example.crud.service;

import com.example.crud.dto.BookPatchRequest;
import com.example.crud.exception.DuplicateIsbnException;
import com.example.crud.model.Book;
import com.example.crud.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("Clean Code", "Robert Martin", 39.99);
        book.setId(1L);
    }

    @Test
    void findAll_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).findAll();
    }

    @Test
    void search_withoutFilters_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = bookService.search(null, "  ", null, null, null);

        assertThat(result).hasSize(1);
        verify(bookRepository).findAll();
        verify(bookRepository, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    void search_withFilters_delegatesToRepository() {
        when(bookRepository.search("Clean", "Martin", "Programming", 10.0, 50.0)).thenReturn(List.of(book));

        List<Book> result = bookService.search("Clean", "Martin", "Programming", 10.0, 50.0);

        assertThat(result).containsExactly(book);
        verify(bookRepository).search("Clean", "Martin", "Programming", 10.0, 50.0);
    }

    @Test
    void findByIsbn_whenExists_returnsBook() {
        when(bookRepository.findByIsbnIgnoreCase("9780132350884")).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findByIsbn("9780132350884");

        assertThat(result).contains(book);
    }

    @Test
    void findById_whenExists_returnsBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAuthor()).isEqualTo("Robert Martin");
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void create_savesAndReturnsBook() {
        Book toCreate = new Book("Refactoring", "Martin Fowler", 49.99);
        Book saved = new Book("Refactoring", "Martin Fowler", 49.99);
        saved.setId(2L);
        when(bookRepository.save(toCreate)).thenReturn(saved);

        Book result = bookService.create(toCreate);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("Refactoring");
        verify(bookRepository).save(toCreate);
    }

    @Test
    void create_withDuplicateIsbn_throwsConflict() {
        Book toCreate = new Book("Clean Code", "Robert Martin", 39.99, "9780132350884", "Programming", 2008);
        when(bookRepository.existsByIsbnIgnoreCase("9780132350884")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(toCreate))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessageContaining("9780132350884");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_whenExists_updatesFields() {
        Book updated = new Book("Clean Code", "Robert C. Martin", 42.00);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Book> result = bookService.update(1L, updated);

        assertThat(result).isPresent();
        assertThat(result.get().getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(result.get().getPrice()).isEqualTo(42.00);
        verify(bookRepository).save(book);
    }

    @Test
    void update_whenMissing_returnsEmpty() {
        Book updated = new Book("X", "Y", 10.0);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.update(99L, updated);

        assertThat(result).isEmpty();
        verify(bookRepository, never()).save(any());
    }

    @Test
    void patch_whenExists_updatesOnlyProvidedFields() {
        BookPatchRequest patch = new BookPatchRequest();
        patch.setPrice(29.99);
        patch.setGenre("Software");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Book> result = bookService.patch(1L, patch);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Clean Code");
        assertThat(result.get().getPrice()).isEqualTo(29.99);
        assertThat(result.get().getGenre()).isEqualTo("Software");
        verify(bookRepository).save(book);
    }

    @Test
    void patch_whenIsbnTaken_throwsConflict() {
        BookPatchRequest patch = new BookPatchRequest();
        patch.setIsbn("9780201633610");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnIgnoreCaseAndIdNot("9780201633610", 1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.patch(1L, patch))
                .isInstanceOf(DuplicateIsbnException.class);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void delete_whenExists_returnsTrue() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        boolean deleted = bookService.delete(1L);

        assertThat(deleted).isTrue();
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void delete_whenMissing_returnsFalse() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        boolean deleted = bookService.delete(99L);

        assertThat(deleted).isFalse();
        verify(bookRepository, never()).deleteById(any());
    }
}
