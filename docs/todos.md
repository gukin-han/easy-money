# TODO 현황

## 완료

### Disclosure 도메인
- [x] 프로젝트 초기 설정 (Spring Boot 4.0.2, Java 21, H2)
- [x] Disclosure 엔티티 및 도메인 모델 구현
- [x] DART OpenAPI 공시 수집 기능 (`DartApiClient`)
- [x] 공시 본문 추출 (ZIP → XML → 텍스트 파싱)
- [x] 중복 공시 필터링 (`findExistingReceiptNumbers`)
- [x] 공시 분류기 구현 (`TitleBasedDisclosureClassifier`)
- [x] 공시 분류기를 domain 계층으로 이동
- [x] DartPollingScheduler (60초 간격 폴링)
- [x] DisclosureController (`GET /api/disclosures`)

### Analysis 도메인
- [x] AnalysisReport 엔티티 및 도메인 모델 구현
- [x] LLM 분석 파이프라인 (`LlmClient` 포트)
- [x] SpringAiLlmClient (OpenAI 연동, 조건부 빈)
- [x] NoOpLlmClient (API 키 미설정 시 fallback)
- [x] AnalysisEventListener (NewDisclosureEvent → 본문 조회 → LLM 분석)
- [x] AnalysisController (`GET /api/analyses`)

### Market 도메인
- [x] StockPrice / MarketReaction 엔티티 구현
- [x] StockClient 포트 + KisStockClient (KIS Open API 연동)
- [x] NoOpStockClient (API 키 미설정 시 fallback)
- [x] KisTokenManager (OAuth2 토큰 발급/캐싱/자동갱신)
- [x] MarketReactionService (전일 vs 당일 종가 비교 → 등락률 계산)
- [x] StockPriceService (현재가 조회/저장)
- [x] MarketEventListener (NewDisclosureEvent → 주가 추적)
- [x] StockPriceScheduler (장중 5분 간격 폴링)
- [x] MarketController (`GET /api/market-reactions`)

### 이벤트 플로우
- [x] NewDisclosureEvent 설계 (stockCode, disclosureDate 포함)
- [x] 팬아웃 구조: NewDisclosureEvent → Analysis + Market 독립 실행
- [x] AnalysisCompletedEvent 제거 (불필요한 직렬 의존 해소)

### 인프라 / 공통
- [x] RestClientConfig (dartRestClient + kisRestClient)
- [x] GlobalExceptionHandler (DartApiException + KisApiException)
- [x] SchedulingConfig (@EnableScheduling + @EnableAsync)
- [x] Virtual Thread 활성화

### 문서
- [x] 프로젝트 가이드 및 규칙 문서
- [x] 도메인 스펙 (disclosure, analysis, market)
- [x] ADR: 아키텍처, 이벤트 드리븐, HTTP 클라이언트, LLM 클라이언트, 공시 분류기, KIS API 클라이언트

---

## 미완료

### 운영 준비
- [ ] H2 → PostgreSQL 전환 (서버 재시작 시 데이터 유실 방지)
- [ ] API 키 시크릿 관리 (환경변수 → Secrets Manager 등)
- [ ] Docker 컨테이너화 + docker-compose 구성
- [ ] 헬스체크 엔드포인트 (`/actuator/health`)

### 안정성 개선
- [x] AnalysisEventListener 에러 처리 (실패 시 로그 기록, 상태 PENDING_ANALYSIS 유지)
- [ ] 이벤트 처리 실패 시 재시도 메커니즘
- [ ] 공시 본문 파싱 강화
  - CSS/`<style>` 블록 제거 (현재 정규식이 태그만 제거, CSS 텍스트는 잔존)
  - EUC-KR 인코딩 대응
  - `<table>` 구조 의미 보존 (현재 태그 제거 시 셀 값이 공백으로 합쳐짐)
- [ ] ZIP 내 .xml 파일 여러 개인 경우 처리

### 기능 확장
- [ ] 분석 결과 + 시장 반응 통합 리포트 API
- [ ] 등락률 기준 알림 (임계값 초과 시 알림)
- [ ] 종목별 / 기간별 시장 반응 조회 API
- [ ] 공시 카테고리별 통계 API

### 테스트 보강
- [x] 실제 DART + LLM 통합 테스트 (LlmAnalysisIntegrationTest)
- [x] 이벤트 플로우 통합 테스트 (DisclosureAnalysisFlowTest)
- [ ] 실제 DART 공시 XML 샘플 기반 파싱 테스트
- [ ] KisStockClient 통합 테스트 (MockRestServiceServer)
- [ ] MarketController 웹 레이어 테스트
