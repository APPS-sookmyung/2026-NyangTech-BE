package com.nyangtech.nyangtechbackend.cat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nyangtech.nyangtechbackend.cat.domain.Cat;
import com.nyangtech.nyangtechbackend.cat.domain.CatCollection;
import com.nyangtech.nyangtechbackend.cat.domain.CatType;
import com.nyangtech.nyangtechbackend.cat.domain.UserCatUnlock;
import com.nyangtech.nyangtechbackend.global.config.JpaAuditingConfig;
import com.nyangtech.nyangtechbackend.user.domain.User;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class CatRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired CatTypeRepository catTypeRepository;
    @Autowired CatRepository catRepository;
    @Autowired UserCatUnlockRepository unlockRepository;
    @Autowired CatCollectionRepository collectionRepository;

    User user;
    User other;
    CatType basic;
    CatType rare;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.createLocal("me@example.com", "pw", "내닉네임"));
        other = userRepository.save(User.createLocal("other@example.com", "pw", "남닉네임"));
        basic = catTypeRepository.save(CatType.basic("CHEESE", "치즈냥", "/c.png"));
        rare = catTypeRepository.save(CatType.rare("TUXEDO", "턱시도냥", "1마리 졸업하기", 1, "/t.png"));
    }

    private Cat saveCat(User owner, String name) {
        return catRepository.saveAndFlush(Cat.create(owner, basic, name));
    }

    @Test
    void 테이블이_만들어지고_고양이를_저장_조회할_수_있다() {
        Cat saved = saveCat(user, "나비");

        Cat found = catRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("나비");
        assertThat(found.getLevel()).isEqualTo(1);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void 종류_코드는_중복될_수_없다() {
        assertThatThrownBy(() -> catTypeRepository.saveAndFlush(CatType.basic("CHEESE", "또치즈", "/x.png")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 기본_종류와_희귀_종류를_나누어_조회한다() {
        assertThat(catTypeRepository.findByUnlockGraduationCountIsNullOrderByIdAsc())
                .extracting(CatType::getCode).containsExactly("CHEESE");
        assertThat(catTypeRepository.findByUnlockGraduationCountIsNotNullOrderByIdAsc())
                .extracting(CatType::getCode).containsExactly("TUXEDO");
    }

    @Test
    void 현재_고양이는_가장_최근에_만난_고양이다() {
        assertThat(catRepository.findFirstByUserIdOrderByIdDesc(user.getId())).isEmpty();

        saveCat(user, "첫째");
        Cat second = saveCat(user, "둘째");

        assertThat(catRepository.findFirstByUserIdOrderByIdDesc(user.getId()).orElseThrow().getId())
                .isEqualTo(second.getId());
    }

    @Test
    void 졸업하지_않은_고양이_존재_여부와_조회가_유저별로_구분된다() {
        Cat mine = saveCat(user, "나비");

        assertThat(catRepository.existsByUserId(user.getId())).isTrue();
        assertThat(catRepository.existsByUserId(other.getId())).isFalse();
        assertThat(catRepository.existsByUserIdAndGraduatedFalse(user.getId())).isTrue();
        assertThat(catRepository.findActiveByUserIdForUpdate(user.getId()).orElseThrow().getId())
                .isEqualTo(mine.getId());
        assertThat(catRepository.findActiveByUserIdForUpdate(other.getId())).isEmpty();
    }

    @Test
    void 졸업한_고양이는_활성_조회에서_빠진다() {
        Cat cat = saveCat(user, "나비");
        cat.increaseAffection(700);
        cat.graduate();
        catRepository.saveAndFlush(cat);

        assertThat(catRepository.existsByUserIdAndGraduatedFalse(user.getId())).isFalse();
        assertThat(catRepository.findActiveByUserIdForUpdate(user.getId())).isEmpty();
        assertThat(catRepository.existsByUserId(user.getId())).isTrue();
    }

    @Test
    void 남의_고양이_ID로는_조회되지_않는다() {
        Cat mine = saveCat(user, "나비");

        assertThat(catRepository.findByIdAndUserIdForUpdate(mine.getId(), user.getId())).isPresent();
        assertThat(catRepository.findByIdAndUserIdForUpdate(mine.getId(), other.getId())).isEmpty();
    }

    @Test
    void 해금_기록은_유저와_종류_조합당_하나만_저장된다() {
        unlockRepository.saveAndFlush(UserCatUnlock.unlocked(user, rare));

        assertThatThrownBy(() -> unlockRepository.saveAndFlush(UserCatUnlock.unlocked(user, rare)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 해금된_종류_ID만_조회된다() {
        unlockRepository.saveAndFlush(UserCatUnlock.unlocked(user, rare));

        assertThat(unlockRepository.findUnlockedCatTypeIds(user.getId())).containsExactly(rare.getId());
        assertThat(unlockRepository.findUnlockedCatTypeIds(other.getId())).isEmpty();
        assertThat(unlockRepository.existsByUserIdAndCatTypeIdAndUnlockedTrue(user.getId(), rare.getId())).isTrue();
        assertThat(unlockRepository.existsByUserIdAndCatTypeIdAndUnlockedTrue(other.getId(), rare.getId())).isFalse();
    }

    @Test
    void 졸업_기록_수를_유저별로_센다() {
        collectionRepository.save(CatCollection.of(user, basic, LocalDateTime.now()));
        collectionRepository.save(CatCollection.of(user, basic, LocalDateTime.now()));
        collectionRepository.saveAndFlush(CatCollection.of(other, basic, LocalDateTime.now()));

        assertThat(collectionRepository.countByUserId(user.getId())).isEqualTo(2);
        assertThat(collectionRepository.countByUserId(other.getId())).isEqualTo(1);
    }

    @Test
    void 종류가_없는_유저의_목록은_비어있다() {
        assertThat(List.copyOf(unlockRepository.findUnlockedCatTypeIds(user.getId()))).isEmpty();
    }
}
