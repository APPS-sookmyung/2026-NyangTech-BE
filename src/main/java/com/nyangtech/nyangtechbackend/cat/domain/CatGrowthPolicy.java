package com.nyangtech.nyangtechbackend.cat.domain;

/**
 * 고양이 성장 규칙 (호감도 → 레벨 → 졸업).
 *
 * <p>※ 기획이 확정되지 않아 임시로 정한 값이다. 기획이 정해지면 이 파일의 숫자만 고치면 된다.
 * 다른 곳에서는 규칙을 직접 계산하지 말고 이 클래스를 통해서만 사용한다.
 */
public final class CatGrowthPolicy {

    private CatGrowthPolicy() {
    }

    /** 레벨별 "그 레벨이 시작되는 호감도". 인덱스 0 = 레벨 1. */
    private static final int[] LEVEL_START_AFFECTION = {0, 100, 250, 450, 700};

    public static final int MAX_LEVEL = LEVEL_START_AFFECTION.length;

    /** 호감도 상한. 최고 레벨에 도달하면 더 쌓아도 의미가 없으므로 여기서 멈춘다. */
    public static final int MAX_AFFECTION = LEVEL_START_AFFECTION[MAX_LEVEL - 1];

    /** 졸업 후 프론트가 이동할 "다음 고양이 선택" 화면 경로. (프론트와 확정 필요) */
    public static final String NEXT_SELECTION_URL = "/cat/select";

    /** 호감도에 해당하는 레벨을 계산한다. */
    public static int levelFor(int affection) {
        int level = 1;
        for (int i = 0; i < LEVEL_START_AFFECTION.length; i++) {
            if (affection >= LEVEL_START_AFFECTION[i]) {
                level = i + 1;
            }
        }
        return level;
    }

    /** 다음 레벨이 시작되는 호감도. 이미 최고 레벨이면 null. (API의 nextStepMarker) */
    public static Integer nextLevelAffection(int level) {
        return level >= MAX_LEVEL ? null : LEVEL_START_AFFECTION[level];
    }

    /** 졸업 가능 여부: 최고 레벨에 도달해야 한다. */
    public static boolean canGraduate(int level) {
        return level >= MAX_LEVEL;
    }
}
