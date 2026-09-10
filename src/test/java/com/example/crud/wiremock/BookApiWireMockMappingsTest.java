package com.example.crud.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class BookApiWireMockMappingsTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("wiremock"))
            .build();

    private HttpClient httpClient;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        baseUrl = wireMock.getRuntimeInfo().getHttpBaseUrl();
    }

    @Test
    void getAllBooks_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(getRequest("/api/books"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Clean Code", "Robert Martin");
        wireMock.verify(getRequestedFor(urlEqualTo("/api/books")));
    }

    @Test
    void getBookById_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(getRequest("/api/books/1"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":1");
        wireMock.verify(getRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void getBookById_whenMissing_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(getRequest("/api/books/99"));

        assertThat(response.statusCode()).isEqualTo(404);
        wireMock.verify(getRequestedFor(urlEqualTo("/api/books/99")));
    }

    @Test
    void createBook_usesClasspathMapping() throws Exception {
        String body = """
                {"title":"Clean Code","author":"Robert Martin","price":39.99}""";

        HttpResponse<String> response = send(jsonRequest("POST", "/api/books", body));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"id\":1");
        wireMock.verify(postRequestedFor(urlEqualTo("/api/books")));
    }

    @Test
    void updateBook_usesClasspathMapping() throws Exception {
        String body = """
                {"title":"Clean Code","author":"Robert C. Martin","price":42.00}""";

        HttpResponse<String> response = send(jsonRequest("PUT", "/api/books/1", body));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Robert C. Martin");
        wireMock.verify(putRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void searchBooks_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(getRequest("/api/books?author=Martin"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Robert Martin");
        wireMock.verify(getRequestedFor(urlEqualTo("/api/books?author=Martin")));
    }

    @Test
    void getBookByIsbn_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(getRequest("/api/books/isbn/9780132350884"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("9780132350884");
        wireMock.verify(getRequestedFor(urlEqualTo("/api/books/isbn/9780132350884")));
    }

    @Test
    void patchBook_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(jsonRequest("PATCH", "/api/books/1", """
                {"price":29.99}"""));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("29.99");
        wireMock.verify(patchRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void deleteBook_usesClasspathMapping() throws Exception {
        HttpResponse<String> response = send(deleteRequest("/api/books/1"));

        assertThat(response.statusCode()).isEqualTo(204);
        wireMock.verify(deleteRequestedFor(urlEqualTo("/api/books/1")));
    }

    private HttpRequest getRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
    }

    private HttpRequest deleteRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();
    }

    private HttpRequest jsonRequest(String method, String path, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
