// entity/Report.java
package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private int month;
    private float increaseRate;

    public Report(Long userId, int month, float increaseRate) {
        this.userId = userId;
        this.month = month;
        this.increaseRate = increaseRate;
    }
}