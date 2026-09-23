package com.nyangtech.nyangtechbackend.user.dto;

/** 수정 후 현재 프로필. 고양이가 아직 없으면 catName 은 null. */
public record ProfileUpdateResponse(String nickname, String catName) {
}
