import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/client'
import { ConfirmDelete } from '../../components/ConfirmDelete'
import { useFetch } from '../../hooks/useFetch'

export function AdminWorkoutsPage() {
  const [version, setVersion] = useState(0)
  const { data, error, loading } = useFetch(() => api.workoutTemplates(), [version])
  const [message, setMessage] = useState<string | null>(null)

  async function handleDelete(id: number) {
    setMessage(null)
    try {
      await api.deleteWorkoutTemplate(id)
      setVersion((v) => v + 1)
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Could not delete')
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h2>Workout templates</h2>
        <Link to="/admin/workouts/new" className="button primary">
          New template
        </Link>
      </div>
      <p className="muted small">
        The recommendation picks a template by goal and location, then the one closest to the user&apos;s days per week.
      </p>

      {loading && <p className="muted">Loading…</p>}
      {error && <p className="form-error">{error.message}</p>}
      {message && (
        <p className="form-error" role="alert">
          {message}
        </p>
      )}

      {data && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Goal</th>
                <th>Location</th>
                <th>Days</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {data.map((t) => (
                <tr key={t.id}>
                  <td>{t.name}</td>
                  <td>
                    <span className="chip">{t.goalCode}</span>
                  </td>
                  <td>
                    <span className="chip">{t.locationCode}</span>
                  </td>
                  <td>{t.daysPerWeek}</td>
                  <td className="row-actions">
                    <Link to={`/admin/workouts/${t.id}`} className="button">
                      Edit
                    </Link>
                    <ConfirmDelete onConfirm={() => handleDelete(t.id)} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
