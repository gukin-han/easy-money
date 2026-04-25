# Company 도메인 스펙 (회사 마스터)

## 역할
"이 종목코드는 어떤 회사인가? 그 회사는 언제부터 무슨 이름이었나?"

## 책임
- 상장 법인 마스터 데이터 보관 (corp_code, stock_code, 사명, 시장, 상장상태)
- 회사 속성 변경을 **이벤트로 기록**하고, **SCD2 projection**으로 시점별 상태 복원
- 다른 도메인(disclosure, market, 향후 financial)이 회사 정보를 조회할 수 있는 단일 진실 공급원
- DART 고유번호(`corp_code`)와 KRX 종목코드(`stock_code`)의 매핑

## 책임 아님 (의도적 제외)
- Corporate Actions (액면분할·합병·배당) — E4 별도 도메인
- 재무제표·시세 데이터 — E2/E3 별도 도메인
- 다른 도메인의 `stockCode` 문자열 참조를 강제로 FK로 치환 — 아키텍처 규칙(도메인 간 Entity 직접 참조 금지) 유지

## 설계 패턴: Append-only 이벤트 로그 + SCD2 Read Model (CQRS-lite)

```
[외부 변경 감지: DART corpCode.xml 재수집 등]
            │
            ▼
┌──────────────────────────┐       ┌──────────────────────────┐
│  company_change_event    │       │  company_version          │
│  append-only 이벤트 로그  │  ──▶  │  SCD2 projection          │
│  (감사·구독용 원천 기록)  │       │  (시점별 상태 조회)       │
└──────────────────────────┘       └──────────────────────────┘
            │
            ▼ (트랜잭션 커밋 후)
  Spring ApplicationEvent 팬아웃 → 다른 도메인 구독
```

**핵심 원칙**
- 변경은 항상 **이벤트 INSERT → projection 업데이트**를 같은 트랜잭션에서 수행
- 이벤트 로그는 **append-only** (UPDATE·DELETE 금지)
- projection은 언제든 이벤트로부터 재생성 가능하지만, **이벤트가 상태의 단일 진실(Event Sourcing)은 아님** — projection도 정상 경로의 진실. 감사·재구축 용도의 이중 저장.
- 조회는 **projection만** 봄 (이벤트 로그는 읽기 경로에 없음)

## 핵심 객체

### Company (불변 앵커)
회사의 **정체성**만 담음. 이 레코드 자체는 변하지 않음.

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK | 내부 surrogate key |
| `corpCode` | VARCHAR(8) | UNIQUE, NOT NULL | DART 고유번호 — 외부 불변 앵커 |
| `createdAt` | DATETIME | NOT NULL | 시스템 등록 시각 |

속성(이름·종목코드·시장·상장상태)은 여기에 저장하지 않음. 전부 `company_version`에서 조회.

### CompanyChangeEvent (append-only 이벤트 로그)
회사에 **무슨 일이 일어났는지** 기록. 원천 감사 로그.

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK | |
| `companyId` | BIGINT | NOT NULL, index | |
| `eventType` | VARCHAR(40) | NOT NULL | enum (아래 참조) |
| `payload` | JSON | NOT NULL | 이벤트별 상세 데이터 |
| `occurredAt` | DATE | NOT NULL | 현실 세계 발생일 (예: 사명 변경 공시일) |
| `recordedAt` | DATETIME | NOT NULL | 시스템 기록 시각 |
| `source` | VARCHAR(40) | NOT NULL | `DART_CORP_CODE_XML` / `KRX_SYNC` / `MANUAL` 등 |

index: `(companyId, occurredAt)`

**이벤트 타입**

| eventType | payload 예시 | 발생 시점 |
|---|---|---|
| `COMPANY_REGISTERED` | `{name, stockCode, market, listed}` | 시스템 최초 등록 (시드) |
| `COMPANY_RENAMED` | `{previousName, newName}` | 사명 변경 감지 |
| `TICKER_REASSIGNED` | `{previousStockCode, newStockCode}` | 종목코드 변경 (합병·분할 승계 등) |
| `MARKET_TRANSFERRED` | `{previousMarket, newMarket}` | 시장 이전 (KOSDAQ → KOSPI 등) |
| `LISTING_STATUS_CHANGED` | `{previousListed, newListed, reason}` | 상장·상장폐지·재상장 |

payload는 JSON이라 이벤트 타입 추가 시 스키마 마이그레이션 불필요.

### CompanyVersion (SCD2 projection, 읽기 모델)
현재 상태 + 과거 시점 복원을 위한 **조회 최적화 테이블**.

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `id` | BIGINT | PK | |
| `companyId` | BIGINT | NOT NULL | |
| `name` | VARCHAR(100) | NOT NULL | |
| `stockCode` | VARCHAR(6) | NULLABLE | 상장폐지 상태면 직전 값 유지 |
| `market` | VARCHAR(20) | NULLABLE | KOSPI / KOSDAQ / KONEX |
| `listed` | BOOLEAN | NOT NULL | |
| `effectiveFrom` | DATE | NOT NULL | 이 버전이 유효해진 날 |
| `effectiveTo` | DATE | NULLABLE | null = 현재 유효 |
| `isCurrent` | BOOLEAN | NOT NULL | 빠른 현재값 필터용 (`effectiveTo IS NULL`과 동치) |
| `versionNo` | INT | NOT NULL | 회사별 버전 순번 (1, 2, 3, …) |

unique: `(companyId, effectiveFrom)`, `(companyId, versionNo)`
index: `(companyId, isCurrent)`, `(stockCode, effectiveFrom)` — 종목코드 재사용 대응

**조회 예시**
```sql
-- 현재 회사 정보
SELECT * FROM company_version
 WHERE company_id = ? AND is_current = TRUE;

-- 2018-05-10 시점의 회사 정보
SELECT * FROM company_version
 WHERE company_id = ?
   AND effective_from <= '2018-05-10'
   AND (effective_to IS NULL OR effective_to > '2018-05-10');

-- 종목코드 재사용 대응 (123456이 2015년엔 A사, 지금은 B사인 경우)
SELECT * FROM company_version
 WHERE stock_code = '123456'
   AND effective_from <= :targetDate
   AND (effective_to IS NULL OR effective_to > :targetDate);
```

## 쓰기 흐름 (트랜잭션 내)

```
1. 외부 데이터 수집 → 기존 projection과 diff
2. BEGIN TRANSACTION
3. INSERT INTO company_change_event (...)                  ← 원천 기록
4. UPDATE company_version
     SET effective_to = :today, is_current = FALSE
   WHERE company_id = ? AND is_current = TRUE               ← 기존 버전 종료
5. INSERT INTO company_version (..., effective_from=:today,
     effective_to=NULL, is_current=TRUE, version_no = N+1)  ← 새 버전
6. COMMIT
7. ApplicationEventPublisher.publish(CompanyChangedEvent)    ← 다른 도메인 팬아웃
```

시드(최초 등록)도 동일하게 `COMPANY_REGISTERED` 이벤트 + `company_version` 최초 row(`versionNo=1`) INSERT.

## MVP 단계 쪼개기

### Phase 1 (이번 티켓 범위)
- 3개 테이블 전부 생성 (`company`, `company_change_event`, `company_version`)
- DART `corpCode.xml` 1회 시드 → `COMPANY_REGISTERED` 이벤트 + `company_version` row 일괄 생성
- 변경 감지 로직은 **없음** (시드만)
- DoD:
  - [ ] Flyway V2 마이그레이션 SQL
  - [ ] JPA 엔티티 3개
  - [ ] `DartCorpCodeClient` (corpCode.xml 파서)
  - [ ] `CompanySeeder` (시드 배치, 수동 트리거)
  - [ ] 시드 후 검증: `SELECT COUNT(*) FROM company_version WHERE is_current = TRUE` ≈ 2,500

**왜 Phase 1에 이벤트 로그까지 포함?**
나중에 추가하면 **기존 시드 데이터에 대해 `COMPANY_REGISTERED` 이벤트를 소급 생성**해야 함. 처음부터 넣어두면 retrofit 비용 0.

### Phase 2 — 변경 감지
- 주기적 재수집으로 사명·종목코드·시장 변경 감지
- 해당 이벤트 타입 발행 + projection 업데이트
- Spring `ApplicationEventPublisher`로 도메인 이벤트 팬아웃
- DoD:
  - [ ] `Company.renameTo()`, `Company.reassignTicker()` 도메인 메서드
  - [ ] 변경 감지 스케줄러
  - [ ] `CompanyRenamedEvent`, `TickerReassignedEvent` Spring 이벤트

### Phase 3 — 상장상태 추적
- `LISTING_STATUS_CHANGED` 이벤트 (상장폐지·재상장)
- KRX 공시 데이터 연계

### Phase 4 — Corporate Actions (E4로 분리, 이 도메인 밖)
액면분할·합병·배당 등. 이벤트 저장소 성격이 더 강해서 별도 도메인으로 설계.

## 패키지 구조
```
company/
├── domain/
│   ├── model/          Company, CompanyVersion, CompanyChangeEvent, EventType
│   ├── service/        (Phase 2) ChangeDetector
│   └── repository/     CompanyRepository, CompanyVersionRepository,
│                       CompanyChangeEventRepository, CorpCodeClient (포트)
├── application/
│   ├── service/        CompanySeeder, CompanyQueryService
│   │                   (Phase 2) CompanySyncService
│   └── dto/            CompanyInfo
├── infrastructure/
│   ├── persistence/    JPA repositories + Impl
│   └── api/            DartCorpCodeClient (corpCode.xml 파서)
└── interfaces/
    └── web/            CompanyController (GET /api/companies/{stockCode})
```

## 인바운드
- `CompanySeeder` — 수동 트리거 (초기 시드)
- (Phase 2) `CompanySyncScheduler` — 주기 재수집 + 변경 감지
- `CompanyController` — 종목 조회 API

## 아웃바운드
- `DartCorpCodeClient` — DART `GET /api/corpCode.xml`
  - ZIP 응답 → XML 파싱 → `(corp_code, corp_name, stock_code, modify_date)` 리스트
  - `stock_code` 비어있지 않은 것만 = 상장사 필터
- JPA repositories — 영속화
- `ApplicationEventPublisher` — (Phase 2+) 도메인 이벤트 발행 (트랜잭션 커밋 후)

## 식별자 전략

| 식별자 | 특성 | 용도 |
|---|---|---|
| `company.id` | 내부 surrogate BIGINT, 불변 | 도메인 내부 FK (`company_version`, `company_change_event`) |
| `corp_code` | 8자리, DART legal entity 기준, 불변 | 외부 연동 앵커 |
| `stock_code` | 6자리, 재사용됨·변경 가능 | 사용자 노출, 타 도메인의 문자열 참조 |

**원칙**
- 다른 도메인(disclosure, market)은 현재처럼 `stockCode` 문자열로 참조 (아키텍처 규칙 유지)
- Company 조회 API는 `stockCode` 키 → 내부에서 `company_version (stock_code, is_current)` 인덱스로 해소
- 종목코드 재사용 이슈는 `(stock_code, effective_from)` 범위 조회로 대응

## 이 패턴이 이벤트 소싱과 다른 점 (중요)

| | 이벤트 소싱 | 이 도메인의 접근 |
|---|---|---|
| 상태의 단일 진실 | 이벤트 로그 | `company_version` projection |
| projection 손실 시 | 이벤트 재생으로 복원 (필수 계약) | 재복원은 *가능*하지만 계약 아님 |
| 읽기 경로 | 이벤트 replay 또는 projection | projection만 |
| 쓰기 규약 | 이벤트 발행만 | 이벤트 INSERT + projection 업데이트 (같은 트랜잭션) |

**이 도메인은 이벤트 소싱이 아니라 "감사 로그 + CQRS-lite"** 다. projection이 곧 진실이고, 이벤트 로그는 보조.

진짜 이벤트 소싱 스타일은 **E4 CorporateAction**에서 적용 예정 (액면분할·합병은 본질적으로 이벤트이고 과거 주가·EPS adjustment 계산에 replay가 필요).

## 기존 `disclosure.domain.Corporate` 처리
- 현재 이 엔티티는 어디에서도 import되지 않는 dead code
- Phase 1 마이그레이션에서 기존 `corporate` 테이블 DROP
- `disclosure.Disclosure.corporateName` 필드는 유지 (수집 당시 스냅샷)

## 마이그레이션 (Flyway)
```
V2__create_company.sql
  - DROP TABLE IF EXISTS corporate
  - CREATE TABLE company
  - CREATE TABLE company_change_event
  - CREATE TABLE company_version
  - CREATE indexes
```

## 관련 문서
- `docs/rules/architecture.md` — 도메인 구조 규칙
- `docs/adr/` — 이 도메인 도입 관련 ADR (append-only 이벤트 로그 + SCD2 projection 선택 배경) 추가 예정
