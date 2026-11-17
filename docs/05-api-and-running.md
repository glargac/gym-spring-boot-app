# Running the app and API reference

## Run it
```bash
# Backend (Spring Boot 4.1, Java 25). `java` is not linked system-wide on this machine, so point JAVA_HOME at the Homebrew JDK:
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn spring-boot:run     # http://localhost:8080
JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn test                # 37 tests

# Frontend (Vite + React + TypeScript)
cd frontend
npm install
npm run dev                                                      # http://localhost:5173
npm run build && npm run lint                                    # type-check, bundle, lint
```
The database is **in-memory H2**: every backend restart wipes users and preferences, and Flyway rebuilds the schema and seed data.

## Configuration (environment variables)
| Variable | Default | Purpose |
|----------|---------|---------|
| `WELLNESS_JWT_SECRET` | random key per start | HS256 signing key, at least 32 characters. With the default, tokens stop working when the app restarts. |
| `WELLNESS_ADMIN_PASSWORD` | random, logged once at startup | Password of the seeded `admin@wellness.local`. Setting it also resets the password on every start. |
| `WELLNESS_CORS_ORIGINS` | `http://localhost:5173` | Browser origins allowed to call the API. |
| `VITE_API_URL` (frontend) | `http://localhost:8080` | Where the frontend sends requests. |

The generated admin password appears in the startup log: `Generated admin password for admin@wellness.local ...`.

## Endpoints
| Method and path | Who | What |
|-----------------|-----|------|
| `POST /api/auth/login` | public | email + password, returns a 1-hour JWT (`sub` = user id, `role` claim) |
| `POST /api/users` | public | register a USER (a client can never choose its role) |
| `GET /api/goals`, `/api/diet-types`, `/api/training-locations` | public | catalogs, sorted by name |
| `POST /api/health/assessment` | public | BMI, BMR, TDEE and advice for the given body data |
| `GET` / `PUT /api/users/{userId}/preferences` | that user or ADMIN | read / replace (upsert) preferences |
| `GET /api/users/{userId}/recommendation` | that user or ADMIN | metrics, workout, meal plan scaled to the target kcal, notes |
| `PUT` / `DELETE /api/admin/{goals,diet-types,training-locations}/{code}` | ADMIN | create-or-update / delete a catalog item (409 if still referenced) |
| `GET`, `POST /api/admin/workout-templates`, `GET`, `PUT`, `DELETE .../{id}` | ADMIN | full workout templates with days and exercises |
| `GET`, `POST /api/admin/meal-plans`, `GET`, `PUT`, `DELETE .../{id}` | ADMIN | full meal plans with meals (kcal are base weights, scaled per user) |

Send the token as `Authorization: Bearer <token>`. Everything not listed as public is denied by default.

## Error format
Every error is `application/problem+json` (RFC 9457): `status`, `title`, `detail`, `instance`, plus `errors: {field: message}` for validation problems (nested fields appear as `days[0].exercises[0].name`).

| Status | Meaning |
|--------|---------|
| 400 | validation failed, unknown catalog code, malformed body |
| 401 | missing, invalid or expired token; wrong email or password (same message for both) |
| 403 | valid token, not allowed (someone else's data, or admin area) |
| 404 | user, template, plan or preferences not found |
| 409 | email already registered; catalog item still in use |

Validation messages follow the server's locale (Spanish on this machine).

## Code map
```
backend/src/main/java/com/wellness/
  health/    BMI/BMR/TDEE calculation + /api/health/assessment
  catalog/   Goal, DietType, TrainingLocation entities, repositories, public GET endpoints
  user/      AppUser, UserPreference, registration, preferences, @OwnerOrAdmin usage
  workout/   WorkoutTemplate -> WorkoutDay -> WorkoutExercise (aggregate)
  meal/      MealPlan -> Meal (aggregate)
  plan/      recommendation engine: Strategy interface + implementations, service, response views
  admin/     admin CRUD: AdminPlanService (Template Method) + workout/meal subclasses, controllers, request records
  security/  SecurityConfig, JWT encode/decode, login, AdminBootstrap, 401/403 JSON handlers
  web/       ApiExceptionHandler + shared exceptions
frontend/src/
  api/       typed fetch client + DTO types
  auth/      AuthProvider, JWT decoding (UI only), useAuth
  pages/     Home, Login, Register, Preferences, Recommendation
  components/ Layout, ProtectedRoute, Field
```
