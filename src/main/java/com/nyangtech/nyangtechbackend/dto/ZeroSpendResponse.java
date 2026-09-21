// dto/ZeroSpendResponse.java
package com.nyangtech.nyangtechbackend.dto;

import java.time.LocalDate;

public record ZeroSpendResponse(LocalDate date, boolean isZeroSpend) {}