import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, ApiError } from '../api/client'
import type { ActivityLevel, Sex } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { Field } from '../components/Field'

const SEX_OPTIONS: { value: Sex; label: string }[] = [
  { value: 'FEMALE', label: 'Female' },
  { value: 'MALE', label: 'Male' },
  { value: 'OTHER', label: 'Other' },
]

const ACTIVITY_OPTIONS: { value: ActivityLevel; label: string }[] = [
  { value: 'SEDENTARY', label: 'Sedentary (desk job, little exercise)' },
  { value: 'LIGHT', label: 'Light (1–3 workouts a week)' },
  { value: 'MODERATE', label: 'Moderate (3–5 workouts a week)' },
  { value: 'ACTIVE', label: 'Active (6–7 workouts a week)' },
]

export function RegisterPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

  // Every input is held as a string (that is what inputs give us); numbers are converted once, on submit.
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    birthDate: '',
    sex: 'FEMALE' as Sex,
    heightCm: '',
    weightKg: '',
    activityLevel: 'LIGHT' as ActivityLevel,
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  // Lazy initial state runs once; 'en-CA' formats the LOCAL date as yyyy-MM-dd (toISOString would use UTC).
  const [today] = useState(() => new Date().toLocaleDateString('en-CA'))

  const set =
    <K extends keyof typeof form>(key: K) =>
    (value: (typeof form)[K]) =>
      setForm((current) => ({ ...current, [key]: value }))

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors({})
    setFormError(null)
    setBusy(true)
    try {
      await api.register({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        birthDate: form.birthDate,
        sex: form.sex,
        heightCm: Number(form.heightCm),
        weightKg: Number(form.weightKg),
        activityLevel: form.activityLevel,
      })
      await login(form.email, form.password) // registration does not log you in: do it for the user
      navigate('/preferences', { replace: true })
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setErrors({ email: 'This email is already registered' })
      } else if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setErrors(err.fieldErrors)
      } else {
        setFormError(err instanceof Error ? err.message : 'Something went wrong')
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="card">
      <h1>Create your account</h1>
      <p className="muted">We use these details to estimate your calorie needs.</p>
      <form onSubmit={handleSubmit} noValidate>
        <div className="grid-2">
          <Field label="Full name" error={errors.fullName}>
            {(p) => <input {...p} value={form.fullName} onChange={(e) => set('fullName')(e.target.value)} autoComplete="name" required />}
          </Field>
          <Field label="Email" error={errors.email}>
            {(p) => <input {...p} type="email" value={form.email} onChange={(e) => set('email')(e.target.value)} autoComplete="email" required />}
          </Field>
        </div>
        <Field label="Password" error={errors.password} hint="8 to 72 characters">
          {(p) => (
            <input {...p} type="password" value={form.password} onChange={(e) => set('password')(e.target.value)} autoComplete="new-password" required />
          )}
        </Field>
        <div className="grid-2">
          <Field label="Date of birth" error={errors.birthDate}>
            {(p) => <input {...p} type="date" max={today} value={form.birthDate} onChange={(e) => set('birthDate')(e.target.value)} required />}
          </Field>
          <Field label="Sex" error={errors.sex}>
            {(p) => (
              <select {...p} value={form.sex} onChange={(e) => set('sex')(e.target.value as Sex)}>
                {SEX_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            )}
          </Field>
        </div>
        <div className="grid-2">
          <Field label="Height (cm)" error={errors.heightCm}>
            {(p) => (
              <input {...p} type="number" inputMode="numeric" min={100} max={250} value={form.heightCm} onChange={(e) => set('heightCm')(e.target.value)} required />
            )}
          </Field>
          <Field label="Weight (kg)" error={errors.weightKg}>
            {(p) => (
              <input {...p} type="number" inputMode="decimal" step="0.1" min={30} max={300} value={form.weightKg} onChange={(e) => set('weightKg')(e.target.value)} required />
            )}
          </Field>
        </div>
        <Field label="Activity level" error={errors.activityLevel}>
          {(p) => (
            <select {...p} value={form.activityLevel} onChange={(e) => set('activityLevel')(e.target.value as ActivityLevel)}>
              {ACTIVITY_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          )}
        </Field>
        {formError && (
          <p className="form-error" role="alert">
            {formError}
          </p>
        )}
        <button type="submit" className="primary" disabled={busy}>
          {busy ? 'Creating account…' : 'Create account'}
        </button>
      </form>
      <p className="muted small">
        Already registered? <Link to="/login">Log in</Link>.
      </p>
    </section>
  )
}
