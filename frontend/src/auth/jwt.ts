import type { Role } from '../api/types'

export interface Session {
  token: string
  userId: number
  role: Role
  expiresAt: number // epoch milliseconds
}

/**
 * Reads the claims of a JWT WITHOUT verifying the signature. That is fine here because the result only drives
 * the UI (which links to show, when to log out). The server verifies the signature on every request, so editing
 * the token in the browser can never grant real access.
 */
export function sessionFromToken(token: string): Session | null {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(payload.length / 4) * 4, '=')
    const claims = JSON.parse(atob(base64)) as { sub?: string; role?: string; exp?: number }
    const userId = Number(claims.sub)
    if (!Number.isInteger(userId) || !claims.exp) return null
    if (claims.role !== 'ADMIN' && claims.role !== 'USER') return null
    return { token, userId, role: claims.role, expiresAt: claims.exp * 1000 }
  } catch {
    return null
  }
}

export function isExpired(session: Session): boolean {
  return Date.now() >= session.expiresAt
}
