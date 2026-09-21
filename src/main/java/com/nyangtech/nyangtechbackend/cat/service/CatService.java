package com.nyangtech.nyangtechbackend.cat.service;

import com.nyangtech.nyangtechbackend.cat.domain.Cat;
import com.nyangtech.nyangtechbackend.cat.domain.CatCollection;
import com.nyangtech.nyangtechbackend.cat.domain.CatGrowthPolicy;
import com.nyangtech.nyangtechbackend.cat.domain.CatType;
import com.nyangtech.nyangtechbackend.cat.domain.UserCatUnlock;
import com.nyangtech.nyangtechbackend.cat.dto.AdoptResponse;
import com.nyangtech.nyangtechbackend.cat.dto.CatInitResponse;
import com.nyangtech.nyangtechbackend.cat.dto.CatStatusResponse;
import com.nyangtech.nyangtechbackend.cat.dto.GraduateResponse;
import com.nyangtech.nyangtechbackend.cat.dto.RareCatResponse;
import com.nyangtech.nyangtechbackend.cat.exception.CatErrorCode;
import com.nyangtech.nyangtechbackend.cat.port.MonthlyBudgetRegistrar;
import com.nyangtech.nyangtechbackend.cat.repository.CatCollectionRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatTypeRepository;
import com.nyangtech.nyangtechbackend.cat.repository.UserCatUnlockRepository;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.service.UserService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고양이 관련 기능의 창구. 다른 도메인(축2, home)은 고양이 데이터가 필요하면 Repository가 아니라 이 Service를 호출한다.
 *
 * <p>"유저당 졸업하지 않은 고양이는 1마리"라는 규칙은 고양이를 새로 만드는 곳(initCat, adopt)에서
 * 유저 행에 락을 걸어 동시 요청에서도 지킨다.
 */
@Service
@RequiredArgsConstructor
public class CatService {

    private final UserService userService;
    private final CatRepository catRepository;
    private final CatTypeRepository catTypeRepository;
    private final UserCatUnlockRepository userCatUnlockRepository;
    private final CatCollectionRepository catCollectionRepository;
    private final ObjectProvider<MonthlyBudgetRegistrar> budgetRegistrars;

    /** 온보딩: 첫 고양이를 만든다. 기본 종류 중 첫 번째 종류로 시작한다. 이미 고양이가 있으면 CAT_ALREADY_EXISTS. */
    @Transactional
    public CatInitResponse initCat(Long userId, String catName, long monthlyBudget) {
        User user = userService.getUserForUpdate(userId);
        if (catRepository.existsByUserId(userId)) {
            throw new BusinessException(CatErrorCode.CAT_ALREADY_EXISTS);
        }

        Cat cat = catRepository.save(Cat.create(user, defaultCatType(), catName));
        // 축2 예산 기능이 연결되어 있으면 월 예산을 넘긴다. (같은 트랜잭션이라 실패하면 고양이 생성도 취소)
        budgetRegistrars.orderedStream().forEach(registrar -> registrar.register(userId, monthlyBudget));

        return new CatInitResponse(userId, cat.getId(), cat.getAffection());
    }

    /** 현재 고양이(가장 최근에 만난 고양이)의 성장 상태. 고양이가 없으면 CAT_NOT_FOUND. */
    @Transactional(readOnly = true)
    public CatStatusResponse getStatus(Long userId) {
        Cat cat = currentCat(userId);
        return new CatStatusResponse(
                cat.getId(),
                cat.getLevel(),
                cat.getAffection(),
                CatGrowthPolicy.nextLevelAffection(cat.getLevel()),
                cat.isGraduated(),
                cat.canGraduate());
    }

    /** 희귀 고양이 종류별 해금 여부. */
    @Transactional(readOnly = true)
    public List<RareCatResponse> getRareUnlocks(Long userId) {
        Set<Long> unlockedIds = new HashSet<>(userCatUnlockRepository.findUnlockedCatTypeIds(userId));
        return catTypeRepository.findByUnlockGraduationCountIsNotNullOrderByIdAsc().stream()
                .map(type -> new RareCatResponse(
                        type.getId(), type.getTypeName(), unlockedIds.contains(type.getId()), type.getUnlockConditionText()))
                .toList();
    }

    /**
     * 고양이를 졸업시킨다. 도감에 기록하고, 졸업 수 조건을 채운 희귀 종류를 해금한다.
     * 내 고양이가 아니거나 없는 고양이면 CAT_NOT_FOUND (남의 고양이 존재 여부를 알려주지 않기 위해 구분하지 않는다).
     * 같은 고양이에 대한 동시 요청은 락으로 한 번만 성공한다.
     */
    @Transactional
    public GraduateResponse graduate(Long userId, Long catId) {
        Cat cat = catRepository.findByIdAndUserIdForUpdate(catId, userId)
                .orElseThrow(() -> new BusinessException(CatErrorCode.CAT_NOT_FOUND));

        cat.graduate();
        CatCollection collection = catCollectionRepository.save(
                CatCollection.of(cat.getUser(), cat.getCatType(), LocalDateTime.now()));
        unlockRareTypes(cat.getUser());

        return new GraduateResponse(collection.getId(), CatGrowthPolicy.NEXT_SELECTION_URL);
    }

    /** 졸업 후 새 고양이를 맞이한다. 함께하는 고양이가 있으면 ACTIVE_CAT_EXISTS, 해금 안 된 종류면 CAT_TYPE_LOCKED. */
    @Transactional
    public AdoptResponse adopt(Long userId, Long catTypeId, String catName) {
        User user = userService.getUserForUpdate(userId);
        if (catRepository.existsByUserIdAndGraduatedFalse(userId)) {
            throw new BusinessException(CatErrorCode.ACTIVE_CAT_EXISTS);
        }

        CatType type = catTypeRepository.findById(catTypeId)
                .orElseThrow(() -> new BusinessException(CatErrorCode.CAT_TYPE_NOT_FOUND));
        if (type.isRare() && !userCatUnlockRepository.existsByUserIdAndCatTypeIdAndUnlockedTrue(userId, type.getId())) {
            throw new BusinessException(CatErrorCode.CAT_TYPE_LOCKED);
        }

        Cat cat = catRepository.save(Cat.create(user, type, catName));
        return new AdoptResponse(cat.getId(), cat.getName(), type.getTypeName(), cat.getAffection());
    }

    /**
     * 호감도를 올린다. (다른 도메인에서 호출: 소비 기록 등) 함께하는 고양이가 없으면 아무 일도 하지 않는다.
     * 같은 고양이를 동시에 올려도 값이 유실되지 않는다. amount 는 1 이상이어야 한다.
     */
    @Transactional
    public void increaseAffection(Long userId, int amount) {
        if (amount <= 0) {
            throw new BusinessException(CatErrorCode.INVALID_AFFECTION_AMOUNT);
        }
        catRepository.findActiveByUserIdForUpdate(userId).ifPresent(cat -> cat.increaseAffection(amount));
    }

    /** 현재 고양이의 이름을 바꾼다. 고양이가 없으면 CAT_NOT_FOUND. */
    @Transactional
    public void renameCurrentCat(Long userId, String catName) {
        currentCat(userId).rename(catName);
    }

    /** 현재 고양이의 이름. 고양이가 없으면 빈 값. */
    @Transactional(readOnly = true)
    public Optional<String> findCurrentCatName(Long userId) {
        return catRepository.findFirstByUserIdOrderByIdDesc(userId).map(Cat::getName);
    }

    private Cat currentCat(Long userId) {
        return catRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new BusinessException(CatErrorCode.CAT_NOT_FOUND));
    }

    private CatType defaultCatType() {
        return catTypeRepository.findByUnlockGraduationCountIsNullOrderByIdAsc().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("기본 고양이 종류가 없습니다. CatTypeSeeder 데이터를 확인하세요."));
    }

    /** 졸업한 고양이 수가 해금 조건 이상인 희귀 종류를 해금한다. 이미 해금된 것은 그대로 둔다. */
    private void unlockRareTypes(User user) {
        long graduatedCount = catCollectionRepository.countByUserId(user.getId());
        for (CatType type : catTypeRepository.findByUnlockGraduationCountIsNotNullOrderByIdAsc()) {
            if (type.getUnlockGraduationCount() <= graduatedCount) {
                userCatUnlockRepository.findByUserIdAndCatTypeId(user.getId(), type.getId())
                        .ifPresentOrElse(
                                UserCatUnlock::unlock,
                                () -> userCatUnlockRepository.save(UserCatUnlock.unlocked(user, type)));
            }
        }
    }
}
