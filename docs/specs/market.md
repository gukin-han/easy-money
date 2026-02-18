# Market 도메인 스펙 (시장 반응)

## 역할
"시장은 어떻게 반응했는가?"

## 책임
- KIS Open API를 통한 주가 조회
- 공시 시점 전후 가격 비교 (전일 종가 vs 당일 종가)
- 등락률 계산 및 MarketReaction 저장
- 장중 실시간 주가 수집

## 핵심 객체

### StockPrice
- 종목별 일별 주가 데이터 (시가, 고가, 저가, 종가, 거래량)
- unique constraint: (stockCode, tradingDate)
- 가격 필드는 `long` (한국 주가 범위 고려)

### MarketReaction
- 공시 전후 시장 반응 데이터
- priorClose(전일 종가), currentClose(당일 종가), changeRate(등락률)
- `calculateChangeRate()`: BigDecimal(scale=4) 정밀도

## 이벤트 플로우
```
Disclosure 수집 → NewDisclosureEvent(+stockCode, disclosureDate)
  → (팬아웃) MarketEventListener [@Async] → 시장 반응 추적 → MarketReaction 저장
  → (팬아웃) AnalysisEventListener [@Async] → LLM 분석 (별도 독립 수행)
```
- Analysis와 Market은 동일한 NewDisclosureEvent를 동시에 수신 (팬아웃)
- 서로 독립적으로 동작 → 분석 실패가 시장 반응 추적에 영향 없음

## 인바운드
- `MarketEventListener` — NewDisclosureEvent 수신 → 시장 반응 추적
- `StockPriceScheduler` — 장중(09:00~15:30 KST) 주가 수집
- `MarketController` — GET /api/market-reactions

## 아웃바운드
- `StockClient` (포트) → `KisStockClient` / `NoOpStockClient`
  - 현재가: GET /uapi/domestic-stock/v1/quotations/inquire-price
  - 일봉: GET /uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice

## 레이어 구조
```
interfaces/
  web/MarketController
  event/MarketEventListener
  scheduler/StockPriceScheduler
application/
  service/MarketReactionService, StockPriceService
  dto/MarketReactionInfo
domain/
  model/StockPrice, MarketReaction
  repository/StockClient, StockPriceRepository, MarketReactionRepository
infrastructure/
  api/KisTokenManager, KisStockClient, NoOpStockClient
  persistence/JpaStockPriceRepository, StockPriceRepositoryImpl, ...
```

## 설정
```properties
kis.api.base-url=https://openapivts.koreainvestment.com:29443
kis.api.appkey=${KIS_APP_KEY:}
kis.api.appsecret=${KIS_APP_SECRET:}
kis.polling.interval=300000
```

## NoOp 모드
KIS API 키 미설정 시 `NoOpStockClient`가 활성화되어 warn 로그만 출력하고 빈 결과 반환.
