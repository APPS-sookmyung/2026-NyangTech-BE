package com.nyangtech.nyangtechbackend.global.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 컨트롤러 파라미터에 붙이면 "지금 요청한 로그인 유저의 ID"를 토큰에서 꺼내 넣어준다.
 * 클라이언트가 userId를 직접 보내게 하지 않기 위한 장치.
 *
 * <pre>
 * public ApiResponse<X> me(@LoginUserId Long userId) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "T(java.lang.Long).parseLong(subject)")
public @interface LoginUserId {
}
