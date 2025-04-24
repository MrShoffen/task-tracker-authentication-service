package org.mrshoffen.tasktracker.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-profile-ws")
public interface UserProfileClient {

    @GetMapping("/users/validate")
    String validateUser(@RequestParam("email") String username, @RequestParam("password") String password);

//    default String getValidateFallback(Sting)
}
