# Database

- **Dev**: in-memory H2 (PostgreSQL mode). It resets on every start; Flyway recreates the schema + seed data (`V1__schema.sql`, `V2__seed.sql`, `V3__more_seed.sql`).
- **Supabase**: it is PostgreSQL. We will use only its database (not its Auth/API): replace `spring.datasource.url/username/password` with your project's JDBC connection string (pooler mode) and pass credentials through environment variables, never in the repo.
- **Why Flyway**: the schema lives as versioned SQL (`V1`, `V2`...), reproducible in any environment. `ddl-auto: validate` makes Hibernate fail at startup if the entities don't match the schema.
- **Model**: reference tables (`goal`, `diet_type`, `training_location`) + admin-managed templates (`workout_*`, `meal_plan`/`meal`) + `app_user` and `user_preference`. The recommendation engine (step 3) will pick a workout template by `(goal, location, days_per_week)` and a meal plan by `(goal, diet_type)`.
- **Seed data**: V2 has the catalogs, two LOSE_WEIGHT workouts, two LOSE_WEIGHT meal plans and the admin user (id 1). V3 adds, with `INSERT ... SELECT` over goal x diet x location, a meal plan for **every** goal x diet and a 3-day workout for **every** goal x location. Every `INSERT` has a `NOT EXISTS` guard, so it skips what V2 created.
- **Admin password**: V2 stores `PLACEHOLDER_REPLACE_WITH_BCRYPT`. A real hash cannot live in a migration (it would be committed), so `AdminBootstrap` sets it at startup from `WELLNESS_ADMIN_PASSWORD` or a random value.
- **Meal calories**: `meal.kcal` is the base weight of a meal in a ~2000 kcal day. The recommendation scales every meal by `targetKcal / sum(kcal)`.
- **Deleting reference rows**: foreign keys protect them. Deleting a goal, diet type or location that is still used fails and the API answers 409.
- **Aggregates**: `workout_template -> workout_day -> workout_exercise` and `meal_plan -> meal` are saved and deleted as a unit (JPA cascade + orphan removal).
