// entity/Spending.java
package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "spending")
public class Spending {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long categoryId;
    private int amount;
    private LocalDate date;

    public Spending(Long userId, Long categoryId, int amount, LocalDate date) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
    }

    public void update(Long categoryId, int amount, LocalDate date) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
    }
}