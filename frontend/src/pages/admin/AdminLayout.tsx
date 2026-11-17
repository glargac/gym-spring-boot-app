import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../auth/useAuth'

/**
 * Shell for every admin page. Showing "Admins only" here is a courtesy: a normal user who types /admin gets a
 * clear message instead of a wall of 403 errors. The real protection is the server, which rejects every
 * /api/admin/** call that does not carry an ADMIN token.
 */
export function AdminLayout() {
  const { session } = useAuth()

  if (session?.role !== 'ADMIN') {
    return (
      <section className="card narrow">
        <h1>Admins only</h1>
        <p className="muted">You need an administrator account to manage catalogs and plans.</p>
      </section>
    )
  }

  return (
    <div className="stack">
      <div>
        <h1>Administration</h1>
        <nav className="tabs" aria-label="Admin sections">
          <NavLink to="/admin/catalogs">Catalogs</NavLink>
          <NavLink to="/admin/workouts">Workout templates</NavLink>
          <NavLink to="/admin/meal-plans">Meal plans</NavLink>
        </nav>
      </div>
      <Outlet />
    </div>
  )
}
