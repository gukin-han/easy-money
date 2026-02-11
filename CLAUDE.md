# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.2 web application using Java 21 and Gradle (Kotlin DSL). Uses Spring Data JPA with an H2 in-memory database, Spring Web MVC, and Lombok.

## Build Commands

```bash
./gradlew build          # Full build with tests
./gradlew bootRun        # Run the application (default port 8080)
./gradlew test           # Run all tests
./gradlew clean build    # Clean rebuild
```

To run a single test class:
```bash
./gradlew test --tests "com.easymoney.EasymoneyApplicationTests"
```

## Architecture

- **Package root:** `com.easymoney`
- **Entry point:** `EasymoneyApplication.java` — standard `@SpringBootApplication`
- **Config:** `src/main/resources/application.properties`
- **Database:** H2 in-memory (with web console available)
- **Testing:** JUnit 5 with `@SpringBootTest` for integration tests

## Documentation Structure

- `docs/rules/` — 코드 컨벤션, 지켜야 할 규칙, 하지 말아야 할 규칙
- `docs/specs/` — 도메인 단위 스펙 문서
- `docs/adr/` — Architecture Decision Records (기술적 결정사항의 컨텍스트 기록)

## Key Dependencies

- **Spring Boot 4.0.2** with spring-boot-starter-web and spring-boot-starter-data-jpa
- **H2 Database** (runtime, in-memory)
- **Lombok** (compile-only, annotation processing)
- **Gradle 8.14.3** via wrapper
