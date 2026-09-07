package com.nyangtech.nyangtechbackend.dto;

import lombok.*;

@Getter
@Setter
public class JoinRequest {

    private String provider;
    private String email;
    private String password;
    private String catName;
}