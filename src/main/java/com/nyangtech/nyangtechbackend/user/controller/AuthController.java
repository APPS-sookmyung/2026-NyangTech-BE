package com.nyangtech.nyangtechbackend.user.controller;

import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import com.nyangtech.nyangtechbackend.user.dto.JoinRequest;
import com.nyangtech.nyangtechbackend.user.dto.JoinResponse;
import com.nyangtech.nyangtechbackend.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 회원가입/로그인 통합. 이미 가입된 이메일이면 로그인, 아니면 신규 가입한다. */
    @PostMapping("/join")
    public ApiResponse<JoinResponse> join(@Valid @RequestBody JoinRequest request) {
        return ApiResponse.ok(authService.join(request));
    }
}
