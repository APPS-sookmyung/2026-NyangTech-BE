package com.nyangtech.nyangtechbackend.user.domain;

import com.nyangtech.nyangtechbackend.global.common.BaseEntity;
import com.nyangtech.nyangtechbackend.global.exception.BusinessException;
import com.nyangtech.nyangtechbackend.user.exception.UserErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSettings extends BaseEntity {

    public static final LocalTime DEFAULT_REMIND_TIME = LocalTime.of(21, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "is_remind_on", nullable = false)
    private boolean remindOn;

    @Column(name = "remind_time", nullable = false)
    private LocalTime remindTime;

    @Column(name = "is_over_budget_on", nullable = false)
    private boolean overBudgetOn;

    private UserSettings(User user) {
        this.user = user;
        this.remindOn = false;
        this.remindTime = DEFAULT_REMIND_TIME;
        this.overBudgetOn = false;
    }

    /** 가입 직후 만들어지는 기본 설정 (모든 알림 꺼짐, 알림 시간 21:00). */
    public static UserSettings createDefault(User user) {
        return new UserSettings(user);
    }

    /**
     * 알림 설정을 변경한다.
     * 리마인드를 끄는 요청에 remindTime이 없으면 기존 시간을 유지한다. (다시 켤 때 이전 시간을 기억)
     */
    public void updateNoti(boolean remindOn, LocalTime remindTime, boolean overBudgetOn) {
        if (remindOn && remindTime == null) {
            throw new BusinessException(UserErrorCode.REMIND_TIME_REQUIRED);
        }
        this.remindOn = remindOn;
        if (remindTime != null) {
            this.remindTime = remindTime;
        }
        this.overBudgetOn = overBudgetOn;
    }
}
