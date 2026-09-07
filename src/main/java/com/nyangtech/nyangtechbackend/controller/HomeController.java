package com.nyangtech.nyangtechbackend.controller;

import com.nyangtech.nyangtechbackend.dto.HomeResponse;
import com.nyangtech.nyangtechbackend.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public HomeResponse getHome(@RequestParam Long userId) {
        return homeService.getHome(userId);
    }
}