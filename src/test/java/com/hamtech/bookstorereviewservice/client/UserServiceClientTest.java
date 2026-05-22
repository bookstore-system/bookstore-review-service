package com.hamtech.bookstorereviewservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamtech.bookstorereviewservice.BookstoreReviewServiceApplication;
import com.hamtech.bookstorereviewservice.model.dto.response.ApiResponse;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BookstoreReviewServiceApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.signerKey=unit-test-signer-key"
})
class UserServiceClientTest {

    @MockBean
    ReviewRepository reviewRepository;

    static MockWebServer mockWebServer;

    @Autowired
    UserServiceClient userServiceClient;

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
    void getUserBasicInfo_getsCorrectPath_andReadsResponse() throws Exception {
        UUID userId = UUID.fromString("987e6543-e21b-12d3-a456-426614174111");
        UserBasicInfoResponse body = new UserBasicInfoResponse();
        body.setUserId(userId);
        body.setDisplayName("Nguyễn Văn A");
        body.setAvatarUrl("https://example.com/avatar/user1.png");

        ApiResponse<UserBasicInfoResponse> wrapped = ApiResponse.<UserBasicInfoResponse>builder()
                .code(200)
                .message("OK")
                .result(body)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(wrapped)));

        ApiResponse<UserBasicInfoResponse> res = userServiceClient.getUserBasicInfo(userId);
        assertThat(res).isNotNull();
        assertThat(res.getResult()).isNotNull();
        assertThat(res.getResult().getUserId()).isEqualTo(userId);
        assertThat(res.getResult().getDisplayName()).isEqualTo("Nguyễn Văn A");
        assertThat(res.getResult().getAvatarUrl()).isEqualTo("https://example.com/avatar/user1.png");

        RecordedRequest req = mockWebServer.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/api/v1/users/" + userId + "/basic-info");
    }
}

