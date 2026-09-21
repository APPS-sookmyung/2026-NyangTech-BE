package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_remind_on")
    private Boolean isRemindOn=false;

    @Column(name = "remind_time")
    private LocalTime remindTime;

    @Column(name = "is_over_budget_on")
    private Boolean isOverBudgetOn = false;
}