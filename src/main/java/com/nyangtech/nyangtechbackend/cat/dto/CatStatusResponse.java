package com.nyangtech.nyangtechbackend.cat.dto;

/**
 * 현재 고양이의 성장 상태.
 * 명세의 {level, affection, nextStepMarker, isGraduated} 에 catId, canGraduate 를 더했다. (졸업 요청에 catId 가 필요하기 때문)
 *
 * @param nextStepMarker 다음 레벨이 시작되는 호감도. 최고 레벨이면 null. (※ 의미는 임시 해석, 프론트와 확인 필요)
 */
public record CatStatusResponse(
        Long catId,
        int level,
        int affection,
        Integer nextStepMarker,
        boolean isGraduated,
        boolean canGraduate
) {
}
