package com.nyangtech.nyangtechbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatInitResponse {

    private Long userId;
    private Long catId;
    private Integer affection;
}