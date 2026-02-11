# 아키텍처 규칙

## 패키지 구조
베이스 패키지: `com.easymoney`

```
com.easymoney
├── global                          // 전역 공통 (config, error)
├── disclosure                      // 공시 수집 도메인
├── analysis                        // LLM 분석 도메인
└── market                          // 시장 반응 도메인
```

각 도메인은 `interfaces → application → domain ← infrastructure` 레이어를 갖는다.

## 의존 방향 규칙
- **domain** 레이어는 다른 레이어에 의존하지 않는다.
- **application**은 domain에만 의존한다.
- **interfaces**와 **infrastructure**는 application과 domain에 의존할 수 있다.
- **도메인 간 직접 의존은 금지한다.** 이벤트 또는 application 레이어 간 연동을 사용한다.

## 지켜야 할 것
- Repository 인터페이스는 `domain/repository`에, 구현체는 `infrastructure/persistence`에 둔다.
- 외부 API 클라이언트는 `infrastructure/api`에 둔다.
- DTO(Command, Info)는 `application/dto`에 둔다.
- 스케줄러, 이벤트 리스너는 `interfaces`에 둔다.

## 하지 말아야 할 것
- domain 레이어에서 Spring 어노테이션(@Service, @Component 등) 사용 금지
- domain 레이어에서 infrastructure 클래스 직접 참조 금지
- 도메인 간 Entity 직접 참조 금지 (ID로만 참조)
