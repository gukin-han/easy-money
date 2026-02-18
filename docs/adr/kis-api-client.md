# ADR: KIS Open API 클라이언트 설계

- **날짜**: 2026-02-14
- **상태**: 채택

## 맥락
Market 도메인에서 한국투자증권(KIS) Open API를 연동하여 주가 데이터를 조회해야 한다.
KIS API는 OAuth2 토큰 인증, rate limit(5 req/sec sandbox), 고유 헤더 규격 등의 특성이 있다.

## 결정

### 1. 포트-어댑터 패턴
- `StockClient` 인터페이스를 domain 계층에 정의 (포트)
- `KisStockClient`가 infrastructure 계층에서 구현 (어댑터)
- `NoOpStockClient`가 API 키 미설정 시 fallback 역할

### 2. 조건부 빈 등록
- `KisStockClient`: `@Primary` + `@ConditionalOnProperty(name = "kis.api.appkey")`
- `NoOpStockClient`: `@Component` (기본 빈)
- 기존 `SpringAiLlmClient` / `NoOpLlmClient` 패턴과 동일

### 3. 토큰 관리 (KisTokenManager)
- OAuth2 토큰 발급/캐싱/자동갱신
- 만료 1시간 전 갱신 (double-check locking)
- `ReentrantLock` 사용 (virtual thread에서 synchronized 대신)

### 4. Rate Limit
- 호출 간 200ms sleep (sandbox 제한: 초당 5회)
- `Thread.sleep()` 사용 (virtual thread 환경에서 안전)

### 5. RestClient 빈
- `kisRestClient` 빈을 `RestClientConfig`에 추가
- `@ConditionalOnProperty(name = "kis.api.appkey")`로 조건부 생성
- base-url: `kis.api.base-url` 프로퍼티

### 6. 에러 처리
- `KisApiException` (DartApiException과 동일 패턴)
- `GlobalExceptionHandler`에서 KIS_API_ERROR로 매핑
- 네트워크 에러는 warn 로그 + Optional.empty() 반환

## 대안 검토

### WebClient (리액티브) 대신 RestClient
- 프로젝트 전체가 Spring MVC + virtual thread 기반
- RestClient가 기존 패턴과 일관성 유지
- virtual thread가 블로킹 호출을 효율적으로 처리

### 토큰 갱신에 @Scheduled 대신 lazy refresh
- API 호출 시점에 토큰 유효성 확인 후 필요시 갱신
- 불필요한 토큰 발급 방지
- 서비스 비활성 시간에도 토큰 유지 불필요

## 결과
- KIS API 키 유무에 따라 자동으로 실 연동 / NoOp 전환
- 기존 코드베이스의 패턴(DartClient, LlmClient)과 일관성 유지
- rate limit과 토큰 관리가 인프라 계층에 캡슐화
