package com.nyangtech.nyangtechbackend.service;

import com.nyangtech.nyangtechbackend.dto.HomeResponse;
import com.nyangtech.nyangtechbackend.entity.Cat;
import com.nyangtech.nyangtechbackend.entity.User;
import com.nyangtech.nyangtechbackend.repository.CatRepository;
import com.nyangtech.nyangtechbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserRepository userRepository;
    private final CatRepository catRepository;

    public HomeResponse getHome(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        Cat cat = catRepository.findByUserId(userId)
                .orElseThrow();

        return HomeResponse.builder()
                .nickname(user.getNickname())
                .coin(user.getCoin())
                .catName(cat.getName())
                .level(cat.getLevel())
                .affection(cat.getAffection())
                .build();
    }
}