-- More seed data so EVERY goal x diet has a meal plan and EVERY goal x location has a workout.
-- V2 is already applied everywhere, so it must never be edited: new data = new migration.
-- Written with INSERT ... SELECT (no hard-coded ids) and the NOT EXISTS guards skip the rows V2 already created.

-- ---------------------------------------------------------------------------------------------
-- Meal plans: one per goal x diet. kcal_adjust depends on the goal.
-- ---------------------------------------------------------------------------------------------
INSERT INTO meal_plan (goal_code, diet_type_code, kcal_adjust, name)
SELECT g.code, d.code, a.adjust, g.name || ' - ' || d.name
FROM goal g
CROSS JOIN diet_type d
JOIN (VALUES ('LOSE_WEIGHT', -500), ('BURN_FAT', -350), ('MAINTAIN', 0),
             ('BUILD_MUSCLE', 250), ('GAIN_WEIGHT', 500)) AS a(goal_code, adjust)
  ON a.goal_code = g.code
WHERE NOT EXISTS (SELECT 1 FROM meal_plan mp WHERE mp.goal_code = g.code AND mp.diet_type_code = d.code);

-- Meals per diet. kcal here is the BASE weight of each meal (a 2000 kcal day); the API scales the
-- portions to the user's own target, so these numbers are proportions, not final calories.
INSERT INTO meal (meal_plan_id, meal_type, description, kcal)
SELECT mp.id, m.meal_type, m.description, m.kcal
FROM meal_plan mp
JOIN (VALUES
 ('STANDARD',      'BREAKFAST', 'Oatmeal with fruit and 2 eggs', 500),
 ('STANDARD',      'LUNCH',     'Grilled chicken, brown rice, salad', 700),
 ('STANDARD',      'SNACK',     'Greek yogurt and nuts', 200),
 ('STANDARD',      'DINNER',    'Baked fish with vegetables', 600),
 ('KETO',          'BREAKFAST', 'Scrambled eggs with avocado', 500),
 ('KETO',          'LUNCH',     'Salmon with broccoli and butter', 700),
 ('KETO',          'SNACK',     'Cheese and walnuts', 200),
 ('KETO',          'DINNER',    'Chicken salad with olive oil', 600),
 ('VEGETARIAN',    'BREAKFAST', 'Greek yogurt, granola and berries', 500),
 ('VEGETARIAN',    'LUNCH',     'Lentil and vegetable curry with rice', 700),
 ('VEGETARIAN',    'SNACK',     'Hummus with carrot sticks', 200),
 ('VEGETARIAN',    'DINNER',    'Egg and spinach omelette with whole-grain toast', 600),
 ('VEGAN',         'BREAKFAST', 'Tofu scramble with vegetables', 500),
 ('VEGAN',         'LUNCH',     'Chickpea quinoa bowl with tahini', 700),
 ('VEGAN',         'SNACK',     'Fruit and seed mix', 200),
 ('VEGAN',         'DINNER',    'Black bean tacos with avocado', 600),
 ('HIGH_PROTEIN',  'BREAKFAST', 'Egg white omelette with turkey and oats', 500),
 ('HIGH_PROTEIN',  'LUNCH',     'Lean beef, sweet potato and green beans', 700),
 ('HIGH_PROTEIN',  'SNACK',     'Cottage cheese and whey shake', 200),
 ('HIGH_PROTEIN',  'DINNER',    'Grilled tuna steak with quinoa', 600),
 ('MEDITERRANEAN', 'BREAKFAST', 'Whole-grain toast, feta, tomato and olive oil', 500),
 ('MEDITERRANEAN', 'LUNCH',     'Grilled sea bream with chickpea salad', 700),
 ('MEDITERRANEAN', 'SNACK',     'Olives and a piece of fruit', 200),
 ('MEDITERRANEAN', 'DINNER',    'Lentil stew with vegetables and olive oil', 600)
) AS m(diet_code, meal_type, description, kcal)
  ON m.diet_code = mp.diet_type_code
WHERE NOT EXISTS (SELECT 1 FROM meal x WHERE x.meal_plan_id = mp.id);

-- ---------------------------------------------------------------------------------------------
-- Workouts: a 3-day template for every goal x location that does not have one yet.
-- The days/exercises depend on the location; the volume (sets/reps/cardio minutes) depends on the goal.
-- ---------------------------------------------------------------------------------------------
INSERT INTO workout_template (goal_code, location_code, days_per_week, name)
SELECT g.code, l.code, 3, g.name || ' - ' || l.name || ' (3 days)'
FROM goal g
CROSS JOIN training_location l
WHERE NOT EXISTS (SELECT 1 FROM workout_template t WHERE t.goal_code = g.code AND t.location_code = l.code);

INSERT INTO workout_day (template_id, day_index, focus)
SELECT t.id, d.day_index, d.focus
FROM workout_template t
JOIN (VALUES
 ('GYM',     1, 'Legs and glutes'),
 ('GYM',     2, 'Chest, back and core'),
 ('GYM',     3, 'Shoulders and arms'),
 ('OUTDOOR', 1, 'Easy run and legs'),
 ('OUTDOOR', 2, 'Park calisthenics'),
 ('OUTDOOR', 3, 'Intervals'),
 ('HOME',    1, 'Lower body'),
 ('HOME',    2, 'Upper body and core'),
 ('HOME',    3, 'Full body circuit')
) AS d(location_code, day_index, focus)
  ON d.location_code = t.location_code
WHERE NOT EXISTS (SELECT 1 FROM workout_day x WHERE x.template_id = t.id);

-- kind STRENGTH uses the goal's sets/reps; kind CARDIO uses 1 set of N minutes.
INSERT INTO workout_exercise (day_id, name, sets, reps)
SELECT wd.id, e.name,
       CASE WHEN e.kind = 'STRENGTH' THEN p.sets ELSE 1 END,
       CASE WHEN e.kind = 'STRENGTH' THEN p.reps ELSE CAST(p.cardio_min AS VARCHAR(10)) || ' min' END
FROM workout_day wd
JOIN workout_template t ON t.id = wd.template_id
JOIN (VALUES
 ('GYM',     1, 'Squat',               'STRENGTH'),
 ('GYM',     1, 'Romanian deadlift',   'STRENGTH'),
 ('GYM',     1, 'Incline treadmill',   'CARDIO'),
 ('GYM',     2, 'Bench press',         'STRENGTH'),
 ('GYM',     2, 'Barbell row',         'STRENGTH'),
 ('GYM',     2, 'Cable crunch',        'STRENGTH'),
 ('GYM',     3, 'Overhead press',      'STRENGTH'),
 ('GYM',     3, 'Biceps curl',         'STRENGTH'),
 ('GYM',     3, 'Stationary bike',     'CARDIO'),
 ('OUTDOOR', 1, 'Warm-up jog',         'CARDIO'),
 ('OUTDOOR', 1, 'Walking lunges',      'STRENGTH'),
 ('OUTDOOR', 1, 'Step-ups on a bench', 'STRENGTH'),
 ('OUTDOOR', 2, 'Push-ups',            'STRENGTH'),
 ('OUTDOOR', 2, 'Pull-ups',            'STRENGTH'),
 ('OUTDOOR', 2, 'Bodyweight squats',   'STRENGTH'),
 ('OUTDOOR', 3, 'Hill sprints',        'STRENGTH'),
 ('OUTDOOR', 3, 'Burpees',             'STRENGTH'),
 ('OUTDOOR', 3, 'Cool-down walk',      'CARDIO'),
 ('HOME',    1, 'Bodyweight squats',   'STRENGTH'),
 ('HOME',    1, 'Glute bridges',       'STRENGTH'),
 ('HOME',    1, 'Jumping jacks',       'CARDIO'),
 ('HOME',    2, 'Push-ups',            'STRENGTH'),
 ('HOME',    2, 'Plank',               'STRENGTH'),
 ('HOME',    2, 'Mountain climbers',   'CARDIO'),
 ('HOME',    3, 'Burpees',             'STRENGTH'),
 ('HOME',    3, 'Reverse lunges',      'STRENGTH'),
 ('HOME',    3, 'Skipping rope',       'CARDIO')
) AS e(location_code, day_index, name, kind)
  ON e.location_code = t.location_code AND e.day_index = wd.day_index
JOIN (VALUES
 ('LOSE_WEIGHT',  3, '12', 25),
 ('BURN_FAT',     3, '15', 30),
 ('MAINTAIN',     3, '10', 20),
 ('BUILD_MUSCLE', 4, '8',  10),
 ('GAIN_WEIGHT',  4, '10', 10)
) AS p(goal_code, sets, reps, cardio_min)
  ON p.goal_code = t.goal_code
WHERE NOT EXISTS (SELECT 1 FROM workout_exercise x WHERE x.day_id = wd.id);
