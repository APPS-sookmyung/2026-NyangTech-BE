package com.nyangtech.nyangtechbackend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinResponse {

    private String token;
    private Boolean isNewUser;
}