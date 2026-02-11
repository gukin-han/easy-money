# Analysis 도메인 스펙 (LLM 분석)

## 역할
"이것은 호재인가 악재인가?"

## 책임
- 수집된 공시 텍스트를 LLM에게 질의
- 감성 분석
- 정량 평가 (점수화)

## 핵심 객체
- **AnalysisReport** — 분석 결과
- **Sentiment** — 감성 상태 (Enum)

## 인바운드
- `AnalysisEventListener` — 공시 수집 이벤트 수신

## 아웃바운드
- `SpringAiClient` — OpenAI/Gemini 연동
