# 이벤트 기반 도메인 연동 결정 기록

## TransactionalEventListener 채택
- **날짜**: 2026-02-12
- **상태**: 채택

### 컨텍스트
- Disclosure 도메인 → Analysis 도메인 간 연동 필요
- 도메인 간 직접 의존 금지 (아키텍처 원칙)
- 공시 저장 확정 후 분석 시작해야 함 (데이터 정합성)

### 후보 비교

| 방식 | 정합성 | 비동기 | 비고 |
|---|---|---|---|
| **@TransactionalEventListener(AFTER_COMMIT)** | 커밋 후 실행 보장 | 동기 (기본) | Spring 내장, 추가 인프라 불필요 |
| @EventListener | 트랜잭션 내 실행 | 동기 | 분석 실패 시 공시 저장 롤백 위험 |
| 메시지 큐 (Kafka, RabbitMQ) | 독립 보장 | 비동기 | 현재 규모에 과도한 인프라 |

### 결정
- `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` + `@Async`
- 이벤트: `NewDisclosureEvent(disclosureId, receiptNumber, corporateName, title, stockCode, disclosureDate)`
- `global/event` 패키지에 이벤트 배치 → 도메인 간 공유
- 팬아웃 구조: 하나의 이벤트를 여러 리스너가 동시 수신

### 흐름
```
DisclosureCollectionService.collect()
  → Collector: DART API 조회 → 비상장사 제외 → 중복 필터링
  → Processor: 분류 → 저장 → NewDisclosureEvent 발행
  → 트랜잭션 커밋
  → (팬아웃) AnalysisEventListener.handle() [@Async]
    → DartClient.fetchDocumentContent() → 본문 텍스트 추출
    → AnalysisService.analyze() → LLM 호출 + AnalysisReport 저장
  → (팬아웃) MarketEventListener.handle() [@Async]
    → 전일 종가 + 당일 종가 조회 → MarketReaction 저장
```

### 구조 변경 이력
- **초기**: 직렬 구조 (NewDisclosureEvent → Analysis → AnalysisCompletedEvent → Market)
- **현재**: 팬아웃 구조 (NewDisclosureEvent → Analysis, Market 동시 수신)
- 변경 이유: Analysis와 Market은 독립적인 관심사 → 직렬 의존 불필요

### 특성
- 비동기 실행: `@Async`로 리스너가 별도 스레드에서 실행
- 분석/시장 반응 추적이 서로 독립적으로 동작 (하나 실패해도 다른 쪽 영향 없음)
- 분석 실패 시 공시 저장에 영향 없음 (이미 커밋 완료)

### 알려진 이슈: 가상 스레드 + 대량 이벤트 시 커넥션 풀 고갈
- **발견일**: 2026-02-20
- **상태**: 미해결
- **현상**: 오늘자 전체 공시 수집(714건) 시 @Async 이벤트 리스너가 약 1,182개(분석 468 + 시장 714) 동시 실행되며 HikariCP 커넥션 풀(기본 10개)이 고갈되어 380건 분석 실패
- **원인**: `AFTER_COMMIT` + `@Async` 조합에서 각 리스너가 독립 트랜잭션(= 새 커넥션)을 필요로 함. `spring.threads.virtual.enabled=true`로 가상 스레드 사용 중이라 동시 실행 수 제한이 없어 모든 이벤트가 즉시 실행됨
- **후보 해결 방안**:
  1. Semaphore로 동시 분석/추적 수 제한 (e.g. 동시 20개)
  2. 커넥션 풀 크기 증가 (근본 해결은 아님)
  3. 이벤트를 즉시 발행하지 않고 배치로 묶어서 처리
  4. 메시지 큐(Redis, Kafka 등) 도입으로 백프레셔 적용
