# Roadmap: Wellness App + Senior Java Interview Prep

Each step = **build one piece** + **study one concept** + **leave one interview exercise**.

| # | We build | We study | Status |
|---|----------|----------|--------|
| 0 | Spring Boot skeleton, DB schema, seed data, BMI/BMR calculation | Java 8→27 (`01-java-8-to-27.md`), what Spring Boot is | Done |
| 1 | JPA entities + repositories + "create user" use case | SRP, DIP, DTO vs Entity | Done: entities, catalogs, registration, preferences |
| 2 | Spring Security + JWT, ADMIN/USER roles | Code smells: Long Method, Large Class | Done: login, roles, ownership checks, CORS |
| 3 | Recommendation engine (workout + meal plan from preferences) | Strategy, Replace Conditional with Polymorphism | Done with Strategy. A Factory was not needed: Spring injects the ordered list |
| 4 | Admin CRUD for templates (workouts/meals) | Template Method, Builder, OCP | Backend done. Template Method (`AdminPlanService`) and Builder (`AppUser.Builder`, test builder) applied in a refactor |
| 5 | Frontend (Vite + React + TS) | API contract, error handling | User flow and admin screens (catalogs, workout templates, meal plans) done. No frontend tests |
| 6 | Move to Supabase (Postgres) | Transactions, N+1, indexes | Not started |

Running instructions and the endpoint list are in `05-api-and-running.md`.

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

## Decisions made while building (steps 1 to 5)
- **Spring Boot 4 moved test slices** (`@DataJpaTest`, `@WebMvcTest`/MockMvc) into separate test starters that the pom does not include. We use `@SpringBootTest` (service tests with `@Transactional` rollback, and real-HTTP tests on a random port with their own in-memory database).
- **JWT without a JWT library**: `spring-boot-starter-security-oauth2-resource-server` brings Nimbus; HS256 with a shared secret keeps it simple (RS256 would let other services verify tokens without being able to mint them).
- **Diet is a hard constraint, location is soft**: a vegan never receives a fallback to another diet, while a missing location falls back to another place and says so.
- **Meal calories are base weights**: stored values describe proportions of a ~2000 kcal day and the API scales them to each user's target.
- **Never edit an applied migration**: extra seed data went into `V3__more_seed.sql`.
