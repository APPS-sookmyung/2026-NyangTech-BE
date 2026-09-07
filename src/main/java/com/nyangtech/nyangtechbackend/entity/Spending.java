package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "spending")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private LocalDate date;
}