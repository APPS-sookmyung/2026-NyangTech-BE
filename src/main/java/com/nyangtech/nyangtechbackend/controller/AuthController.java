package com.nyangtech.nyangtechbackend.controller;

import com.nyangtech.nyangtechbackend.dto.JoinRequest;
import com.nyangtech.nyangtechbackend.dto.JoinResponse;
import com.nyangtech.nyangtechbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    public JoinResponse join(@Valid @RequestBody JoinRequest request) {
        return authService.join(request);
    }
}