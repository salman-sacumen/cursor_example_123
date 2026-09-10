package com.example.crud;

import com.example.crud.model.Book;
import com.example.crud.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void fullCrudFlow() throws Exception {
        Book createRequest = new Book("Clean Code", "Robert Martin", 39.99);

        String createResponse = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Robert Martin"));

        Book updateRequest = new Book("Clean Code", "Robert C. Martin", 42.00);
        mockMvc.perform(put("/api/books/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.price").value(42.00));

        mockMvc.perform(delete("/api/books/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchPatchAndIsbnLookup() throws Exception {
        Book cleanCode = new Book("Clean Code", "Robert Martin", 39.99, "9780132350884", "Programming", 2008);
        Book refactoring = new Book("Refactoring", "Martin Fowler", 49.99, "9780201485677", "Programming", 1999);
        Book dune = new Book("Dune", "Frank Herbert", 18.50, "9780441172719", "Science Fiction", 1965);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cleanCode)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refactoring)))
                .andExpect(status().isCreated());
        String duneResponse = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dune)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long duneId = objectMapper.readTree(duneResponse).get("id").asLong();

        mockMvc.perform(get("/api/books").param("genre", "Programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/books").param("minPrice", "40").param("maxPrice", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Refactoring"));

        mockMvc.perform(get("/api/books/isbn/9780132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));

        mockMvc.perform(patch("/api/books/" + duneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":21.00,\"genre\":\"Sci-Fi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.price").value(21.00))
                .andExpect(jsonPath("$.genre").value("Sci-Fi"));

        Book duplicateIsbn = new Book("Clean Coder", "Robert Martin", 29.99, "9780132350884", "Programming", 2011);
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateIsbn)))
                .andExpect(status().isConflict());
    }
}
