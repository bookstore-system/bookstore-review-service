package com.hamtech.bookstorereviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "order-service", url = "${clients.order-service.url:}")
public interface OrderServiceClient {

    @GetMapping("/api/v1/orders/check-purchased")
    CheckPurchasedResponse checkPurchased(@RequestParam("userId") UUID userId,
                                          @RequestParam("bookId") UUID bookId);
}

