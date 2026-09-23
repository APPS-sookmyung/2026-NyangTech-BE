package com.nyangtech.nyangtechbackend.support;

import com.nyangtech.nyangtechbackend.cat.repository.CatCollectionRepository;
import com.nyangtech.nyangtechbackend.cat.repository.CatRepository;
import com.nyangtech.nyangtechbackend.cat.repository.UserCatUnlockRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserRepository;
import com.nyangtech.nyangtechbackend.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 롤백되지 않는(@Transactional 이 아닌) 테스트가 실제로 저장한 유저 관련 데이터를 지운다.
 * 다른 데이터를 참조하는 것부터 순서대로 지운다. 고양이 종류(cat_types) 같은 기본 데이터는 남겨둔다.
 */
@Component
@RequiredArgsConstructor
public class TestDataCleaner {

    private final CatCollectionRepository catCollectionRepository;
    private final UserCatUnlockRepository userCatUnlockRepository;
    private final CatRepository catRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    public void clean() {
        catCollectionRepository.deleteAllInBatch();
        userCatUnlockRepository.deleteAllInBatch();
        catRepository.deleteAllInBatch();
        userSettingsRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
