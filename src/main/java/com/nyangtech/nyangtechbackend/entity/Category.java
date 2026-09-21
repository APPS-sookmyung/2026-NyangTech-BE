// entity/Category.java
package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String name;

    public Category(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}