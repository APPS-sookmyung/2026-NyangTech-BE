// dto/SpendingCreateRequest.java
package com.nyangtech.nyangtechbackend.dto;

import java.time.LocalDate;

public record SpendingCreateRequest(
        int amount,
        String category,
        String memo,
        LocalDate date,
        String receiptImg
) {}