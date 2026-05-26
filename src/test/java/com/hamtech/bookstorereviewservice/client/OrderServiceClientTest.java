package com.hamtech.bookstorereviewservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamtech.bookstorereviewservice.BookstoreReviewServiceApplication;
import com.hamtech.bookstorereviewservice.repository.ReviewRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BookstoreReviewServiceApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.signerKey=unit-test-signer-key"
})
class OrderServiceClientTest {

    @MockBean
    ReviewRepository reviewRepository;

    static MockWebServer mockWebServer;

    @Autowired
    OrderServiceClient orderServiceClient;

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

    @AfterAll
    static void afterAll() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void checkPurchased_sendsQueryParams_andReadsResponse() throws Exception {
        UUID userId = UUID.fromString("987e6543-e21b-12d3-a456-426614174111");
        UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        CheckPurchasedResponse body = new CheckPurchasedResponse();
        body.setPurchased(true);
        body.setMessage("User has successfully purchased and received this book.");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(body)));

        CheckPurchasedResponse res = orderServiceClient.checkPurchased(userId, bookId);
        assertThat(res).isNotNull();
        assertThat(res.isPurchased()).isTrue();
        assertThat(res.getMessage()).contains("successfully purchased");

        RecordedRequest req = mockWebServer.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        String decodedPath = URLDecoder.decode(req.getPath(), StandardCharsets.UTF_8);
        assertThat(decodedPath).startsWith("/api/v1/orders/check-purchased?");
        assertThat(decodedPath).contains("userId=" + userId);
        assertThat(decodedPath).contains("bookId=" + bookId);
    }
}

