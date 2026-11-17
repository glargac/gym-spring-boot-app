import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api/client'
import { ConfirmDelete } from '../../components/ConfirmDelete'
import { useFetch } from '../../hooks/useFetch'

export function AdminMealPlansPage() {
  const [version, setVersion] = useState(0)
  const { data, error, loading } = useFetch(() => api.mealPlans(), [version])
  const [message, setMessage] = useState<string | null>(null)

  async function handleDelete(id: number) {
    setMessage(null)
    try {
      await api.deleteMealPlan(id)
      setVersion((v) => v + 1)
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Could not delete')
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h2>Meal plans</h2>
        <Link to="/admin/meal-plans/new" className="button primary">
          New meal plan
        </Link>
      </div>
      <p className="muted small">
        The recommendation picks a plan by goal and diet. Diet is a hard rule: a user is never given a plan for a different diet.
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
                <th>Diet</th>
                <th>Adjustment</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {data.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>
                    <span className="chip">{p.goalCode}</span>
                  </td>
                  <td>
                    <span className="chip">{p.dietTypeCode}</span>
                  </td>
                  <td>{p.kcalAdjust > 0 ? `+${p.kcalAdjust}` : p.kcalAdjust} kcal</td>
                  <td className="row-actions">
                    <Link to={`/admin/meal-plans/${p.id}`} className="button">
                      Edit
                    </Link>
                    <ConfirmDelete onConfirm={() => handleDelete(p.id)} />
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
