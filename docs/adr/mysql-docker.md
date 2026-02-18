# ADR: H2 → MySQL 전환 + Docker 배포

## 상태
승인됨

## 맥락
- H2 in-memory DB 사용 시 서버 재시작마다 데이터가 유실됨
- 프로덕션 환경에서 데이터 영속성이 필수적
- 컨테이너 기반 배포로 환경 일관성을 확보해야 함

## 결정

### 프로덕션 DB: MySQL 8.0
- `com.mysql:mysql-connector-j` 런타임 의존성 추가
- `spring.jpa.hibernate.ddl-auto=update` (스키마 자동 마이그레이션)
- DB 자격 증명은 환경변수(`DB_USERNAME`, `DB_PASSWORD`)로 주입

### 테스트 DB: Testcontainers MySQL
- `spring-boot-testcontainers` + `testcontainers:mysql` 테스트 의존성 추가
- `@ServiceConnection`으로 datasource 자동 주입 (테스트 설정에 datasource URL 불필요)
- `MySqlTestContainerConfig`를 `@TestConfiguration`으로 작성, `@Import`로 사용
- `spring.jpa.hibernate.ddl-auto=create-drop` (테스트마다 깨끗한 DB)

### Docker 배포
- `Dockerfile`: eclipse-temurin:21-jre 기반, fat JAR 실행
- `docker-compose.yml`: MySQL + App 서비스 구성
- `.env.example`: 환경변수 템플릿

## 제거된 것
- `spring-boot-h2console` 의존성
- H2 관련 application.properties 설정 (`spring.h2.*`, `org.h2.Driver`, `jdbc:h2:mem:*`)
- H2는 `testRuntimeOnly`로 유지 (일부 단위 테스트 fallback용)

## 결과
- 서버 재시작 시 데이터 보존
- 프로덕션과 테스트 환경의 DB 엔진 일치 (MySQL ↔ MySQL)
- `docker compose up`으로 전체 스택 원커맨드 배포 가능
