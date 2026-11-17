import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

/**
 * Route guard: renders the nested routes only for a logged-in user. This is a UX convenience, NOT security:
 * anyone can change client code, so the real checks (token signature, ownership, role) stay on the server.
 */
export function ProtectedRoute() {
  const { session } = useAuth()
  const location = useLocation()
  if (!session) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}
