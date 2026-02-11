# Disclosure 도메인 스펙 (공시 수집)

## 역할
"무엇이 발표되었는가?"

## 책임
- DART에서 공시 목록 수집
- 관심 종목 기준 필터링
- 원문 데이터 정제
- 공시 본문 텍스트 추출 (ZIP → XML → 텍스트)
- 새 공시 수집 시 이벤트 발행

## 핵심 객체
- **Disclosure** — 공시 정보
- **Corporate** — 기업 정보

## 패키지 구조
```
disclosure/
├── domain/
│   ├── model/          Disclosure, Corporate
│   └── repository/     DisclosureRepository (포트), DartClient (포트)
├── application/
│   ├── service/        DisclosureService, DisclosureCollectionService
│   └── dto/            DisclosureInfo
├── infrastructure/
│   ├── persistence/    JpaDisclosureRepository, DisclosureRepositoryImpl
│   └── api/            DartApiClient, dto/
└── interfaces/
    ├── web/            DisclosureController
    └── scheduler/      DartPollingScheduler (@Scheduled, fixedDelay)
```

## 인바운드
- `DartPollingScheduler` — 주기적 공시 수집 실행 (60초 간격, fixedDelay)
- `DisclosureController` — 공시 조회/수집 API

## 아웃바운드
- `DartApiClient` — DART Open API 연동
  - `fetchRecentDisclosures()` — 공시 목록 조회 (`/list.json`)
  - `fetchDocumentContent(receiptNumber)` — 본문 조회 (`/document.xml` → ZIP 해제 → XML 태그 제거 → 텍스트 추출, 최대 10,000자)
- `JpaDisclosureRepository` — 공시 데이터 영속화
- `ApplicationEventPublisher` — `NewDisclosureEvent` 발행 (트랜잭션 내)

## 스케줄러 흐름
```
DartPollingScheduler (@Scheduled fixedDelay=60s)
  → DisclosureCollectionService.collect()
    → DART API에서 최근 공시 조회
    → 중복 필터링 → 새 공시 저장
    → NewDisclosureEvent 발행 (각 새 공시별)
  → 트랜잭션 커밋 후 → AnalysisEventListener가 이벤트 수신
```

## API
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/disclosures` | 전체 공시 조회 |
| POST | `/api/disclosures/collect` | 수동 공시 수집 |
