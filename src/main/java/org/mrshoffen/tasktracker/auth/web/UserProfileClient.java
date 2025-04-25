package org.mrshoffen.tasktracker.auth.web;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-profile-ws")
public interface UserProfileClient {

    @GetMapping("/users/id")
    String getUserIdByEmailAndPassword(@RequestParam("email") String username, @RequestParam("password") String password);

}
