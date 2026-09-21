# nyangtech-backend

소비 습관 기반 고양이 키우기(앱) BE 레포지토리

## Tech Stack
- Java 25
- Spring Boot 4.1
- Spring Data JPA
- MySQL

## Team
 - 고양이/성장 라인: 온보딩·계정, 홈·고양이 성장, 상점·커스터마이징 
 - 데이터/소비 라인: 소비 기록·예산, 분석 리포트, 소셜·친구

## Git Convention

### Commit Convention

`타입: 제목` 형식으로 작성합니다.

| 타입 | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 코드 구조 개선 |
| `docs` | 문서 수정 |
| `style` | 코드 포맷팅 (로직 변화 없음) |
| `test` | 테스트 코드 추가/수정 |
| `chore` | 빌드 설정, 패키지 매니저 등 |
| `design` | UI 디자인 변경 |
| `rename` | 파일/폴더명 수정 |
| `remove` | 파일 삭제 |

**규칙**
- 제목은 50자 이내, 끝에 마침표 X
- 제목은 명령형으로 작성 (예: "~구현", "~수정")
- 본문이 필요하면 제목 아래 한 줄 띄우고 작성

### Branch Convention
| 브랜치 | 설명 |
|---|---|
| `main` | 배포 가능한 안정 버전 |
| `develop` | 개발 통합 브랜치 |
| `feature/기능명` | 기능 개발 브랜치 |
| `fix/버그명` | 버그 수정 브랜치 |