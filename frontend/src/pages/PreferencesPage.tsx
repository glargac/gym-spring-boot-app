import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import type { DietType, Goal, PreferencesResponse, TrainingLocation } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { Field } from '../components/Field'
import { useFetch } from '../hooks/useFetch'

interface PageData {
  goals: Goal[]
  locations: TrainingLocation[]
  diets: DietType[]
  existing: PreferencesResponse | null
}

export function PreferencesPage() {
  const { session } = useAuth()
  const userId = session!.userId // ProtectedRoute guarantees a session here

  const { data, error, loading } = useFetch<PageData>(async () => {
    // The three catalogs and the saved preferences are independent, so they load in parallel.
    const [goals, locations, diets, existing] = await Promise.all([
      api.goals(),
      api.trainingLocations(),
      api.dietTypes(),
      api.getPreferences(userId).catch((err: unknown) => {
        if (err instanceof ApiError && err.status === 404) return null // nothing saved yet is not an error
        throw err
      }),
    ])
    return { goals, locations, diets, existing }
  }, [userId])

  if (loading) return <p className="muted">Loading…</p>
  if (error || !data) return <p className="form-error">{error?.message ?? 'Could not load the form'}</p>

  // The form only mounts once its data exists, so it can build its initial state directly from props.
  // (Filling state from an effect would cost an extra render and flash the empty form first.)
  return <PreferencesForm userId={userId} data={data} />
}

function PreferencesForm({ userId, data }: { userId: number; data: PageData }) {
  const navigate = useNavigate()
  const saved = data.existing

  const [form, setForm] = useState({
    goalCode: saved?.goalCode ?? data.goals[0]?.code ?? '',
    locationCode: saved?.locationCode ?? data.locations[0]?.code ?? '',
    dietTypeCode: saved?.dietTypeCode ?? data.diets[0]?.code ?? '',
    daysPerWeek: String(saved?.daysPerWeek ?? 3),
    hoursPerDay: String(saved?.hoursPerDay ?? 1),
    excludedFoods: saved?.excludedFoods ?? '',
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const set = (key: keyof typeof form) => (value: string) => setForm((current) => ({ ...current, [key]: value }))

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors({})
    setFormError(null)
    setBusy(true)
    try {
      await api.savePreferences(userId, {
        goalCode: form.goalCode,
        locationCode: form.locationCode,
        dietTypeCode: form.dietTypeCode,
        daysPerWeek: Number(form.daysPerWeek),
        hoursPerDay: Number(form.hoursPerDay),
        excludedFoods: form.excludedFoods.trim() || null,
      })
      navigate('/recommendation')
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) setErrors(err.fieldErrors)
      else setFormError(err instanceof Error ? err.message : 'Something went wrong')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="card">
      <h1>Your preferences</h1>
      <p className="muted">Tell us what you are aiming for and we will build a plan around it.</p>
      <form onSubmit={handleSubmit} noValidate>
        <Field label="Goal" error={errors.goalCode} hint={data.goals.find((g) => g.code === form.goalCode)?.description ?? undefined}>
          {(p) => (
            <select {...p} value={form.goalCode} onChange={(e) => set('goalCode')(e.target.value)}>
              {data.goals.map((g) => (
                <option key={g.code} value={g.code}>
                  {g.name}
                </option>
              ))}
            </select>
          )}
        </Field>
        <div className="grid-2">
          <Field label="Where do you train?" error={errors.locationCode}>
            {(p) => (
              <select {...p} value={form.locationCode} onChange={(e) => set('locationCode')(e.target.value)}>
                {data.locations.map((l) => (
                  <option key={l.code} value={l.code}>
                    {l.name}
                  </option>
                ))}
              </select>
            )}
          </Field>
          <Field label="Diet" error={errors.dietTypeCode}>
            {(p) => (
              <select {...p} value={form.dietTypeCode} onChange={(e) => set('dietTypeCode')(e.target.value)}>
                {data.diets.map((d) => (
                  <option key={d.code} value={d.code}>
                    {d.name}
                  </option>
                ))}
              </select>
            )}
          </Field>
        </div>
        <div className="grid-2">
          <Field label="Days per week" error={errors.daysPerWeek}>
            {(p) => <input {...p} type="number" min={1} max={7} value={form.daysPerWeek} onChange={(e) => set('daysPerWeek')(e.target.value)} />}
          </Field>
          <Field label="Hours per day" error={errors.hoursPerDay} hint="0.5 to 8, one decimal">
            {(p) => (
              <input {...p} type="number" step="0.5" min={0.5} max={8} value={form.hoursPerDay} onChange={(e) => set('hoursPerDay')(e.target.value)} />
            )}
          </Field>
        </div>
        <Field label="Foods to avoid" error={errors.excludedFoods} hint="Optional, separated by commas (for example: peanuts, shrimp)">
          {(p) => <input {...p} value={form.excludedFoods} onChange={(e) => set('excludedFoods')(e.target.value)} />}
        </Field>
        {formError && (
          <p className="form-error" role="alert">
            {formError}
          </p>
        )}
        <button type="submit" className="primary" disabled={busy}>
          {busy ? 'Saving…' : 'Save and see my plan'}
        </button>
      </form>
    </section>
  )
}
