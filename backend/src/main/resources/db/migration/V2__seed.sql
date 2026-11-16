INSERT INTO goal (code, name, description) VALUES
 ('LOSE_WEIGHT', 'Lose weight', 'Moderate calorie deficit'),
 ('GAIN_WEIGHT', 'Gain weight', 'Calorie surplus'),
 ('BUILD_MUSCLE', 'Build muscle', 'Hypertrophy with a slight surplus'),
 ('BURN_FAT', 'Burn fat', 'Cutting while preserving muscle'),
 ('MAINTAIN', 'Maintain', 'Keep weight and stay healthy');

INSERT INTO diet_type (code, name, description) VALUES
 ('STANDARD', 'Standard', 'Meat, vegetables, carbohydrates'),
 ('KETO', 'Keto', 'Low carb, high fat'),
 ('VEGETARIAN', 'Vegetarian', 'No meat'),
 ('VEGAN', 'Vegan', 'No animal products'),
 ('HIGH_PROTEIN', 'High protein', 'Protein first'),
 ('MEDITERRANEAN', 'Mediterranean', 'Fish, olive oil, legumes');

INSERT INTO training_location (code, name) VALUES
 ('GYM', 'Gym'), ('OUTDOOR', 'Outdoors'), ('HOME', 'At home');

-- Workout: lose weight, gym, 3 days
INSERT INTO workout_template (goal_code, location_code, days_per_week, name)
VALUES ('LOSE_WEIGHT', 'GYM', 3, 'Full body + cardio (3 days)');

INSERT INTO workout_day (template_id, day_index, focus) VALUES
 (1, 1, 'Legs and glutes + cardio'),
 (1, 2, 'Chest, back and core'),
 (1, 3, 'Shoulders, arms and HIIT');

INSERT INTO workout_exercise (day_id, name, sets, reps) VALUES
 (1, 'Squat', 4, '10'), (1, 'Romanian deadlift', 3, '10'), (1, 'Incline treadmill', 1, '20 min'),
 (2, 'Bench press', 4, '10'), (2, 'Barbell row', 4, '10'), (2, 'Plank', 3, '45 s'),
 (3, 'Overhead press', 3, '10'), (3, 'Curls and dips', 3, '12'), (3, 'HIIT bike', 8, '30 s/30 s');

-- Workout: lose weight, outdoors, 3 days
INSERT INTO workout_template (goal_code, location_code, days_per_week, name)
VALUES ('LOSE_WEIGHT', 'OUTDOOR', 3, 'Running + calisthenics (3 days)');

INSERT INTO workout_day (template_id, day_index, focus) VALUES
 (2, 1, 'Easy jog'), (2, 2, 'Park calisthenics'), (2, 3, 'Running intervals');

INSERT INTO workout_exercise (day_id, name, sets, reps) VALUES
 (4, 'Continuous jog', 1, '30 min'),
 (5, 'Push-ups', 4, '12'), (5, 'Assisted pull-ups', 3, '8'), (5, 'Jump squats', 3, '15'),
 (6, '200 m sprint', 8, '1 min rest');

-- Meal plans: lose weight (500 kcal deficit)
INSERT INTO meal_plan (goal_code, diet_type_code, kcal_adjust, name)
VALUES ('LOSE_WEIGHT', 'STANDARD', -500, 'Standard deficit'),
       ('LOSE_WEIGHT', 'KETO', -400, 'Keto deficit');

INSERT INTO meal (meal_plan_id, meal_type, description, kcal) VALUES
 (1, 'BREAKFAST', 'Oatmeal with fruit and 2 eggs', 450),
 (1, 'LUNCH', 'Grilled chicken, brown rice, salad', 600),
 (1, 'SNACK', 'Greek yogurt and almonds', 200),
 (1, 'DINNER', 'Baked fish with vegetables', 450),
 (2, 'BREAKFAST', 'Scrambled eggs with avocado', 450),
 (2, 'LUNCH', 'Salmon with broccoli and butter', 650),
 (2, 'SNACK', 'Cheese and walnuts', 250),
 (2, 'DINNER', 'Chicken salad with olive oil', 450);

-- Initial admin user. password_hash is a PLACEHOLDER: it gets replaced when we add Spring Security (BCrypt).
INSERT INTO app_user (email, password_hash, role, full_name, birth_date, sex, height_cm, weight_kg, activity_level)
VALUES ('admin@wellness.local', 'PLACEHOLDER_REPLACE_WITH_BCRYPT', 'ADMIN',
        'Admin', DATE '1985-01-01', 'OTHER', 175, 75.00, 'MODERATE');
