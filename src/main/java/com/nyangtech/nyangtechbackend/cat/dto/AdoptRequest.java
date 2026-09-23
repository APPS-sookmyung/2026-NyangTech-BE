package com.nyangtech.nyangtechbackend.cat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** 졸업 후 새 고양이를 맞이하는 요청. (※ 명세에 없는, 졸업 흐름을 완성하기 위해 추가한 API) */
public record AdoptRequest(
        @NotNull
        Long catTypeId,

        @NotNull
        @Pattern(regexp = CatConstraints.CAT_NAME_REGEX, message = CatConstraints.CAT_NAME_MESSAGE)
        String catName
) {
}
