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
- `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
- 이벤트: `NewDisclosureEvent(disclosureId, receiptNumber, corporateName, title)`
- `global/event` 패키지에 이벤트 배치 → 도메인 간 공유

### 흐름
```
DisclosureCollectionService.collect()
  → 새 공시 save + NewDisclosureEvent 발행
  → 트랜잭션 커밋
  → AnalysisEventListener.handle()
    → DartClient.fetchDocumentContent() → 본문 텍스트 추출
    → AnalysisService.analyze() → LLM 호출 + AnalysisReport 저장
```

### 특성
- 동기 실행: 분석 완료까지 스케줄러 대기 → `fixedDelay`와 자연스럽게 결합
- 분석 실패 시 공시 저장에 영향 없음 (이미 커밋 완료)
- 향후 비동기 전환: `@Async` 추가 또는 메시지 큐 도입 가능
