import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, ApiError } from '../../api/client'
import type { DietType, Goal, MealPlanAdmin, MealType } from '../../api/types'
import { ErrorSummary } from '../../components/ErrorSummary'
import { Field } from '../../components/Field'
import { useFetch } from '../../hooks/useFetch'

const MEAL_TYPES: { value: MealType; label: string }[] = [
  { value: 'BREAKFAST', label: 'Breakfast' },
  { value: 'LUNCH', label: 'Lunch' },
  { value: 'SNACK', label: 'Snack' },
  { value: 'DINNER', label: 'Dinner' },
]

interface EditorData {
  goals: Goal[]
  diets: DietType[]
  existing: MealPlanAdmin | null
}

export function MealPlanEditorPage() {
  const { id } = useParams()
  const planId = id ? Number(id) : null

  const { data, error, loading } = useFetch<EditorData>(async () => {
    const [goals, diets, existing] = await Promise.all([
      api.goals(),
      api.dietTypes(),
      planId === null ? Promise.resolve(null) : api.mealPlan(planId),
    ])
    return { goals, diets, existing }
  }, [planId])

  if (loading) return <p className="muted">Loading…</p>
  if (error || !data) return <p className="form-error">{error?.message ?? 'Could not load the editor'}</p>

  return <MealPlanForm key={planId ?? 'new'} data={data} planId={planId} />
}

interface MealForm {
  mealType: MealType
  description: string
  kcal: string
}

/** A typical day, so a new plan starts with sensible proportions instead of an empty list. */
const DEFAULT_MEALS: MealForm[] = [
  { mealType: 'BREAKFAST', description: '', kcal: '500' },
  { mealType: 'LUNCH', description: '', kcal: '700' },
  { mealType: 'SNACK', description: '', kcal: '200' },
  { mealType: 'DINNER', description: '', kcal: '600' },
]

function MealPlanForm({ data, planId }: { data: EditorData; planId: number | null }) {
  const navigate = useNavigate()
  const existing = data.existing

  const [goalCode, setGoalCode] = useState(existing?.goalCode ?? data.goals[0]?.code ?? '')
  const [dietTypeCode, setDietTypeCode] = useState(existing?.dietTypeCode ?? data.diets[0]?.code ?? '')
  const [kcalAdjust, setKcalAdjust] = useState(String(existing?.kcalAdjust ?? 0))
  const [name, setName] = useState(existing?.name ?? '')
  const [meals, setMeals] = useState<MealForm[]>(
    existing
      ? existing.meals.map((m) => ({ mealType: m.type as MealType, description: m.description, kcal: String(m.kcal) }))
      : DEFAULT_MEALS,
  )
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const baseTotal = meals.reduce((sum, m) => sum + (Number(m.kcal) || 0), 0)

  function updateMeal(index: number, patch: Partial<MealForm>) {
    setMeals((current) => current.map((m, i) => (i === index ? { ...m, ...patch } : m)))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors({})
    setFormError(null)
    setBusy(true)
    const body = {
      goalCode,
      dietTypeCode,
      kcalAdjust: Number(kcalAdjust),
      name: name.trim(),
      meals: meals.map((m) => ({ mealType: m.mealType, description: m.description.trim(), kcal: Number(m.kcal) })),
    }
    try {
      if (planId === null) await api.createMealPlan(body)
      else await api.updateMealPlan(planId, body)
      navigate('/admin/meal-plans')
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) setErrors(err.fieldErrors)
      else setFormError(err instanceof Error ? err.message : 'Something went wrong')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h2>{planId === null ? 'New meal plan' : 'Edit meal plan'}</h2>
        <Link to="/admin/meal-plans">Back to the list</Link>
      </div>

      <form onSubmit={handleSubmit} noValidate>
        <Field label="Name" error={errors.name}>
          {(p) => <input {...p} value={name} onChange={(e) => setName(e.target.value)} required />}
        </Field>
        <div className="grid-3">
          <Field label="Goal" error={errors.goalCode}>
            {(p) => (
              <select {...p} value={goalCode} onChange={(e) => setGoalCode(e.target.value)}>
                {data.goals.map((g) => (
                  <option key={g.code} value={g.code}>
                    {g.name}
                  </option>
                ))}
              </select>
            )}
          </Field>
          <Field label="Diet" error={errors.dietTypeCode}>
            {(p) => (
              <select {...p} value={dietTypeCode} onChange={(e) => setDietTypeCode(e.target.value)}>
                {data.diets.map((d) => (
                  <option key={d.code} value={d.code}>
                    {d.name}
                  </option>
                ))}
              </select>
            )}
          </Field>
          <Field label="Calorie adjustment" error={errors.kcalAdjust} hint="Added to the user's daily energy use, e.g. -500 to lose weight">
            {(p) => <input {...p} type="number" min={-2000} max={2000} step={50} value={kcalAdjust} onChange={(e) => setKcalAdjust(e.target.value)} />}
          </Field>
        </div>

        <fieldset className="editor-block">
          <legend>Meals</legend>
          <p className="muted small">
            The kcal values are proportions of a ~2000 kcal day. Each user sees them scaled to their own target (this plan adds up to {baseTotal}).
          </p>
          {meals.map((meal, index) => (
            <div key={index} className="meal-row">
              <Field label="Type" error={errors[`meals[${index}].mealType`]}>
                {(p) => (
                  <select {...p} value={meal.mealType} onChange={(e) => updateMeal(index, { mealType: e.target.value as MealType })}>
                    {MEAL_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>
                        {t.label}
                      </option>
                    ))}
                  </select>
                )}
              </Field>
              <Field label="Description" error={errors[`meals[${index}].description`]}>
                {(p) => <input {...p} value={meal.description} onChange={(e) => updateMeal(index, { description: e.target.value })} required />}
              </Field>
              <Field label="kcal" error={errors[`meals[${index}].kcal`]}>
                {(p) => <input {...p} type="number" min={1} max={3000} value={meal.kcal} onChange={(e) => updateMeal(index, { kcal: e.target.value })} />}
              </Field>
              <button
                type="button"
                className="danger-text"
                disabled={meals.length === 1}
                aria-label={`Remove meal ${index + 1}`}
                onClick={() => setMeals((current) => current.filter((_, i) => i !== index))}
              >
                Remove
              </button>
            </div>
          ))}
          <button type="button" onClick={() => setMeals((current) => [...current, { mealType: 'SNACK', description: '', kcal: '200' }])}>
            Add meal
          </button>
        </fieldset>

        <ErrorSummary
          formError={formError}
          errors={errors}
          shownInline={(key) =>
            ['name', 'goalCode', 'dietTypeCode', 'kcalAdjust'].includes(key) || /^meals\[\d+\]\.(mealType|description|kcal)$/.test(key)
          }
        />
        <div className="actions">
          <button type="submit" className="primary" disabled={busy}>
            {busy ? 'Saving…' : 'Save meal plan'}
          </button>
          <Link to="/admin/meal-plans" className="button">
            Cancel
          </Link>
        </div>
      </form>
    </section>
  )
}
