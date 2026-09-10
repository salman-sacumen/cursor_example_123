package com.example.crud.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class BookApiWireMockTest {

    private static final String BOOK_JSON = """
            {"id":1,"title":"Clean Code","author":"Robert Martin","price":39.99}""";
    private static final String CREATE_BODY = """
            {"title":"Clean Code","author":"Robert Martin","price":39.99}""";
    private static final String UPDATE_BODY = """
            {"title":"Clean Code","author":"Robert C. Martin","price":42.00}""";
    private static final String UPDATED_BOOK_JSON = """
            {"id":1,"title":"Clean Code","author":"Robert C. Martin","price":42.00}""";

    private HttpClient httpClient;
    private String baseUrl;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        httpClient = HttpClient.newHttpClient();
        baseUrl = wmRuntimeInfo.getHttpBaseUrl();
    }

    @Test
    void getAllBooks_returnsOk() throws Exception {
        stubFor(get(urlEqualTo("/api/books"))
                .willReturn(jsonResponse(200, "[" + BOOK_JSON + "]")));

        HttpResponse<String> response = send(getRequest("/api/books"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Clean Code", "Robert Martin");
        verify(getRequestedFor(urlEqualTo("/api/books")));
    }

    @Test
    void getBookById_whenExists_returnsOk() throws Exception {
        stubFor(get(urlEqualTo("/api/books/1"))
                .willReturn(jsonResponse(200, BOOK_JSON)));

        HttpResponse<String> response = send(getRequest("/api/books/1"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":1", "Clean Code");
        verify(getRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void getBookById_whenMissing_returnsNotFound() throws Exception {
        stubFor(get(urlEqualTo("/api/books/99"))
                .willReturn(aResponse().withStatus(404)));

        HttpResponse<String> response = send(getRequest("/api/books/99"));

        assertThat(response.statusCode()).isEqualTo(404);
        verify(getRequestedFor(urlEqualTo("/api/books/99")));
    }

    @Test
    void createBook_returnsCreated() throws Exception {
        stubFor(post(urlEqualTo("/api/books"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(CREATE_BODY))
                .willReturn(jsonResponse(201, BOOK_JSON)));

        HttpResponse<String> response = send(jsonRequest("POST", "/api/books", CREATE_BODY));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"id\":1", "Clean Code");
        verify(postRequestedFor(urlEqualTo("/api/books"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(CREATE_BODY)));
    }

    @Test
    void createBook_withInvalidBody_returnsBadRequest() throws Exception {
        String invalidBody = """
                {"title":"","author":"","price":-5.0}""";
        stubFor(post(urlEqualTo("/api/books"))
                .withRequestBody(equalToJson(invalidBody))
                .willReturn(aResponse().withStatus(400)));

        HttpResponse<String> response = send(jsonRequest("POST", "/api/books", invalidBody));

        assertThat(response.statusCode()).isEqualTo(400);
        verify(postRequestedFor(urlEqualTo("/api/books"))
                .withRequestBody(equalToJson(invalidBody)));
    }

    @Test
    void updateBook_whenExists_returnsOk() throws Exception {
        stubFor(put(urlEqualTo("/api/books/1"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(UPDATE_BODY))
                .willReturn(jsonResponse(200, UPDATED_BOOK_JSON)));

        HttpResponse<String> response = send(jsonRequest("PUT", "/api/books/1", UPDATE_BODY));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Robert C. Martin", "42.0");
        verify(putRequestedFor(urlEqualTo("/api/books/1"))
                .withRequestBody(equalToJson(UPDATE_BODY)));
    }

    @Test
    void updateBook_whenMissing_returnsNotFound() throws Exception {
        String missingUpdate = """
                {"title":"X","author":"Y","price":10.0}""";
        stubFor(put(urlEqualTo("/api/books/99"))
                .withRequestBody(equalToJson(missingUpdate))
                .willReturn(aResponse().withStatus(404)));

        HttpResponse<String> response = send(jsonRequest("PUT", "/api/books/99", missingUpdate));

        assertThat(response.statusCode()).isEqualTo(404);
        verify(putRequestedFor(urlEqualTo("/api/books/99")));
    }

    @Test
    void searchBooks_byAuthor_returnsMatches() throws Exception {
        stubFor(get(urlEqualTo("/api/books?author=Martin"))
                .willReturn(jsonResponse(200, "[" + BOOK_JSON + "]")));

        HttpResponse<String> response = send(getRequest("/api/books?author=Martin"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Robert Martin");
        verify(getRequestedFor(urlEqualTo("/api/books?author=Martin")));
    }

    @Test
    void getBookByIsbn_whenExists_returnsOk() throws Exception {
        stubFor(get(urlEqualTo("/api/books/isbn/9780132350884"))
                .willReturn(jsonResponse(200, BOOK_JSON)));

        HttpResponse<String> response = send(getRequest("/api/books/isbn/9780132350884"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Clean Code");
        verify(getRequestedFor(urlEqualTo("/api/books/isbn/9780132350884")));
    }

    @Test
    void patchBook_whenExists_returnsOk() throws Exception {
        String patchBody = """
                {"price":29.99}""";
        stubFor(patch(urlEqualTo("/api/books/1"))
                .withRequestBody(equalToJson(patchBody))
                .willReturn(jsonResponse(200, """
                        {"id":1,"title":"Clean Code","author":"Robert Martin","price":29.99}""")));

        HttpResponse<String> response = send(jsonRequest("PATCH", "/api/books/1", patchBody));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("29.99");
    }

    @Test
    void createBook_withDuplicateIsbn_returnsConflict() throws Exception {
        stubFor(post(urlEqualTo("/api/books"))
                .withRequestBody(equalToJson(CREATE_BODY))
                .willReturn(jsonResponse(409, """
                        {"message":"A book with ISBN 9780132350884 already exists"}""")));

        HttpResponse<String> response = send(jsonRequest("POST", "/api/books", CREATE_BODY));

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.body()).contains("already exists");
    }

    @Test
    void deleteBook_whenExists_returnsNoContent() throws Exception {
        stubFor(delete(urlEqualTo("/api/books/1"))
                .willReturn(aResponse().withStatus(204)));

        HttpResponse<String> response = send(deleteRequest("/api/books/1"));

        assertThat(response.statusCode()).isEqualTo(204);
        verify(deleteRequestedFor(urlEqualTo("/api/books/1")));
    }

    @Test
    void deleteBook_whenMissing_returnsNotFound() throws Exception {
        stubFor(delete(urlEqualTo("/api/books/99"))
                .willReturn(aResponse().withStatus(404)));

        HttpResponse<String> response = send(deleteRequest("/api/books/99"));

        assertThat(response.statusCode()).isEqualTo(404);
        verify(deleteRequestedFor(urlEqualTo("/api/books/99")));
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

    private static ResponseDefinitionBuilder jsonResponse(int status, String body) {
        return aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }
}
