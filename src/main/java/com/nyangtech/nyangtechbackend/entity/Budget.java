// entity/Budget.java
package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private int year;
    private int month;
    private int totalAmount;

    public Budget(Long userId, int year, int month, int totalAmount) {
        this.userId = userId;
        this.year = year;
        this.month = month;
        this.totalAmount = totalAmount;
    }

    public void updateTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }
}