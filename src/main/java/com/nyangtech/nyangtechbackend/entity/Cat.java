package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "cat_type_id")
    private Long catTypeId;

    private String name;

    private Integer level=1;

    private Integer affection=0;

    @Column(name = "is_graduated")
    private Boolean isGraduated=false;
}