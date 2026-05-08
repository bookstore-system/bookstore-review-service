package com.hamtech.bookstorereviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${clients.user-service.url:}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}/basic-info")
    UserBasicInfoResponse getUserBasicInfo(@PathVariable("userId") UUID userId);
}

