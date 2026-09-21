package com.nyangtech.nyangtechbackend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeResponse {

    private String nickname;
    private Integer coin;

    private String catName;
    private Integer level;
    private Integer affection;
}