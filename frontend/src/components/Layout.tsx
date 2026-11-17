import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function Layout() {
  const { session, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <>
      <header className="topbar">
        <NavLink to="/" className="brand">
          <span className="brand-mark" aria-hidden="true">
            ●
          </span>
          Wellness
        </NavLink>
        <nav className="nav">
          {session ? (
            <>
              <NavLink to="/preferences">My preferences</NavLink>
              <NavLink to="/recommendation">My plan</NavLink>
              {session.role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
              <span className="badge" title="Your role, read from the access token">
                {session.role}
              </span>
              <button type="button" className="link-button" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login">Log in</NavLink>
              <NavLink to="/register">Create account</NavLink>
            </>
          )}
        </nav>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  )
}
