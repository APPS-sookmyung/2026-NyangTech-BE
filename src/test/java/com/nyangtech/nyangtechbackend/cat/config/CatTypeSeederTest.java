package com.nyangtech.nyangtechbackend.cat.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.nyangtech.nyangtechbackend.cat.domain.CatType;
import com.nyangtech.nyangtechbackend.cat.repository.CatTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CatTypeSeederTest {

    @Autowired CatTypeRepository catTypeRepository;
    @Autowired CatTypeSeeder catTypeSeeder;

    @Test
    void 서버가_켜지면_기본_종류와_희귀_종류가_채워진다() {
        assertThat(catTypeRepository.findByUnlockGraduationCountIsNullOrderByIdAsc())
                .extracting(CatType::getCode).containsExactly("CHEESE", "GRAY");
        assertThat(catTypeRepository.findByUnlockGraduationCountIsNotNullOrderByIdAsc())
                .extracting(CatType::getCode).containsExactly("TUXEDO", "SIAMESE", "BLACK");
    }

    @Test
    void 희귀_종류는_해금_조건_문구와_필요_졸업_수를_가진다() {
        CatType siamese = catTypeRepository.findByCode("SIAMESE").orElseThrow();

        assertThat(siamese.isRare()).isTrue();
        assertThat(siamese.getUnlockGraduationCount()).isEqualTo(2);
        assertThat(siamese.getUnlockConditionText()).isEqualTo("고양이 2마리 졸업하기");
    }

    @Test
    void 여러_번_실행해도_중복으로_들어가지_않는다() {
        long before = catTypeRepository.count();

        catTypeSeeder.run(null);
        catTypeSeeder.run(null);

        assertThat(catTypeRepository.count()).isEqualTo(before);
    }
}
