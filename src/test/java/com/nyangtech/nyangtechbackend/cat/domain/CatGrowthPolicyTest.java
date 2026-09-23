package com.nyangtech.nyangtechbackend.cat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CatGrowthPolicyTest {

    @ParameterizedTest(name = "호감도 {0} → 레벨 {1}")
    @CsvSource({
            "0, 1", "1, 1", "99, 1",
            "100, 2", "249, 2",
            "250, 3", "449, 3",
            "450, 4", "699, 4",
            "700, 5", "10000, 5"
    })
    void 호감도에_맞는_레벨이_계산된다(int affection, int expectedLevel) {
        assertThat(CatGrowthPolicy.levelFor(affection)).isEqualTo(expectedLevel);
    }

    @Test
    void 다음_레벨_기준_호감도는_최고_레벨에서만_null이다() {
        assertThat(CatGrowthPolicy.nextLevelAffection(1)).isEqualTo(100);
        assertThat(CatGrowthPolicy.nextLevelAffection(2)).isEqualTo(250);
        assertThat(CatGrowthPolicy.nextLevelAffection(3)).isEqualTo(450);
        assertThat(CatGrowthPolicy.nextLevelAffection(4)).isEqualTo(700);
        assertThat(CatGrowthPolicy.nextLevelAffection(CatGrowthPolicy.MAX_LEVEL)).isNull();
    }

    @Test
    void 최고_레벨이_되어야_졸업할_수_있다() {
        assertThat(CatGrowthPolicy.canGraduate(CatGrowthPolicy.MAX_LEVEL - 1)).isFalse();
        assertThat(CatGrowthPolicy.canGraduate(CatGrowthPolicy.MAX_LEVEL)).isTrue();
    }

    @Test
    void 호감도_상한은_최고_레벨이_시작되는_값이다() {
        assertThat(CatGrowthPolicy.levelFor(CatGrowthPolicy.MAX_AFFECTION)).isEqualTo(CatGrowthPolicy.MAX_LEVEL);
        assertThat(CatGrowthPolicy.levelFor(CatGrowthPolicy.MAX_AFFECTION - 1)).isEqualTo(CatGrowthPolicy.MAX_LEVEL - 1);
    }
}
