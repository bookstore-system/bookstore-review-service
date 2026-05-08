package com.hamtech.bookstorereviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "book-service", url = "${clients.book-service.url:}")
public interface BookServiceClient {

    @GetMapping("/api/v1/books/{bookId}/exists")
    BookExistsResponse checkBookExists(@PathVariable("bookId") UUID bookId);
}

