package com.hamtech.bookstorereviewservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamtech.bookstorereviewservice.BookstoreReviewServiceApplication;
import com.hamtech.bookstorereviewservice.repository.ReviewRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BookstoreReviewServiceApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.signerKey=unit-test-signer-key"
})
class BookServiceClientTest {

    @MockBean
    ReviewRepository reviewRepository;

    static MockWebServer mockWebServer;

    @Autowired
    BookServiceClient bookServiceClient;

    @Autowired
    ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) throws Exception {
        if (mockWebServer == null) {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        }
        String baseUrl = mockWebServer.url("/").toString();
        registry.add("clients.book-service.url", () -> baseUrl);
        registry.add("clients.user-service.url", () -> baseUrl);
        registry.add("clients.order-service.url", () -> baseUrl);
    }

    @BeforeAll
    static void beforeAll() {
        // started in DynamicPropertySource
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void checkBookExists_getsCorrectPath_andReadsResponse() throws Exception {
        UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        BookExistsResponse body = new BookExistsResponse();
        body.setExists(true);
        body.setBookId(bookId);
        body.setTitle("Clean Code");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(body)));

        BookExistsResponse res = bookServiceClient.checkBookExists(bookId);
        assertThat(res).isNotNull();
        assertThat(res.isExists()).isTrue();
        assertThat(res.getBookId()).isEqualTo(bookId);
        assertThat(res.getTitle()).isEqualTo("Clean Code");

        RecordedRequest req = mockWebServer.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/api/v1/books/" + bookId + "/exists");
    }
}

