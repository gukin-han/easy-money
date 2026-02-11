# HTTP Client 결정 기록

## RestClient + 가상 스레드 조합 채택
- **날짜**: 2026-02-10
- **상태**: 채택

### 컨텍스트
- DART OpenAPI 등 외부 API 호출을 위한 HTTP 클라이언트 선택
- Spring Boot 4.0.2 / Java 21 환경

### 후보 비교

| 라이브러리 | 방식 | 추가 의존성 | 비고 |
|---|---|---|---|
| **RestClient** | 동기 | 없음 (webmvc 포함) | Spring 6.1+ 도입, 현재 권장 |
| **WebClient** | 리액티브 | webflux 필요 | 리액티브 스택 전체 딸려옴 |
| **RestTemplate** | 동기 | 없음 | 유지보수 모드, 신규 비추천 |
| **OpenFeign** | 선언형 | Spring Cloud | 버전 관리 부담 |

### 트렌드
- Java 21 가상 스레드: 동기 코드로 비동기 수준 처리량 확보 가능
- I/O 블로킹 시 캐리어 스레드에서 자동 분리 → 스레드 자원 점유 없음
- Spring이 이 흐름에 맞춰 RestClient 도입 → 가상 스레드 환경에서 리액티브 불필요

### 결정
- HTTP 클라이언트: **RestClient**
- **가상 스레드 활성화** (`spring.threads.virtual.enabled=true`)
- DART API 호출 위주의 단순 I/O 패턴 → 동기 + 가상 스레드가 적합

### 주의: 가상 스레드 Pinning (Java 21)
- `synchronized` 블록 진입 시 가상 스레드가 캐리어 스레드에 고정(pin) → 이점 상실
- 우리 코드: `synchronized` 대신 `ReentrantLock` 사용
- 라이브러리(HikariCP, JDBC 등) 내부 `synchronized`도 영향 가능 → 의존성 업데이트 시 확인
- Java 24(JEP 491)에서 근본 해결 → 버전 업그레이드 시 해소
- 모니터링: `-Djdk.tracePinnedThreads=short`
