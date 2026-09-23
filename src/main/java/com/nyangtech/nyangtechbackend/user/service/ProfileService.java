package com.nyangtech.nyangtechbackend.user.service;

import com.nyangtech.nyangtechbackend.cat.service.CatService;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.dto.ProfileUpdateResponse;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로필 수정. 유저(닉네임)와 고양이(이름) 두 도메인에 걸친 작업이라 한 트랜잭션으로 묶는다.
 * 둘 중 하나라도 실패하면 다른 하나의 변경도 함께 취소된다.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;
    private final CatService catService;

    @Transactional
    public ProfileUpdateResponse update(Long userId, String nickname, String catName) {
        if (nickname == null && catName == null) {
            throw new BusinessException(UserErrorCode.NOTHING_TO_UPDATE);
        }

        if (catName != null) {
            catService.renameCurrentCat(userId, catName);
        }
        String currentNickname = nickname != null
                ? userService.changeNickname(userId, nickname)
                : userService.getUser(userId).getNickname();
        String currentCatName = catName != null
                ? catName
                : catService.findCurrentCatName(userId).orElse(null);

        return new ProfileUpdateResponse(currentNickname, currentCatName);
    }
}
