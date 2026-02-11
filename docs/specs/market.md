# Market 도메인 스펙 (시장 반응)

## 역할
"시장은 어떻게 반응했는가?"

## 책임
- 주가 조회
- 공시 시점 전후 가격 비교
- 등락률 계산

## 핵심 객체
- **StockPrice** — 주가
- **MarketReaction** — 시장 반응

## 인바운드
- `MarketDataScheduler` — 장중 주가 수집

## 아웃바운드
- `KisApiClient` — 한국투자증권 API 연동
