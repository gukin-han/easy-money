# easymoney

> DART 전자공시 수집 및 LLM 기반 감성 분석 자동화 서비스

매일 쏟아지는 주식 공시를 사람이 일일이 읽지 않아도 되도록, DART API에서 공시를 수집하고 LLM으로 주가 영향도를 분석한다.

## 기술 아티클

- [HikariCP 커넥션 고갈 추적기](https://gukin-han.tistory.com/67) — Virtual Thread 700개가 풀 10개에 막힌 문제. HikariCP 소스 분석으로 원인을 파악하고 트랜잭션 경계 분리로 해결.
- [Virtual Thread 도입](https://gukin-han.tistory.com/55) — Virtual Thread 도입 배경과 구조 변경 과정.

## 기술 스택

- **Backend**: Java 21, Spring Boot, JPA, Virtual Thread
- **DB**: MySQL, HikariCP
- **외부 연동**: DART Open API, OpenAI API

## 로컬 실행

```bash
docker compose up -d
./gradlew bootRun
```
