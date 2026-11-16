# Roadmap: Wellness App + Senior Java Interview Prep

Each step = **build one piece** + **study one concept** + **leave one interview exercise**.

| # | We build | We study |
|---|----------|----------|
| 0 | Spring Boot skeleton, DB schema, seed data, BMI/BMR calculation (DONE, not yet run) | Java 8→27 (`01-java-8-to-27.md`), what Spring Boot is |
| 1 | JPA entities + repositories + "create user" use case | SRP, DIP, DTO vs Entity, Strategy |
| 2 | Spring Security + JWT, ADMIN/USER roles | Code smells: Long Method, Large Class |
| 3 | Recommendation engine (workout + meal plan from preferences) | Strategy, Factory, Replace Conditional with Polymorphism |
| 4 | Admin CRUD for templates (workouts/meals) | Template Method, Builder, OCP |
| 5 | Frontend (Vite + React + TS) | API contract, error handling |
| 6 | Move to Supabase (Postgres) | Transactions, N+1, indexes |

## Decisions made
- **Java 25 (LTS)** as the build target. Java 27 exists (Sept 2026), but Spring Boot 4.1 officially declares support up to 25. Interviews usually ask about LTS releases (8, 11, 17, 21, 25). Change `java.version` to 27 once Boot supports it.
- **Spring Boot 4.1**: modular monolith first; microservices once there is a real reason (see note below).
- **DB**: PostgreSQL. Supabase is hosted Postgres, so it works. Local dev uses H2 in Postgres mode; migrations use Flyway, so moving to Supabase is only configuration.
- **Frontend**: Node.js is a runtime, not a frontend framework. Recommended: Vite + React + TypeScript.

## About microservices
For this app, starting with a single service organized in domain packages (`user`, `health`, `plan`, `admin`) is best practice. We can split it later (e.g. `plan-service`) to study service-to-service communication, but starting from a modular monolith is the mature answer in an interview.

## Spring Boot in 5 lines
- **Inversion of Control**: Spring creates and wires the objects (beans); you declare dependencies in the constructor.
- **Auto-configuration**: based on the classpath, it configures the web server, JPA, Flyway, etc.
- **Starters**: one dependency brings a coherent set (`starter-webmvc`, `starter-data-jpa`).
- **Layers**: Controller (HTTP) → Service (business rules) → Repository (DB).
- **`application.yml`**: per-environment configuration.
