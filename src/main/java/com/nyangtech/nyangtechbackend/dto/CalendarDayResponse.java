package com.nyangtech.nyangtechbackend.dto;

import java.time.LocalDate;

public record CalendarDayResponse(LocalDate date, int totalDailyAmount) {}