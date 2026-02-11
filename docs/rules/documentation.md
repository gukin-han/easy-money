# 문서 작성 규칙

## docs 폴더 구조

```
docs/
├── adr/        # 기술 주제별 결정 기록
├── specs/      # 도메인 단위 스펙
└── rules/      # 코드 컨벤션 및 규칙
```

## ADR (Architecture Decision Records)
- 기술 주제별로 파일을 분리한다 (예: `architecture.md`, `database.md`)
- 하나의 파일 안에서 결정사항을 changelog 형태로 누적한다
- 각 항목에는 날짜, 상태(채택/폐기), 컨텍스트, 결정 내용을 포함한다

## Specs
- 도메인 단위로 파일을 분리한다 (예: `disclosure.md`, `analysis.md`)
- 역할, 책임, 핵심 객체, 인바운드/아웃바운드를 포함한다

## Rules
- 주제별로 파일을 분리한다 (예: `architecture.md`, `documentation.md`)
