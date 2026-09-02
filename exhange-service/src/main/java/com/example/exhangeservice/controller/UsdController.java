package com.example.exhangeservice.controller;

import com.example.exhangeservice.dto.ResponseUsd;
import com.example.exhangeservice.service.UsdApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usd")
public class UsdController {

    private final UsdApiService apiService;

    public UsdController(UsdApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/course")
    public ResponseUsd getCourse() {
       return apiService.getUsdCourse();
    }
}
