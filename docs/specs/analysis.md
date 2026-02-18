# Analysis 도메인 스펙 (LLM 분석)

## 역할
"이것은 호재인가 악재인가?"

## 책임
- 수집된 공시 텍스트를 LLM에게 질의
- 감성 분석 (POSITIVE / NEUTRAL / NEGATIVE)
- 정량 평가 (점수화: -100 ~ +100)
- 분석 결과 영속화

## 핵심 객체
- **AnalysisReport** — 분석 결과 JPA 엔티티 (disclosureId, sentiment, score, summary, analyzedAt)
- **Sentiment** — 감성 상태 enum (POSITIVE, NEUTRAL, NEGATIVE)
- **AnalysisResult** — LLM 응답 매핑 record (sentiment, score, summary)

## 패키지 구조
```
analysis/
├── domain/
│   ├── model/          Sentiment, AnalysisReport, AnalysisResult
│   └── repository/     AnalysisReportRepository (포트), LlmClient (포트)
├── application/
│   ├── service/        AnalysisService
│   └── dto/            AnalysisReportInfo
├── infrastructure/
│   ├── persistence/    JpaAnalysisReportRepository, AnalysisReportRepositoryImpl
│   └── llm/            SpringAiLlmClient (ChatClient 사용)
└── interfaces/
    ├── event/          AnalysisEventListener (@TransactionalEventListener)
    └── web/            AnalysisController (GET /api/analyses)
```

## 인바운드
- `AnalysisEventListener` — `NewDisclosureEvent` 수신 → 본문 조회 + 분석 트리거
  - `@Async` + `@TransactionalEventListener(AFTER_COMMIT)`
  - 실패 시 예외를 catch하고 로그 기록 → 상태는 `PENDING_ANALYSIS`로 유지
- `AnalysisController` — `GET /api/analyses` 분석 결과 조회

## 아웃바운드
- `SpringAiLlmClient` — Spring AI ChatClient를 통한 LLM 호출
- `AnalysisReportRepositoryImpl` — JPA를 통한 분석 결과 영속화

## API
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/analyses` | 전체 분석 결과 조회 |
