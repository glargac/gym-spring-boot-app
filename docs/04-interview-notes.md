# Interview Notes: Step 0 (what exists today)

Format for every step: **concept → likely question → honest critique of the code**. In the interview you will see working but flawed code, so the critique is the main exercise.

## 1. Records: `HealthMetrics`, `AssessmentRequest`
A record is an immutable data carrier. The compiler generates the constructor, accessors, `equals`, `hashCode` and `toString`.
- **Likely question:** "Record vs. Lombok `@Value` vs. a plain class?"
- **Answer:** a record is final, cannot extend a class, and can implement interfaces. Lombok is a compile-time tool, a record is a language feature.
- **Senior trap:** a JPA `@Entity` cannot be a record. Hibernate needs a no-arg constructor and mutable fields. Records are for DTOs and value objects.

## 2. Enums with behavior: `BmiCategory`, `ActivityLevel`
`ActivityLevel` holds its own `factor`, so there is no `if (level == MODERATE) return 1.55`.
- **Smell:** Switch Statements. refactoring.guru fixes it with Replace Conditional with Polymorphism.
- **Critique:** `BmiCategory.of()` is still an if-chain. For numeric ranges that is fine. Over-engineering is also a defect.
- **Likely question:** "Rewrite this if/else without if/else." Options: enum with fields, Strategy map, polymorphism.

## 3. Switch expressions: `adviceFor`, `sexConstant`
A switch expression over an enum must be exhaustive. Adding `BmiCategory.SEVERE` breaks compilation until every switch handles it.
- **Why it matters:** the compiler finds every place to update, unlike an if/else chain that fails silently.
- **Trade-off:** adding a category means editing the switch (an OCP violation). Moving `advice` into the enum fixes that.

## 4. Dependency injection: `HealthController` → `HealthMetricsService`
- **Likely question:** "Why constructor injection over field `@Autowired`?"
- **Answer:** the dependency can be `final`, the object is never half-built, tests need no reflection, and a missing dependency fails at startup.
- **Critique (DIP):** the controller depends on the concrete class, not an interface. With one implementation it is acceptable. Introduce an interface when a second implementation appears or you need a mock.

## 5. Layering and DTOs
Controller (HTTP) → service (rules) → repository (data). `@Valid` on the DTO validates at the boundary.
- **Likely question:** "Why not expose entities directly?"
- **Answer:** it leaks the schema, risks mass assignment, and ties API versioning to the database.

## 6. Flaws in the current code (practice spotting these)
| Smell / principle | Where | Fix |
|-------------------|-------|-----|
| Long Parameter List | `calculate(sex, birthDate, heightCm, weightKg, level)` | Introduce Parameter Object |
| Primitive Obsession | `int heightCm`, `double weightKg` | `Height` / `Weight` value-object records |
| Hidden dependency | `LocalDate.now()` in the service; the test uses `minusYears(40)`, fragile around birthdays | inject a `java.time.Clock` |
| Misplaced types | `Sex`, `ActivityLevel` nested in the service, used by controller and DB | move to a domain package |
| Precision | `double` for BMI is fine; for money use `BigDecimal` | know why |

## 7. Database design
- **Reference tables vs. enums:** goals, diets and locations are tables because the admin configures them without redeploying (OCP applied to data). `role` and `sex` are fixed, so a CHECK constraint is enough.
- **Flaw:** `excluded_foods` is a comma-separated string, violating first normal form (1NF). Fix: a join table.
- **Flaw:** `app_user.weight_kg` holds only the current value. Progress tracking needs a `weight_log` history table.
- **`ddl-auto: validate`:** Flyway owns the schema, Hibernate only checks entities against it at startup.
- **Seed password:** a placeholder. Never store plain text, always BCrypt.

## Coming next
1. Strategy for the recommender (replaces an if/else per goal)
2. Factory to pick the strategy
3. Template Method for plan generation
4. Builder for complex objects
5. Observer with `ApplicationEventPublisher` ("user created")
6. JPA: N+1, lazy vs. eager, `@Transactional`
7. Spring Security and JWT
8. Concurrency: virtual threads, `CompletableFuture`
