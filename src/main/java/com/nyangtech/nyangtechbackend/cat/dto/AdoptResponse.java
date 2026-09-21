package com.nyangtech.nyangtechbackend.cat.dto;

public record AdoptResponse(Long catId, String catName, String catType, int affection) {
}
