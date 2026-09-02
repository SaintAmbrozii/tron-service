package com.example.userservice.controller;

import com.example.userservice.domain.User;
import com.example.userservice.dto.RegisterDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    public static final String USER_ID_HEADER_NAME = "X-USER-ID";

    private final UserService userService;

    @PostMapping("register")
    public User register(@RequestBody RegisterDto dto, @RequestHeader(name = USER_ID_HEADER_NAME) String userId) {

        log.info("Received POST request to register user. UserId {}, request {}",
                userId, dto);
        return userService.save(dto,userId);
    }

    @GetMapping("profile")
    public Optional<UserDto> getProfile(@RequestHeader(name = USER_ID_HEADER_NAME) String userId){
        log.info("User Get request to profile. UserId {}",
                userId);
        return userService.findById(userId);
    }

    @PatchMapping("update")
    public UserDto update(@RequestHeader(name = USER_ID_HEADER_NAME) String userId,
                                 @RequestBody UserUpdateRequest request){
        log.info("User Update request to profile. UserId {}",
                userId);
        return userService.update(userId, request);

    }

}



