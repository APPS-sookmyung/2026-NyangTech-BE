package com.nyangtech.nyangtechbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSettingsRequest {

    private Boolean isRemindOn;

    private String remindTime;

    private Boolean isOverBudgetOn;
}