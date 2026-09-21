package com.nyangtech.nyangtechbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatInitRequest {

    private String catName;
    private Integer monthlyBudget;
}