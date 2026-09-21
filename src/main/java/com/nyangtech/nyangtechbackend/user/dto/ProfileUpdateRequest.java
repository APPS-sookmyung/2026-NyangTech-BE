package com.nyangtech.nyangtechbackend.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 프로필 수정 요청. 현재는 닉네임만 수정한다.
 * (고양이 이름 catName은 cat 도메인 구현 후 추가 예정)
 */
public record ProfileUpdateRequest(
        @NotNull
        @Pattern(regexp = UserConstraints.NICKNAME_REGEX, message = UserConstraints.NICKNAME_MESSAGE)
        String nickname
) {
}
