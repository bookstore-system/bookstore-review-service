package com.hamtech.bookstorereviewservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.hamtech.bookstorereviewservice.repository.ReviewRepository;

@SpringBootTest(classes = BookstoreReviewServiceApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.signerKey=unit-test-signer-key"
})
class BookstoreReviewServiceApplicationTests {

    @MockBean
    ReviewRepository reviewRepository;

    @Test
    void contextLoads() {
    }

}
