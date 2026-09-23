package com.nyangtech.nyangtechbackend.cat.config;

import com.nyangtech.nyangtechbackend.cat.domain.CatType;
import com.nyangtech.nyangtechbackend.cat.repository.CatTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 서버가 켜질 때 고양이 종류 기본 데이터를 채운다. code 기준으로 이미 있으면 건너뛰므로 여러 번 실행해도 안전하다.
 * 종류를 추가하려면 아래 목록에 한 줄만 더하면 된다. (※ 이름/이미지/해금 조건은 임시 값)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatTypeSeeder implements ApplicationRunner {

    private final CatTypeRepository catTypeRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<CatType> seeds = List.of(
                CatType.basic("CHEESE", "치즈냥", imageOf("cheese")),
                CatType.basic("GRAY", "회색냥", imageOf("gray")),
                CatType.rare("TUXEDO", "턱시도냥", "고양이 1마리 졸업하기", 1, imageOf("tuxedo")),
                CatType.rare("SIAMESE", "샴냥", "고양이 2마리 졸업하기", 2, imageOf("siamese")),
                CatType.rare("BLACK", "검은냥", "고양이 3마리 졸업하기", 3, imageOf("black"))
        );

        int added = 0;
        for (CatType seed : seeds) {
            if (!catTypeRepository.existsByCode(seed.getCode())) {
                catTypeRepository.save(seed);
                added++;
            }
        }
        if (added > 0) {
            log.info("고양이 종류 기본 데이터 {}건을 추가했습니다.", added);
        }
    }

    private static String imageOf(String name) {
        return "/images/cats/" + name + ".png";
    }
}
