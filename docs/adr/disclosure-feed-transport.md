# 공시 피드 통신 방식 결정 기록

## 제목: DART 공시 실시간 피드 — 서버→클라이언트 전달 방식
- **날짜**: 2026-04-25
- **상태**: 제안 (사용자 검토 대기)

### 컨텍스트
- 새 프론트엔드 디자인(`DART Live Styles.html`)이 공시를 *실시간 피드 카드*로 위에서 떨어지듯 흘려보냄
- 디자인 hook(`useDartStream`)이 4.5초 간격으로 새 이벤트를 push받는 모델을 가정 (mock setInterval)
- 현재 백엔드: `DartPollingScheduler`(60초 fixedDelay) → `disclosure` 적재 → `NewDisclosureEvent` 팬아웃. 프론트로의 push 채널 없음.
- 결정 대상: **백엔드 → 프론트엔드** 새 공시 전달 프로토콜

### 후보 비교

| 방식 | 즉시성 | 인프라 비용 | 양방향 | 구현 난이도 | 인증/세션 | 적합도 |
|---|---|---|---|---|---|---|
| **REST polling** | 폴링 주기에 의존 | 0 (기존 인프라) | 불필요 | 매우 낮음 | 표준 | △ |
| **REST long-polling** | 거의 실시간 | 0 | 불필요 | 낮음 | 표준 | △ |
| **SSE (Server-Sent Events)** | 즉시 | 매우 낮음 | 단방향(서버→클라) | 낮음 | HTTP 표준 헤더 | ⭐⭐⭐ |
| **WebSocket** | 즉시 | 중간 | 양방향 | 중간 | 별도 핸드셰이크 | ⭐ |
| **GraphQL Subscription** | 즉시 | 높음 | 단/양방향 | 높음 | 스키마 도입 부담 | ✕ |
| **MQTT / Pub-Sub broker** | 즉시 | 높음 | 양방향 | 높음 | 별도 인프라 | ✕ |

#### 항목별 트레이드오프 상세

**REST polling**
- 장점: 가장 단순, 캐시 가능, 프록시·로드밸런서 호환성 최고, 디버깅 쉬움
- 단점: 폴링 주기 ↔ 부하 트레이드오프. 디자인이 *실시간 카드 push*를 요구하는 톤이라 4~5초 간격 폴링은 어색하고 새 공시 누락 가능
- 비용: 클라이언트 N명 × 폴링 주기 → 서버 자원 선형 증가
- 결론: 디자인 의도와 일치도 낮음

**REST long-polling**
- 장점: 인프라 변경 없음, 거의 실시간
- 단점: 연결 유지 동안 스레드/커넥션 점유. Virtual Thread 환경이라 가능하지만 게이트웨이/프록시 타임아웃 변수 큼
- 비용: 중간
- 결론: SSE보다 굳이 갈 이유 없음 (SSE가 더 표준화된 long-polling 위 추상)

**SSE (Server-Sent Events)** ⭐ 추천
- 장점:
  - **단방향 push**: 디자인 요구(서버→클라)와 정확히 일치
  - **HTTP 위에서 동작**: 기존 인프라(RestClient, GlobalExceptionHandler, 프록시) 그대로 활용
  - **자동 재연결**: 클라이언트가 끊어지면 브라우저가 알아서 재연결 (Last-Event-ID 헤더로 이어받기 가능)
  - **표준 브라우저 API** (`EventSource`) — 추가 의존성 없음
  - Spring MVC `SseEmitter` 또는 Reactive `Flux<ServerSentEvent>` 둘 다 지원
- 단점:
  - 단방향 (구독 채널 변경 등 명령은 별도 REST 엔드포인트 필요 — 보통 문제 안 됨)
  - 일부 모바일 네트워크에서 idle timeout 변수 (heartbeat로 해결)
  - HTTP/1.1에서 도메인당 동시 연결 6개 제한 (HTTP/2면 무관)
- 비용: 매우 낮음
- 결론: **이 도메인의 정답에 가까움**. 단방향·표준·인프라 부담 X.

**WebSocket**
- 장점: 양방향, 메시지 단위 효율 우수
- 단점:
  - 양방향이 *필요할 때* 의미 있음. 공시 피드는 단방향이라 양방향이 잉여 자원
  - 별도 핸드셰이크 → 인증을 HTTP 쿠키/헤더와 어떻게 묶을지 추가 설계
  - 일부 프록시(SSL 종단·CDN)에서 추가 설정 필요
  - Spring WebSocket 인프라 추가 (STOMP 또는 raw)
- 비용: 중간
- 결론: 이후 *주문·실시간 호가·채팅* 등 양방향이 진짜 필요해질 때 검토. 지금은 오버킬.

**GraphQL Subscription**
- 장점: 스키마 기반 타입 안전성, 다른 GraphQL 사용처와 통합 시 시너지
- 단점:
  - 우리 프로젝트에 GraphQL 자체가 없음 → 도입 비용 vs 이득 비대칭
  - 내부적으로 보통 WebSocket 사용 → WebSocket 단점 그대로 + 스키마 부담
- 결론: 부적합

**MQTT / Pub-Sub broker (Redis Pub/Sub, Kafka)**
- 장점: 다중 소비자·고가용성·메시지 보존
- 단점: 별도 인프라, 브라우저 직접 연결 안 됨 (게이트웨이 필요)
- 결론: 백엔드 *내부* 분산은 의미 있을 수 있지만, *프론트엔드까지의 채널*로는 SSE/WS 게이트웨이가 추가로 필요해 복잡도만 증가

### 결정 (제안)

**SSE 채택**

근거:
1. **디자인 의도 정확 일치** — 단방향 서버→클라 push
2. **인프라 비용 최소** — HTTP 위, 추가 의존성 0
3. **운영 단순** — 자동 재연결, 표준 헤더, 디버깅 도구 호환
4. **확장성 충분** — 동시 연결 수천 단위까지 SseEmitter로 처리 가능. Virtual Thread 환경에서 더 유리.
5. **불필요한 양방향성 회피** — 구독 채널 변경 등은 별도 REST 호출로 해결 (RESTful 한 번 더 명확)

미래 양방향 필요 발생 시 (예: 사용자 알림 설정 실시간 동기화) → 그때 WebSocket 도입 재평가. SSE → WebSocket 마이그레이션은 채널이 분리되어 있어 점진적 가능.

### 설계 초안 (참고)

**엔드포인트**
```
GET /api/disclosures/feed/stream
  Accept: text/event-stream
  Last-Event-ID: <rcept_no>   # optional, 재연결 시 이어받기
```

**이벤트 형식**
```
event: disclosure
id: 20260425000123
data: {"id":"20260425000123","corp":{...},"verdict":"positive",...}

event: heartbeat
data: {"ts":"2026-04-25T07:00:00Z"}
```

**서버 구현 방식**
- `NewDisclosureEvent` 팬아웃에 SSE 브로드캐스터 리스너 추가
- 활성 `SseEmitter` 세트 관리 + 이벤트 발생 시 브로드캐스트
- 30초 간격 heartbeat (idle timeout 회피)

**Backpressure / 제한**
- 클라이언트당 최대 동시 연결 수 제한 (e.g. 1)
- 서버 측 최대 동시 SSE 연결 수 제한 + 초과 시 503

### 결과 (예상)

긍정:
- 프론트엔드 `EventSource` 그대로 사용 가능 (mock `useDartStream`을 EventSource로 교체)
- 기존 이벤트 드리븐 구조에 자연스럽게 결합 (`NewDisclosureEvent` → SSE 브로드캐스터)

부정:
- HTTP/1.1 환경에서 도메인당 6 연결 제한 (실사용엔 거의 영향 없음)
- 일부 사내 프록시·CDN이 SSE 청크 buffering 시 지연 (Cloudflare는 정상, 일부 제품은 설정 필요)

### 미결 사항
- 인증 방식 (현재 인증 시스템 미구현 — 인증 도입 시 SSE 연결에 토큰 어떻게 실을지)
- 다중 종목 구독 시나리오 (전체 피드 vs 종목별 필터링): 일단 전체 피드, 클라이언트에서 필터링 → 부하 커지면 서버 측 필터링 도입
- 모바일 환경에서 SSE 안정성 (Android Chrome OK, iOS Safari는 예전엔 이슈 있었으나 최근 해결)

### 관련 문서
- `docs/specs/disclosure.md` — Disclosure 도메인 스펙
- `docs/adr/event-driven.md` — 도메인 이벤트 팬아웃
- `/tmp/dart-live-design/project-x/project/DART Live Styles.html` — 디자인 원본 (포팅 대기)
