package org.mrshoffen.tasktracker.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-profile-ws")
public interface UserProfileClient {

    @GetMapping("/internal/users/hashed-password")
    String userHashedPassword(@RequestParam("email") String email);

    @GetMapping("/internal/users/id")
    String userId(@RequestParam("email") String email);


    @GetMapping("/internal/users/user-exists")
    Boolean userExists(@RequestParam("email") String email);

    @GetMapping("/internal/users/email")
    String email(@RequestParam("id") UUID userId);
}
