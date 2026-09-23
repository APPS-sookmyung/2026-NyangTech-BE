package com.nyangtech.nyangtechbackend.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void 성공_응답은_success_true와_data를_담는다() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("hello"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void BusinessException은_ErrorCode의_상태와_코드로_변환된다() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void 검증_실패는_400_INVALID_INPUT이다() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 깨진_JSON은_400_INVALID_INPUT이다() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
    }

    @Test
    void 예상하지_못한_예외는_500이고_내부_메시지를_노출하지_않는다() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value(CommonErrorCode.INTERNAL_ERROR.getMessage()));
    }

    @Test
    void 지원하지_않는_메서드는_500이_아니라_405이다() throws Exception {
        mockMvc.perform(get("/test/valid"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/ok")
        ApiResponse<String> ok() {
            return ApiResponse.ok("hello");
        }

        @GetMapping("/test/business")
        ApiResponse<Void> business() {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        @PostMapping("/test/valid")
        ApiResponse<Void> valid(@Valid @RequestBody TestRequest request) {
            return ApiResponse.ok();
        }

        @GetMapping("/test/boom")
        ApiResponse<Void> boom() {
            throw new IllegalStateException("내부 사정");
        }
    }

    record TestRequest(@NotBlank String name) {
    }
}
