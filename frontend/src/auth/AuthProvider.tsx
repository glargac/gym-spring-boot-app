import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, setAccessToken, setUnauthorizedHandler } from '../api/client'
import { AuthContext } from './AuthContext'
import { isExpired, sessionFromToken, type Session } from './jwt'

const STORAGE_KEY = 'wellness.token'

// Storage can throw (private mode, blocked site data), so every access is wrapped. The app still works without it.
function readStoredSession(): Session | null {
  try {
    const token = localStorage.getItem(STORAGE_KEY)
    const session = token ? sessionFromToken(token) : null
    return session && !isExpired(session) ? session : null
  } catch {
    return null
  }
}

function storeToken(token: string | null): void {
  try {
    if (token) localStorage.setItem(STORAGE_KEY, token)
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* ignore: the session just won't survive a reload */
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    const stored = readStoredSession()
    setAccessToken(stored?.token ?? null) // idempotent, so running twice under StrictMode is harmless
    return stored
  })

  const logout = useCallback(() => {
    setAccessToken(null)
    storeToken(null)
    setSession(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
  }, [logout])

  // Log out exactly when the token expires, instead of waiting for the next request to fail with a 401.
  useEffect(() => {
    if (!session) return
    const timer = setTimeout(logout, Math.max(session.expiresAt - Date.now(), 0))
    return () => clearTimeout(timer)
  }, [session, logout])

  const login = useCallback(async (email: string, password: string) => {
    const { accessToken } = await api.login(email, password)
    const next = sessionFromToken(accessToken)
    if (!next) throw new Error('The server returned an unreadable token')
    setAccessToken(accessToken)
    storeToken(accessToken)
    setSession(next)
  }, [])

  const value = useMemo(() => ({ session, login, logout }), [session, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
