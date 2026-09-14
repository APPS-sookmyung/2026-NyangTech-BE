# 냥테크 (NyangTech) — 프로젝트 컨텍스트

고양이 키우기 콘셉트의 개인 재무관리 앱. Spring Boot(Java 25, Gradle) 기반, 도메인별 패키지 구조.

## 담당 영역
- 팀원: 조현진(본인), 박수민 
- 프로젝트는 두 축으로 분리:
  - **축1 (조현진 담당)**: 온보딩·계정 / 홈·고양이 성장 / 상점·커스터마이징
  - 축2 (박수민 담당): 소비 기록·예산 / 분석 리포트 / 소셜·친구

## ERD 요약

### User 도메인
- **user**: id, email, nickname, coin
- **user_settings**: id, user_id(FK→user), is_remind_on, remind_time

### Cat 도메인
- **cat**: id, user_id(FK→user), cat_type_id(FK→cat_type), name, level, affection, is_graduated
- **cat_type**: id, type_name, unlock_condition_text
- **user_cat_unlock**: user_id(FK→user), cat_type_id(FK→cat_type), is_unlocked — 유저별 고양이 종류 해금 여부
- **cat_collection**: id, user_id(FK→user), cat_type_id(FK→cat_type), graduated_at — 졸업(성장 완료)한 고양이 기록

### 가계부(Finance) 도메인
- **category**: id, user_id(FK→user), name — 유저별 커스텀 지출 카테고리
- **spending**: id, user_id(FK→user), category_id(FK→category), amount, date
- **budget**: id, user_id(FK→user), year, month, total_amount
- **budget_category**: id, budget_id(FK→budget), category_id(FK→category), amount — 예산의 카테고리별 배분
- **report**: id, user_id(FK→user), month, increase_rate — 월별 리포트, 증감률

### Social 도메인
- **friendship**: id, user_id(FK→user), friend_id(FK→user), status
- **snack_gift**: id, sender_id(FK→user), receiver_id(FK→user), sent_at — 친구 간 간식 선물

### Shop 도메인
- **item**: id, name, slot, grade, price
- **user_item**: id, user_id(FK→user), item_id(FK→item), is_equipped

## 관계 표기 참고 (DBML)
- `-` : 1:1
- `>` : N:1 (many-to-one, FK가 걸린 쪽이 many)

## API 명세서

### 🟢 축1 — 온보딩 및 계정

| 기능 | Endpoint | Method | Request | Response |
| --- | --- | --- | --- | --- |
| 회원가입/로그인 | `/api/v1/auth/join` | POST | `{provider, email, password}` | `{token, isNewUser}` |
| 초기 고양이 설정 | `/api/v1/user/cat-init` | POST | `{catName, monthlyBudget}` | `{userId, catId, affection}` |
| 알림 설정 변경 | `/api/v1/user/settings/noti` | PATCH | `{isRemindOn, remindTime, isOverBudgetOn}` | `{settingsId}` |
| 프로필 설정 수정 | `/api/v1/user/profile` | PATCH | `{nickname, catName}` | `{nickname, catName}` |

### 🟢 축1 — 홈 및 고양이 성장

| 기능 | Endpoint | Method | Request | Response |
| --- | --- | --- | --- | --- |
| 희귀 고양이 해금 확인 | `/api/v1/cat/rare-unlock` | GET | - | `List<{catType, isUnlocked, conditionText}>` |
| 고양이 졸업 처리 | `/api/v1/cat/graduate` | POST | `{catId}` | `{collectionId, nextSelectionUrl}` |
| 호감도/성장 정보 | `/api/v1/cat/status` | GET | - | `{level, affection, nextStepMarker, isGraduated}` |
| 아이템 장착 변경 | `/api/v1/cat/appearance` | PATCH | `List<itemId>` | `{catImageWithLayers}` |
| 홈 화면 정보 조회 | `/api/v1/home` | GET | - | `{catImage, equippedItems[], affection, currentRate, diaryMessage}` |

### 🟢 축1 — 상점 및 커스터마이징

| 기능 | Endpoint | Method | Request | Response |
| --- | --- | --- | --- | --- |
| 보유 아이템 목록 | `/api/v1/user/items` | GET | `category?` | `List<{itemId, name, catImageLayerUrl}>` |
| 아이템 구매 | `/api/v1/shop/purchase/{itemId}` | POST | - | `{remainingCoin, purchaseId}` |
| 아이템 목록 조회 | `/api/v1/shop/items` | GET | `category (모자/옷 등)` | `List<{itemId, name, grade, price, isOwned}>` |

---

### ⚪ 축2 — 소비 기록 및 예산 (담당 외, 참고용)

| 기능 | Endpoint | Method | Request | Response |
| --- | --- | --- | --- | --- |
| 소비 내역 입력 | `/api/v1/spending` | POST | `{amount, category, memo, date, receiptImg?}` | `{spendingId, earnedCoin}` |
| 소비 내역 수정/삭제 | `/api/v1/spending/{id}` | PUT/DELETE | `{amount, category, memo, date}` | `{success: true}` |
| 소비 0원 기록 | `/api/v1/spending/zero` | POST | `{date}` | `{date, isZeroSpend: true}` |
| 월 예산 설정 | `/api/v1/budget` | POST | `{totalAmount}` | `{budgetId, totalAmount}` |
| 카테고리별 예산 설정 | `/api/v1/budget/categories` | PATCH | `List<{category, amount}>` | `List<{categoryId, amount}>` |
| 달력 뷰 조회 | `/api/v1/spending/calendar` | GET | `{year, month}` | `List<{date, totalDailyAmount}>` |

### ⚪ 축2 — 소셜 및 친구 (담당 외, 참고용)

| 기능 | Endpoint | Method | Request | Response |
| --- | --- | --- | --- | --- |
| 월간 절약왕 랭킹 | `/api/v1/friends/ranking` | GET | - | `List<{rank, nickname, savingRate}>` |
| 간식 선물하기 | `/api/v1/friends/{friendId}/snack` | POST | - | `{earnedCoin: 5, nextAvailableTime}` |
| 친구 고양이 방문 | `/api/v1/friends/{friendId}/cat` | GET | - | `{catAppearance, affection, savingRate, isSnackSent}` |
| 친구 추가 (검색) | `/api/v1/friends/add` | POST | `nickname or inviteCode` | `{friendshipId, status: PENDING}` |
| 친구 목록 조회 | `/api/v1/friends` | GET | - | `List<{friendId, nickname, catThumb, savingRate, rank}>` |

### ⚪ 축2 — 분석 리포트 (담당 외, 참고용)

| 기능 | Endpoint | Method | Request | Response |
| --- | --- | --- | --- | --- |
| 월말 리포트 카드 | `/api/v1/analysis/report/{month}` | GET | - | `{increaseRate, topCategories[], catComment, reportImageUrl}` |
| 일별 소<br/>비 막대 그래프 | `/api/v1/analysis/daily` | GET | `{year, month}` | `List<{day, amount, isOverBudget}>` |
| 카테고리별 분석 | `/api/v1/analysis/category` | GET | `type (weekly/monthly)` | `List<{category, amount, ratio}>` |

## 코딩 컨벤션
> TODO: 응답 포맷(ResponseEntity 래핑 방식), 네이밍 규칙, 예외 처리 방식 등 추가 필요
