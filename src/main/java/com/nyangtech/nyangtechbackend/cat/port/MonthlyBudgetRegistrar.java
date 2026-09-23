package com.nyangtech.nyangtechbackend.cat.port;

/**
 * 초기 고양이 설정(cat-init) 때 받은 "월 예산"을 저장해줄 곳(축2 예산 도메인)과 연결하는 접점.
 *
 * <p>축2 쪽에서 이 인터페이스를 구현한 @Component 를 만들면 cat-init 때 자동으로 호출된다.
 * 구현체가 하나도 없으면(아직 축2 예산 기능이 없으면) 월 예산은 검증만 하고 저장되지 않는다.
 * 호출은 cat-init 과 같은 트랜잭션 안에서 이루어지므로, 여기서 예외가 나면 고양이 생성도 함께 취소된다.
 */
public interface MonthlyBudgetRegistrar {

    void register(Long userId, long monthlyBudget);
}
