# LLM Client 결정 기록

## Spring AI + ChatClient 채택
- **날짜**: 2026-02-12
- **상태**: 채택

### 컨텍스트
- 공시 본문에 대한 감성 분석(호재/악재) 필요
- LLM 프로바이더(OpenAI, Gemini 등) 교체 가능해야 함
- 구조화된 응답(JSON → record) 파싱 필요

### 후보 비교

| 방식 | 추상화 | 프로바이더 교체 | 비고 |
|---|---|---|---|
| **Spring AI ChatClient** | 프레임워크 레벨 | starter만 교체 | `.entity()` 구조화 출력 지원 |
| OpenAI SDK 직접 사용 | 없음 | 코드 전면 수정 | 프로바이더 종속 |
| LangChain4j | 라이브러리 레벨 | 설정 변경 | Spring 통합 미성숙 |

### 결정
- **Spring AI 2.0.0-M2** (`spring-ai-starter-model-openai`)
- `ChatClient.entity(Class)` → 구조화 출력으로 `AnalysisResult` record 직접 매핑
- domain 포트: `LlmClient` 인터페이스 → infrastructure에서 `SpringAiLlmClient`로 구현
- 프로바이더 교체: `spring-ai-starter-model-openai` → 다른 starter로 교체 + properties 변경

### 프롬프트 설계
- 입력: 기업명, 공시 제목, 본문 텍스트
- 출력: `AnalysisResult(sentiment, score, summary)`
  - sentiment: POSITIVE / NEUTRAL / NEGATIVE
  - score: -100 ~ +100
  - summary: 한국어 2-3문장

### 설정
```properties
spring.ai.openai.api-key=${OPENAI_API_KEY:}
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.3
```
