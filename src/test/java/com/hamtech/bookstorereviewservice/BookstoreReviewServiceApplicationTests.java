package com.hamtech.bookstorereviewservice;

import com.hamtech.bookstorereviewservice.client.BookServiceClient;
import com.hamtech.bookstorereviewservice.client.OrderServiceClient;
import com.hamtech.bookstorereviewservice.client.UserServiceClient;
import com.hamtech.bookstorereviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = BookstoreReviewServiceApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.signerKey=unit-test-signer-key"
})
class BookstoreReviewServiceApplicationTests {

    @MockBean
    ReviewRepository reviewRepository;

    @MockBean
    BookServiceClient bookServiceClient;

    @MockBean
    OrderServiceClient orderServiceClient;

    @MockBean
    UserServiceClient userServiceClient;

    @Test
    void contextLoads() {
    }

}
