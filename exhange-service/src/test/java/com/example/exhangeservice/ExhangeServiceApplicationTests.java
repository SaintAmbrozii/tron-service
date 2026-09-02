package com.example.exhangeservice;

import com.example.exhangeservice.dto.ResponseUsd;
import com.example.exhangeservice.service.UsdApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExhangeServiceApplicationTests {

    @Autowired
    UsdApiService usdApiService;

    @Test
    void contextLoads() {
    }

    @Test
    void getCourse(){
        ResponseUsd responseUsd = usdApiService.getUsdCourse();
        System.out.println(responseUsd);
    }

}
