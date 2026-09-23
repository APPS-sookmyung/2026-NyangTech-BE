package com.nyangtech.nyangtechbackend.global.security;

import com.nyangtech.nyangtechbackend.global.common.ApiResponse;
import com.nyangtech.nyangtechbackend.global.exception.CommonErrorCode;
import com.nyangtech.nyangtechbackend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * 보안 필터 단계에서 발생한 인증/인가 실패를 다른 API와 같은 ApiResponse 형식으로 응답한다.
 * (필터는 컨트롤러 앞단이라 GlobalExceptionHandler가 잡지 못하기 때문에 따로 필요하다.)
 */
@Component
@RequiredArgsConstructor
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /** 토큰이 없거나, 위조/만료된 경우 → 401 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(response, CommonErrorCode.UNAUTHORIZED);
    }

    /** 로그인은 했지만 권한이 없는 경우 → 403 */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(response, CommonErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode));
    }
}
