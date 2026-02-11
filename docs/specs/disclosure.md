# Disclosure 도메인 스펙 (공시 수집)

## 역할
"무엇이 발표되었는가?"

## 책임
- DART에서 공시 목록 수집
- 관심 종목 기준 필터링
- 원문 데이터 정제

## 핵심 객체
- **Disclosure** — 공시 정보
- **Corporate** — 기업 정보

## 인바운드
- `DartPollingScheduler` — 주기적 공시 수집 실행
- `DisclosureController` — 공시 조회 API

## 아웃바운드
- `OpenDartApiClient` — DART Open API 연동
- `JpaDisclosureRepository` — 공시 데이터 영속화
