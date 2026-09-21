package com.nyangtech.nyangtechbackend.cat.dto;

/** 희귀 고양이 해금 현황. 명세의 {catType, isUnlocked, conditionText} 에 catTypeId 를 더했다. (새 고양이 맞이하기에 필요) */
public record RareCatResponse(Long catTypeId, String catType, boolean isUnlocked, String conditionText) {
}
