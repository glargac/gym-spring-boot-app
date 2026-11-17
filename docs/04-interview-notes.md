# Interview Notes (steps 0 to 5)

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
- **Seed password:** a placeholder in V2, replaced at startup by `AdminBootstrap` (BCrypt). Never store plain text, and never commit a real hash.

## 8. JPA entities and mapping (`user/`, `workout/`, `meal/`, `catalog/`)
- **`@Enumerated(STRING)`**, never `ORDINAL`: reordering the enum would silently corrupt stored data.
- **`@ManyToOne` is EAGER by default.** We set `LAZY` everywhere. Consequence: with `open-in-view: false`, lazy fields can only be read inside a transaction, so services map to response records before returning (`LazyInitializationException` otherwise).
- **`equals`/`hashCode` on the id only**, with a constant `hashCode`: the hash must not change when `persist()` assigns the id, otherwise the entity is lost in a `HashSet`. Trade-off: every entity of a class shares one bucket.
- **`BigDecimal` for DECIMAL columns** (weight, hours). `double` gives binary rounding errors.
- **`@MapsId`** (shared primary key): `UserPreference`'s id is also its foreign key to `AppUser`, so a user can have at most one.
- **Rich entity vs. setters:** `UserPreference.change(...)` and `AppUser.changePasswordHash(...)` instead of generic setters. Constructors take every required field, so an object is never half-built.
- **N+1:** loading a template's days and then each day's exercises costs many queries. `JOIN FETCH` on two `List` collections throws `MultipleBagFetchException`. We used `@BatchSize(50)`; alternatives are `Set` collections or two separate queries.
- **Aggregates:** `cascade = ALL` + `orphanRemoval = true` on template -> days -> exercises. Clearing the list and re-adding replaces the children in one transaction.
- **Flaw:** three near-identical catalog classes (Duplicated Code). Abstracting them would hide that the three may diverge.

## 9. Spring Data, transactions and idempotency
- **Derived queries** (`findByGoalCodeAndLocationCodeOrderById`) are built from the method name and walk associations (`goal.code`).
- **`@Transactional(readOnly = true)`** skips dirty checking. A `@Transactional` method works through a proxy, so calling it from inside the same class bypasses the transaction (self-invocation).
- **`PUT` is idempotent**: preferences and catalog items are upserts. `POST` creates a new resource each time.
- **Check-then-insert is a race.** Two requests can both pass `existsByEmail`. The `UNIQUE` constraint is the real guard, and we flush inside a `try` to turn its violation into a 409. The same trick makes "delete a referenced row" a clear 409 instead of a 500 at commit time.

## 10. Validation and error handling (`web/ApiExceptionHandler`)
- **Bean Validation** at the boundary (`@Valid`), including nested lists (`days[0].exercises[0].name`). Cross-field rules (days must match `daysPerWeek`) cannot be expressed per field, so the service checks them.
- **`@RestControllerAdvice` + `ProblemDetail`** (RFC 9457): one error shape for the whole API.
- **Filters vs. MVC:** 401 and 403 come from servlet filters, before Spring MVC, so the advice never sees them. `JsonSecurityErrors` gives them the same shape.
- **Status codes:** 400 malformed or invalid, 401 not authenticated, 403 authenticated but not allowed, 404 missing, 409 conflicts with current state.
- **Mass assignment:** `RegisterUserRequest` has no `role` field, so a client cannot choose its role. Extra JSON properties are ignored.
- **Logging secrets:** a record's generated `toString` prints every field, so password-carrying records override it.

## 11. Security (`security/`)
- **Stateless JWT:** the token carries identity and role; the server checks signature, expiry and issuer on each request. HS256 = one shared secret (anyone who can verify can also mint tokens); RS256 separates the two.
- **CSRF disabled** is correct only because auth is a bearer header, not a cookie.
- **Deny by default** (`anyRequest().authenticated()`), plus a URL rule for `/api/admin/**` and `@OwnerOrAdmin` (a composed `@PreAuthorize`) for per-user data. URL rules and method security complement each other.
- **User enumeration:** same 401 for an unknown email and a wrong password, and one BCrypt comparison against a dummy hash even when the email is unknown, so response time does not give it away. Registration's 409 still reveals which emails exist (a common, deliberate trade-off).
- **BCrypt** ignores everything after 72 bytes, so passwords are capped at 72. Hash behind the `PasswordEncoder` interface so the algorithm can change.
- **Stale roles:** the role is frozen in the token until it expires. Fixes: short TTL, refresh tokens, or a revocation list.
- **CORS** is a browser rule. The preflight `OPTIONS` carries no token, so CORS must run before authentication. Use an explicit origin list, never `*` with credentials.
- **Flaws:** no rate limiting or lockout on login, no refresh tokens or logout revocation, the generated admin password is logged (acceptable for local dev only).

## 12. Strategy and the recommendation engine (`plan/`)
- **Strategy:** `WorkoutMatchStrategy` has two implementations (`SameLocationWorkoutMatch`, `AnyLocationWorkoutMatch`). Spring injects `List<WorkoutMatchStrategy>` sorted by `@Order`, so a new rule is a new class and no existing code changes (Open/Closed). No `if/else` chain and no Factory needed.
- **Static method on an interface** (`bestFit`) shares logic between strategies without an abstract base class.
- **Hard vs. soft constraints:** location falls back (and says so in `matchedBy`), diet never does. A vegan getting a chicken plan is worse than getting nothing.
- **Domain correctness:** stored meal calories are proportions, scaled to each user's target so the plan adds up to it. Targets below 1200 kcal are raised, with a note.
- **Flaws:** the excluded-food check is a substring match (misses plurals and synonyms), so it only warns. Workouts reuse the same exercises per location and vary only sets and reps. Plank is stored as "4 x 8" although it should be a duration (`reps` is a string that mixes a number and a unit).

## 13. Flyway discipline
- Applied migrations are immutable: Flyway stores a checksum and refuses to start if one changes. New data means a new `V3__...`.
- `INSERT ... SELECT` over a cross join, with `NOT EXISTS` guards, generates 90 combinations of data without hard-coded ids.

## 14. Testing
- **Boot 4 split the test slices into separate starters**, so we use `@SpringBootTest`. A `@Transactional` test rolls back after each method (service and repository tests); a `JdbcTemplate` shares the same transaction, so SQL deletes used to set up edge cases roll back too.
- **Real HTTP tests** (`RANDOM_PORT`) exercise the whole filter chain (401/403, CORS preflight, admin rules). They commit data, so each class gets its own in-memory database name.
- **A matrix test** (5 goals x 6 diets x 3 locations) proves the seed data has no gaps.
- **Gap:** no frontend tests, and no tests for concurrency (the duplicate-email race is only reasoned about).

## 15. Frontend (React + TypeScript)
- **Effects order:** React runs child effects before the parent's. A token wired up in a parent effect would arrive after the first fetch of a child page, so the API client owns the token and it is set synchronously.
- **Derived state vs. effects:** the preferences form mounts only when its data exists and builds its state from props, instead of filling state in an effect (an extra render and a flash of an empty form).
- **Race conditions in fetching:** `useFetch` ignores a response that arrives after the component unmounted or the inputs changed (`cancelled` flag, the same idea as `AbortController`).
- **Route guards are UX, not security.** The server re-checks everything. Decoding the JWT in the browser only drives the UI.
- **Token storage:** `localStorage` is readable by any injected script (XSS). An httpOnly cookie hides it from scripts but brings CSRF back. There is no free lunch, and the answer depends on the threat model.
- **TypeScript:** string-literal unions instead of enums (`erasableSyntaxOnly` forbids enums); a union exists only at compile time. `fetch` rejects only on network errors, never on HTTP errors, so the client converts non-2xx answers into `ApiError`.
- **Accessibility:** labels linked with `useId`, `aria-invalid` and `aria-describedby` for errors, visible focus, 44px touch targets.

## Coming next
1. Template Method for plan generation, and Builder for complex objects (promised in roadmap step 4, not applied yet)
2. Observer with `ApplicationEventPublisher` ("user registered")
3. Move to Supabase Postgres: transactions, indexes, connection pooling, and why H2 can hide Postgres differences
4. Frontend tests, admin screens, refresh tokens and login rate limiting
5. Concurrency: virtual threads, `CompletableFuture`
