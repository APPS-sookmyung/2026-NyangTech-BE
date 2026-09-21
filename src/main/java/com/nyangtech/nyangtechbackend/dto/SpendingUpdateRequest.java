// dto/SpendingUpdateRequest.java
package com.nyangtech.nyangtechbackend.dto;

import java.time.LocalDate;

public record SpendingUpdateRequest(
        int amount,
        String category,
        String memo,
        LocalDate date
) {}