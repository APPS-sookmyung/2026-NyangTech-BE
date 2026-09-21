package com.nyangtech.nyangtechbackend.cat.controller;

import com.nyangtech.nyangtechbackend.cat.dto.AdoptRequest;
import com.nyangtech.nyangtechbackend.cat.dto.AdoptResponse;
import com.nyangtech.nyangtechbackend.cat.dto.CatInitRequest;
import com.nyangtech.nyangtechbackend.cat.dto.CatInitResponse;
import com.nyangtech.nyangtechbackend.cat.dto.CatStatusResponse;
import com.nyangtech.nyangtechbackend.cat.dto.GraduateRequest;
import com.nyangtech.nyangtechbackend.cat.dto.GraduateResponse;
import com.nyangtech.nyangtechbackend.cat.dto.RareCatResponse;
import com.nyangtech.nyangtechbackend.cat.service.CatService;
import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import com.nyangtech.nyangtechbackend.global.security.LoginUserId;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;

    /** 초기 고양이 설정 (온보딩) */
    @PostMapping("/user/cat-init")
    public ApiResponse<CatInitResponse> initCat(@LoginUserId Long userId,
                                                @Valid @RequestBody CatInitRequest request) {
        return ApiResponse.ok(catService.initCat(userId, request.catName(), request.monthlyBudget()));
    }

    /** 호감도/성장 정보 */
    @GetMapping("/cat/status")
    public ApiResponse<CatStatusResponse> getStatus(@LoginUserId Long userId) {
        return ApiResponse.ok(catService.getStatus(userId));
    }

    /** 희귀 고양이 해금 확인 */
    @GetMapping("/cat/rare-unlock")
    public ApiResponse<List<RareCatResponse>> getRareUnlocks(@LoginUserId Long userId) {
        return ApiResponse.ok(catService.getRareUnlocks(userId));
    }

    /** 고양이 졸업 처리 */
    @PostMapping("/cat/graduate")
    public ApiResponse<GraduateResponse> graduate(@LoginUserId Long userId,
                                                  @Valid @RequestBody GraduateRequest request) {
        return ApiResponse.ok(catService.graduate(userId, request.catId()));
    }

    /** 졸업 후 새 고양이 맞이하기 (명세에 없는 추가 API) */
    @PostMapping("/cat/adopt")
    public ApiResponse<AdoptResponse> adopt(@LoginUserId Long userId,
                                            @Valid @RequestBody AdoptRequest request) {
        return ApiResponse.ok(catService.adopt(userId, request.catTypeId(), request.catName()));
    }
}
