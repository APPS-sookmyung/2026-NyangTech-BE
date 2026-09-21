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
| 회원가입/로그인 | `/api/v1/auth/join` | POST | `{provider, email, password, nickname?}` (nickname은 신규 가입 시 필수, 기존 유저 로그인 시 무시) | `{token, isNewUser}` |
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

### 패키지 구조
- 도메인별 패키지 (`user`, `cat`, `shop`, `finance`, `social`, `analysis`) + 공통 `global`
- 도메인 안에서는 `controller / service / repository / domain / dto` 로 분리
- **다른 도메인의 Repository 직접 접근 금지** — 그 도메인의 Service를 통해서만 호출
- 여러 도메인을 조합만 하는 API(홈 화면 등)는 별도 패키지(`home`)에 둔다

### 응답 포맷
- 모든 API는 `ApiResponse<T>`를 **그대로 반환**한다. (`ResponseEntity`로 감싸지 않음)
  - 성공: `{ "success": true, "data": {...}, "error": null }` — HTTP 200
  - 실패: `{ "success": false, "data": null, "error": { "code": "DUPLICATE_NICKNAME", "message": "..." } }` — ErrorCode의 HTTP 상태
- 성공 시 `ApiResponse.ok(data)`, 데이터가 없으면 `ApiResponse.ok()`

### 예외 처리
- 비즈니스 규칙 위반은 `throw new BusinessException(XxxErrorCode.YYY)`
- 에러 코드는 **도메인별 enum**으로 분리 (`UserErrorCode`, `CatErrorCode`, ...). `ErrorCode` 인터페이스 구현. 공통은 `CommonErrorCode`
  - 이유: 한 파일에 두 사람이 동시에 추가하면 git 충돌이 나기 때문
- 예외 변환은 `GlobalExceptionHandler` 한 곳에서만 한다. 컨트롤러에서 try-catch 금지

### Entity / DTO
- Entity는 `BaseEntity`(createdAt, updatedAt)를 상속하고 `@Setter`를 쓰지 않는다. 상태 변경은 의미 있는 메서드로 (`user.useCoin()`)
- Entity를 API 응답으로 직접 반환하지 않는다. DTO는 Java `record`, 이름은 `XxxRequest` / `XxxResponse`
- 입력 검증은 Request DTO에 `@Valid` + Bean Validation 애노테이션
- 테이블명은 복수형 snake_case (`users`, `cats`, `items`) — `user` 는 DB 예약어라 사용 금지

### 계층별 책임
- Controller: 요청 받기/응답 만들기만. 로직 금지
- Service: 비즈니스 로직 + `@Transactional` (조회는 `readOnly = true`)
- 로그인한 유저의 ID는 토큰에서 꺼낸다. 클라이언트가 userId를 보내지 않는다

### 네이밍
- 클래스: `XxxController`, `XxxService`, `XxxRepository`
- 도메인 간 호출용 메서드는 동사로 의도를 드러낸다 (`addCoin`, `increaseAffection`)

### 테스트
- Service 단위 테스트(Mockito), Repository 테스트(`@DataJpaTest`), API 통합 테스트(MockMvc)
- 성공 케이스뿐 아니라 실패 케이스(잘못된 입력, 인증 실패, 중복)도 반드시 포함

### 비밀 정보
- JWT secret, DB 비밀번호 등은 코드/git에 넣지 않고 환경변수 또는 `application-local.yml`(gitignore됨)에 둔다

## 인증 (JWT)
- `POST /api/v1/auth/join` 으로 받은 `token`을 이후 모든 요청 헤더에 `Authorization: Bearer {token}` 으로 보낸다. (`/api/v1/auth/**` 만 인증 없이 호출 가능)
- 토큰이 없거나 위조/만료되면 `401 UNAUTHORIZED` (ApiResponse 형식). 토큰 유효 기간은 7일이며 Refresh Token은 아직 없다.
- 컨트롤러에서 로그인한 유저의 ID가 필요하면 파라미터에 `@LoginUserId Long userId` 를 붙인다. (클라이언트가 userId를 보내지 않는다)
- JWT 비밀키는 `JWT_SECRET` 환경변수(32바이트 이상)로 지정한다. 미설정 시 서버가 켜질 때마다 임시 키를 만들므로 재시작하면 기존 토큰이 무효가 된다.
- 비밀번호 규칙: 공백 없는 영문/숫자/특수문자 8~64자 / 닉네임 규칙: 한글·영문·숫자·밑줄 2~10자, 중복 불가

## 다른 도메인이 호출하는 user 창구 (`UserService`)
- `getUser(userId)` — 유저 조회 (없으면 `USER_NOT_FOUND`)
- `addCoin(userId, amount)` — 코인 지급, 지급 후 잔액 반환 (소비 기록 보상, 간식 선물 등)
- `useCoin(userId, amount)` — 코인 사용, 남은 잔액 반환. 부족하면 `NOT_ENOUGH_COIN` (상점 구매 등)
- 코인은 동시 요청에도 값이 틀어지지 않도록 락으로 보호된다. **`User.addCoin/useCoin` 을 직접 호출하지 말고 반드시 위 Service 메서드를 사용한다.**

## API 수동 테스트
- IntelliJ에서 `http/*.http` 파일을 열고 ▶ 버튼으로 실행한다. (실행 환경은 `local` 선택)
