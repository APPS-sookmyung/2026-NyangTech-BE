package com.nyangtech.nyangtechbackend.user.dto;

import com.nyangtech.nyangtechbackend.cat.dto.CatConstraints;
import jakarta.validation.constraints.Pattern;

/**
 * 프로필 수정 요청. 바꾸고 싶은 항목만 보내면 되고(생략한 항목은 그대로), 둘 다 생략하면 400이다.
 *
 * @param nickname 새 닉네임 (선택)
 * @param catName  현재 고양이의 새 이름 (선택)
 */
public record ProfileUpdateRequest(
        @Pattern(regexp = UserConstraints.NICKNAME_REGEX, message = UserConstraints.NICKNAME_MESSAGE)
        String nickname,

        @Pattern(regexp = CatConstraints.CAT_NAME_REGEX, message = CatConstraints.CAT_NAME_MESSAGE)
        String catName
) {
}
