package com.example.crud.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class BookPatchRequest {

    @Size(min = 1, message = "Title must not be blank")
    private String title;

    @Size(min = 1, message = "Author must not be blank")
    private String author;

    @Positive(message = "Price must be positive")
    private Double price;

    private String isbn;

    private String genre;

    @Min(value = 1000, message = "Published year must be 1000 or later")
    @Max(value = 2100, message = "Published year must be 2100 or earlier")
    private Integer publishedYear;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
    }
}
