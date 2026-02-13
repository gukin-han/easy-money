# Disclosure 도메인 스펙 (공시 수집)

## 역할
"무엇이 발표되었는가?"

## 책임
- DART에서 공시 목록 수집
- 관심 종목 기준 필터링
- 원문 데이터 정제
- 공시 본문 텍스트 추출 (ZIP → XML → 텍스트)
- 공시 분류 (카테고리/분석 대상 여부 판단)
- 새 공시 수집 시 이벤트 발행 (분석 대상만)

## 핵심 객체
- **Disclosure** — 공시 정보 (상태: NEW → IGNORED | PENDING_ANALYSIS → ANALYZED)
- **DisclosureCategory** — 공시 분류 카테고리 (IGNORABLE / ANALYZABLE)
- **DisclosureStatus** — 공시 처리 상태
- **DisclosureClassifier** — 공시 분류 인터페이스 (도메인 포트)
- **Corporate** — 기업 정보

## 공시 분류 체계

### 분류 구조
- `DisclosureClassifier` 인터페이스 (도메인 계층) → 구현체 교체 가능
- `TitleBasedDisclosureClassifier` (infrastructure) — 제목 키워드 기반 분류
- 향후 API 기반, ML 기반 분류기로 교체 가능

### IGNORABLE (분석 불필요, status → IGNORED)
| 카테고리 | 패턴 | 이유 |
|---|---|---|
| `CORRECTION` | `[기재정정]` | 정정 공시 |
| `ATTACHMENT` | `첨부추가` | 첨부 추가 |
| `AMENDMENT` | `증권신고서` + `정정` | 증권신고서 정정 |
| `SHELL_COMPANY` | `장외회사` | 장외 회사 |
| `SHAREHOLDER_MEETING` | `주주총회소집`, `주주총회집중일` | 절차 공시 |
| `STOCK_OPTION` | `주식매수선택권부여에관한신고` | 행정 신고 |
| `PROXY` | `의결권대리행사권유` | 절차 공시 |
| `SECURITIES_REPORT` | `증권발행실적보고서` | 사후 실적 보고 |
| `PAYMENT_SCHEDULE` | `지급수단별` + `지급기간별` | 보험사 정기 공시 |
| `TRADING_HALT` | `주권매매거래정지` | 거래소 조치 |
| `UNFAITHFUL_DISCLOSURE` | `불성실공시법인지정` | 거래소 제재 |

### ANALYZABLE (분석 대상, status → PENDING_ANALYSIS)
| 카테고리 | 패턴 | 이유 |
|---|---|---|
| `REGULAR_REPORT` | `사업보고서`, `분기보고서`, `반기보고서` | 정기 보고서 |
| `MATERIAL_EVENT` | `주요사항보고` | 주요 사항 |
| `OWNERSHIP_CHANGE` | `지분변동`, `임원ㆍ주요주주`, `주식등의대량보유` | 지분 변동 |
| `TENDER_OFFER` | `공개매수` | 공개 매수 |
| `FINANCIAL_CHANGE` | `매출액또는손익구조`, `파생상품거래손실` | 재무 변동 |
| `DIVIDEND` | `현금ㆍ현물배당결정`, `배당` | 배당 결정 |
| `EARNINGS` | `영업(잠정)실적`, `잠정실적`, `연결재무제표기준영업` | 잠정 실적 |
| `AUDIT_REPORT` | `감사보고서` | 외부 감사 결과 |
| `CONTRACT` | `단일판매ㆍ공급계약`, `계약체결`, `계약해지` | 주요 계약 |
| `LITIGATION` | `소송등의제기`, `소송` | 법적 리스크 |
| `GUARANTEE` | `타인에대한채무보증`, `담보제공`, `채무보증` | 우발 부채 |
| `RELATED_PARTY` | `특수관계인`, `동일인등출자계열회사` | 특수관계인 거래 |
| `MANAGEMENT_ISSUE` | `관리종목지정`, `상장폐지`, `상장적격성` | 경영 위기 |
| `CAPITAL_CHANGE` | `유상증자`, `무상증자`, `자본감소`, `자기주식` | 자본 변동 |
| `OTHER` | 위 패턴에 해당 없음 | 기타 (분석 대상으로 간주) |

### 분류 흐름
```
DisclosureCollectionService.collect()
  → classifier.classify(title) → DisclosureCategory
  → disclosure.applyCategory(category) → status 결정
  → PENDING_ANALYSIS이면 NewDisclosureEvent 발행
```

## 패키지 구조
```
disclosure/
├── domain/
│   ├── model/          Disclosure, DisclosureCategory, DisclosureStatus, Corporate
│   ├── service/        DisclosureClassifier (포트)
│   └── repository/     DisclosureRepository (포트), DartClient (포트)
├── application/
│   ├── service/        DisclosureService, DisclosureCollectionService
│   └── dto/            DisclosureInfo
├── infrastructure/
│   ├── persistence/    JpaDisclosureRepository, DisclosureRepositoryImpl
│   ├── classifier/     TitleBasedDisclosureClassifier
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
    → DisclosureClassifier로 분류
    → 분석 대상(PENDING_ANALYSIS)만 NewDisclosureEvent 발행
  → 트랜잭션 커밋 후 → AnalysisEventListener가 이벤트 수신
```

## API
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/disclosures` | 전체 공시 조회 |
| POST | `/api/disclosures/collect` | 수동 공시 수집 |
