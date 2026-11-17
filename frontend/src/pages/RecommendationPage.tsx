import { Link } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import type { BmiCategory } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { useFetch } from '../hooks/useFetch'

const CATEGORY_LABEL: Record<BmiCategory, string> = {
  UNDERWEIGHT: 'Underweight',
  NORMAL: 'Healthy range',
  OVERWEIGHT: 'Overweight',
  OBESE: 'Obese',
}

const MEAL_LABEL: Record<string, string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  SNACK: 'Snack',
  DINNER: 'Dinner',
}

export function RecommendationPage() {
  const { session } = useAuth()
  const userId = session!.userId
  const { data, error, loading } = useFetch(() => api.recommendation(userId), [userId])

  if (loading) return <p className="muted">Building your plan…</p>

  if (error instanceof ApiError && error.status === 404) {
    return (
      <section className="card narrow">
        <h1>One step first</h1>
        <p className="muted">We need your preferences before we can build a plan.</p>
        <Link to="/preferences" className="button primary">
          Set my preferences
        </Link>
      </section>
    )
  }
  if (error || !data) return <p className="form-error">{error?.message ?? 'Could not load your plan'}</p>

  const { metrics, targetKcal, workout, mealPlan, notes } = data

  return (
    <div className="stack">
      <section className="card">
        <div className="card-head">
          <h1>Your plan</h1>
          <Link to="/preferences">Change preferences</Link>
        </div>

        <div className="stats">
          <Stat label="BMI" value={metrics.bmi.toFixed(1)} sub={CATEGORY_LABEL[metrics.category]} />
          <Stat label="Resting energy (BMR)" value={`${Math.round(metrics.bmr)}`} sub="kcal / day" />
          <Stat label="Daily energy use (TDEE)" value={`${Math.round(metrics.tdee)}`} sub="kcal / day" />
          {targetKcal !== null && <Stat label="Your target" value={`${targetKcal}`} sub="kcal / day" highlight />}
        </div>
        <p className="advice">{metrics.advice}</p>

        {notes.length > 0 && (
          <ul className="notes" aria-label="Things to know">
            {notes.map((note) => (
              <li key={note}>{note}</li>
            ))}
          </ul>
        )}
      </section>

      {workout && (
        <section className="card">
          <h2>{workout.name}</h2>
          <p className="muted small">
            {workout.daysPerWeek} days a week · chosen because: {workout.matchedBy}
          </p>
          <div className="days">
            {workout.days.map((day) => (
              <article key={day.dayIndex} className="day">
                <h3>
                  <span className="day-number">Day {day.dayIndex}</span> {day.focus}
                </h3>
                <ul>
                  {day.exercises.map((exercise) => (
                    <li key={exercise.name}>
                      <span>{exercise.name}</span>
                      <span className="muted">
                        {exercise.sets} × {exercise.reps}
                      </span>
                    </li>
                  ))}
                </ul>
              </article>
            ))}
          </div>
        </section>
      )}

      {mealPlan && (
        <section className="card">
          <h2>{mealPlan.name}</h2>
          <p className="muted small">
            About {mealPlan.totalKcal} kcal a day, with portions scaled to your target (×{mealPlan.portionFactor}).
          </p>
          <ul className="meals">
            {mealPlan.meals.map((meal) => (
              <li key={meal.type}>
                <span className="chip">{MEAL_LABEL[meal.type] ?? meal.type}</span>
                <span className="meal-text">{meal.description}</span>
                <span className="muted">{meal.kcal} kcal</span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}

function Stat({ label, value, sub, highlight = false }: { label: string; value: string; sub: string; highlight?: boolean }) {
  return (
    <div className={highlight ? 'stat highlight' : 'stat'}>
      <span className="stat-label">{label}</span>
      <span className="stat-value">{value}</span>
      <span className="stat-sub">{sub}</span>
    </div>
  )
}
