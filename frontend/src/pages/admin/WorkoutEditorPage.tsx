import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, ApiError } from '../../api/client'
import type { Goal, TrainingLocation, WorkoutTemplateAdmin } from '../../api/types'
import { ErrorSummary } from '../../components/ErrorSummary'
import { Field } from '../../components/Field'
import { useFetch } from '../../hooks/useFetch'

interface EditorData {
  goals: Goal[]
  locations: TrainingLocation[]
  existing: WorkoutTemplateAdmin | null
}

export function WorkoutEditorPage() {
  const { id } = useParams()
  const templateId = id ? Number(id) : null

  const { data, error, loading } = useFetch<EditorData>(async () => {
    const [goals, locations, existing] = await Promise.all([
      api.goals(),
      api.trainingLocations(),
      templateId === null ? Promise.resolve(null) : api.workoutTemplate(templateId),
    ])
    return { goals, locations, existing }
  }, [templateId])

  if (loading) return <p className="muted">Loading…</p>
  if (error || !data) return <p className="form-error">{error?.message ?? 'Could not load the editor'}</p>

  // key: opening a different template mounts a fresh form instead of reusing the previous one's state
  return <WorkoutForm key={templateId ?? 'new'} data={data} templateId={templateId} />
}

// The form keeps every input as text (that is what inputs give us) and converts to numbers once, on submit.
interface ExerciseForm {
  name: string
  sets: string
  reps: string
}
interface DayForm {
  focus: string
  exercises: ExerciseForm[]
}

const newExercise = (): ExerciseForm => ({ name: '', sets: '3', reps: '10' })
const newDay = (): DayForm => ({ focus: '', exercises: [newExercise()] })

function WorkoutForm({ data, templateId }: { data: EditorData; templateId: number | null }) {
  const navigate = useNavigate()
  const existing = data.existing

  const [goalCode, setGoalCode] = useState(existing?.goalCode ?? data.goals[0]?.code ?? '')
  const [locationCode, setLocationCode] = useState(existing?.locationCode ?? data.locations[0]?.code ?? '')
  const [name, setName] = useState(existing?.name ?? '')
  const [days, setDays] = useState<DayForm[]>(
    existing
      ? existing.days.map((d) => ({
          focus: d.focus,
          exercises: d.exercises.map((e) => ({ name: e.name, sets: String(e.sets), reps: e.reps })),
        }))
      : [newDay(), newDay(), newDay()],
  )
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  // The backend demands exactly daysPerWeek days numbered 1..n, so the number of day blocks IS the days-per-week value.
  function setDayCount(count: number) {
    setDays((current) =>
      count > current.length
        ? [...current, ...Array.from({ length: count - current.length }, newDay)]
        : current.slice(0, count),
    )
  }

  function updateDay(index: number, patch: Partial<DayForm>) {
    setDays((current) => current.map((d, i) => (i === index ? { ...d, ...patch } : d)))
  }

  function updateExercise(dayIndex: number, exIndex: number, patch: Partial<ExerciseForm>) {
    setDays((current) =>
      current.map((d, i) =>
        i === dayIndex ? { ...d, exercises: d.exercises.map((e, j) => (j === exIndex ? { ...e, ...patch } : e)) } : d,
      ),
    )
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors({})
    setFormError(null)
    setBusy(true)
    const body = {
      goalCode,
      locationCode,
      daysPerWeek: days.length,
      name: name.trim(),
      days: days.map((d, i) => ({
        dayIndex: i + 1,
        focus: d.focus.trim(),
        exercises: d.exercises.map((e) => ({ name: e.name.trim(), sets: Number(e.sets), reps: e.reps.trim() })),
      })),
    }
    try {
      if (templateId === null) await api.createWorkoutTemplate(body)
      else await api.updateWorkoutTemplate(templateId, body)
      navigate('/admin/workouts')
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
        <h2>{templateId === null ? 'New workout template' : 'Edit workout template'}</h2>
        <Link to="/admin/workouts">Back to the list</Link>
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
          <Field label="Location" error={errors.locationCode}>
            {(p) => (
              <select {...p} value={locationCode} onChange={(e) => setLocationCode(e.target.value)}>
                {data.locations.map((l) => (
                  <option key={l.code} value={l.code}>
                    {l.name}
                  </option>
                ))}
              </select>
            )}
          </Field>
          <Field label="Days per week" error={errors.daysPerWeek} hint="Adds or removes day blocks below">
            {(p) => (
              <input
                {...p}
                type="number"
                min={1}
                max={7}
                value={days.length}
                onChange={(e) => setDayCount(Math.min(7, Math.max(1, Number(e.target.value) || 1)))}
              />
            )}
          </Field>
        </div>

        {days.map((day, dayIndex) => (
          <fieldset key={dayIndex} className="editor-block">
            <legend>Day {dayIndex + 1}</legend>
            <Field label="Focus" error={errors[`days[${dayIndex}].focus`]}>
              {(p) => <input {...p} value={day.focus} onChange={(e) => updateDay(dayIndex, { focus: e.target.value })} required />}
            </Field>
            {day.exercises.map((exercise, exIndex) => (
              <div key={exIndex} className="exercise-row">
                <Field label="Exercise" error={errors[`days[${dayIndex}].exercises[${exIndex}].name`]}>
                  {(p) => (
                    <input {...p} value={exercise.name} onChange={(e) => updateExercise(dayIndex, exIndex, { name: e.target.value })} required />
                  )}
                </Field>
                <Field label="Sets" error={errors[`days[${dayIndex}].exercises[${exIndex}].sets`]}>
                  {(p) => (
                    <input {...p} type="number" min={1} max={20} value={exercise.sets} onChange={(e) => updateExercise(dayIndex, exIndex, { sets: e.target.value })} />
                  )}
                </Field>
                <Field label="Reps or time" error={errors[`days[${dayIndex}].exercises[${exIndex}].reps`]}>
                  {(p) => (
                    <input {...p} value={exercise.reps} onChange={(e) => updateExercise(dayIndex, exIndex, { reps: e.target.value })} />
                  )}
                </Field>
                <button
                  type="button"
                  className="danger-text"
                  disabled={day.exercises.length === 1}
                  aria-label={`Remove exercise ${exIndex + 1} from day ${dayIndex + 1}`}
                  onClick={() => updateDay(dayIndex, { exercises: day.exercises.filter((_, j) => j !== exIndex) })}
                >
                  Remove
                </button>
              </div>
            ))}
            <button type="button" onClick={() => updateDay(dayIndex, { exercises: [...day.exercises, newExercise()] })}>
              Add exercise
            </button>
          </fieldset>
        ))}

        <ErrorSummary
          formError={formError}
          errors={errors}
          shownInline={(key) =>
            ['name', 'goalCode', 'locationCode', 'daysPerWeek'].includes(key) ||
            /^days\[\d+\]\.(focus|exercises\[\d+\]\.(name|sets|reps))$/.test(key)
          }
        />
        <div className="actions">
          <button type="submit" className="primary" disabled={busy}>
            {busy ? 'Saving…' : 'Save template'}
          </button>
          <Link to="/admin/workouts" className="button">
            Cancel
          </Link>
        </div>
      </form>
    </section>
  )
}
