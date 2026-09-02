package com.example.userservice.controller;

import com.example.userservice.domain.User;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.pageable.UserPage;
import com.example.userservice.dto.pageable.UserSearchCriteria;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> userList(){
        log.info("Get User List request");
        return userService.userList();
    }

    @GetMapping("{id}")
    public Optional<UserDto> findByUserId(@RequestParam(name = "id")String uuid) {
        log.info("User Get request to profile. UserId {}",
                uuid);
        return userService.findById(uuid);
    }

    @GetMapping("list")
    public UserPage page(@RequestParam(value = "page", defaultValue = "0", required = false) int page,
                         @RequestParam(value = "count", defaultValue = "50", required = false) int size,
                         @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
                         @RequestParam(value = "sort", defaultValue = "id", required = false) String sortProperty) {
        log.info("Get User Page request");
        return userService.page(page, size, direction, sortProperty);
    }
    @PostMapping("list/filter")
    public UserPage filter(@RequestBody UserSearchCriteria criteria) {
        log.info("Get User Page request by criteria filter");
        return UserPage.of(userService.findByCriteria(criteria));
    }





}
