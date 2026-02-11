# 아키텍처 규칙

## 패키지 구조
```
com.easymoney
├── global                          // 전역 공통 (config, error)
├── disclosure                      // 공시 수집
├── analysis                        // LLM 분석
└── market                          // 시장 반응
```

각 도메인: `interfaces → application → domain ← infrastructure`

## 의존 방향
- **domain** → 어디에도 의존하지 않음
- **application** → domain만
- **interfaces / infrastructure** → application, domain

## DO
- Repository 인터페이스 → `domain/repository`, 구현체 → `infrastructure/persistence`
- 외부 API 클라이언트 → `infrastructure/api`
- DTO → `application/dto`
- 스케줄러, 이벤트 리스너 → `interfaces`

## DON'T
- domain에서 Spring 어노테이션(@Service, @Component 등) 사용
- domain에서 infrastructure 직접 참조
- 도메인 간 Entity 직접 참조 (ID로만)
- 도메인 간 직접 의존 (이벤트 또는 application 간 연동 사용)
