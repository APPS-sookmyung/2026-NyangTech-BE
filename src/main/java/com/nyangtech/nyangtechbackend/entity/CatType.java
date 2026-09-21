package com.nyangtech.nyangtechbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "unlock_condition_text")
    private String unlockConditionText;
}