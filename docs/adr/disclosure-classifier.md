# Disclosure Classifier 결정 기록

## 분류기 인터페이스 분리 + 제목 기반 분류 채택
- **날짜**: 2026-02-13
- **상태**: 채택

### 컨텍스트
- 실제 DART 데이터 100건 수집 시 76건(76%)이 OTHER로 분류됨
- `DisclosureCategory.classify()` 정적 메서드에 4개 IGNORABLE + 4개 ANALYZABLE 패턴만 존재
- 분류 로직이 enum에 직접 박혀 있어 교체 불가
- 향후 LLM 기반 분류기 도입 가능성 있음

### 후보 비교

| 방식 | 장점 | 단점 |
|---|---|---|
| **enum 정적 메서드** (기존) | 단순 | 교체 불가, 테스트 어려움 |
| **도메인 인터페이스 + 구현체** | 교체 가능, 테스트 용이 | 클래스 분리 필요 |
| LLM 분류 단독 | 패턴 관리 불필요 | 비용, 지연, 외부 의존 |

### 결정
- `DisclosureClassifier` 인터페이스를 도메인 포트로 정의
- `TitleBasedDisclosureClassifier`를 도메인 계층에 구현 (순수 키워드 매칭, 외부 의존 없음)
- `DisclosureCategory` enum에서 `classify()` 제거, `isAnalyzable()` → `analyzable` 필드로 변경
- `Disclosure.classify()` → `Disclosure.applyCategory(DisclosureCategory)`로 변경

### 계층 배치 근거

| 구현체 | 계층 | 이유 |
|---|---|---|
| `TitleBasedDisclosureClassifier` | domain/service | 순수 Java 문자열 매칭, 외부 의존 없음 |
| 향후 `LlmDisclosureClassifier` | infrastructure | 외부 LLM API 호출 필요 |
| 향후 `CompositeDisclosureClassifier` | infrastructure | 외부 의존 구현체 조합 |

- 동일 인터페이스의 구현체라도 의존성에 따라 다른 계층에 배치
- 원칙: import 대상이 순수 도메인이면 domain, 외부 라이브러리면 infrastructure

### 분류 패턴 설계
- IGNORABLE 13개: 행정/절차 공시 → IGNORED 처리, LLM 분석 불필요
- ANALYZABLE 14개 + OTHER: 투자 판단 영향 → PENDING_ANALYSIS, 이벤트 발행
- 패턴 매칭: `String.contains()` 기반, IGNORABLE 우선 검사
- 겹치는 패턴 처리: 순서로 우선순위 결정 (예: `증권신고서+정정` → AMENDMENT 먼저, 나머지 `증권신고서` → SECURITIES_FILING)

### 향후 확장 계획
- OTHER가 다시 늘어나면 패턴 추가 또는 LLM 분류기 도입
- Composite 패턴: TitleBased로 1차 필터 → OTHER만 LLM에 위임
- TitleBased는 폐기하지 않고 1차 필터로 유지 (비용/속도 최적화)

### 결과
- OTHER 비율: 76% → 0% (100건 기준)
