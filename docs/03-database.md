# Database

- **Dev**: in-memory H2 (PostgreSQL mode). It resets on every start; Flyway recreates the schema + seed data (`V1__schema.sql`, `V2__seed.sql`).
- **Supabase**: it is PostgreSQL. We will use only its database (not its Auth/API): replace `spring.datasource.url/username/password` with your project's JDBC connection string (pooler mode) and pass credentials through environment variables, never in the repo.
- **Why Flyway**: the schema lives as versioned SQL (`V1`, `V2`...), reproducible in any environment. `ddl-auto: validate` makes Hibernate fail at startup if the entities don't match the schema.
- **Model**: reference tables (`goal`, `diet_type`, `training_location`) + admin-managed templates (`workout_*`, `meal_plan`/`meal`) + `app_user` and `user_preference`. The recommendation engine (step 3) will pick a workout template by `(goal, location, days_per_week)` and a meal plan by `(goal, diet_type)`.
