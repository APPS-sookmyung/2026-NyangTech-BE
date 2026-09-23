package com.nyangtech.nyangtechbackend.user.dto;

import com.nyangtech.nyangtechbackend.user.domain.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입/로그인 요청.
 * nickname은 신규 가입일 때만 필요하다. (이미 가입된 이메일로 로그인할 때는 무시)
 */
public record JoinRequest(
        @NotNull
        AuthProvider provider,

        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank
        @Pattern(regexp = UserConstraints.PASSWORD_REGEX, message = UserConstraints.PASSWORD_MESSAGE)
        String password,

        @Pattern(regexp = UserConstraints.OPTIONAL_NICKNAME_REGEX, message = UserConstraints.NICKNAME_MESSAGE)
        String nickname
) {

    /** 로그에 요청이 출력되어도 비밀번호가 노출되지 않도록 가린다. */
    @Override
    public String toString() {
        return "JoinRequest[provider=" + provider + ", email=" + email + ", password=****, nickname=" + nickname + "]";
    }
}
