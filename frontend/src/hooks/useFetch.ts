import { useEffect, useState } from 'react'

interface FetchState<T> {
  data: T | null
  error: Error | null
  loading: boolean
}

/**
 * Runs `load` when `deps` change. The `cancelled` flag stops a slow, outdated response from overwriting a newer one
 * (a race condition) and from setting state after the component has unmounted.
 * State is only set when a request settles, so a page that must start from scratch when its input changes
 * should be given a `key` by its parent (a new key = a new component instance = a fresh "loading" state).
 */
export function useFetch<T>(load: () => Promise<T>, deps: unknown[]): FetchState<T> {
  const [state, setState] = useState<FetchState<T>>({ data: null, error: null, loading: true })

  useEffect(() => {
    let cancelled = false
    load().then(
      (data) => {
        if (!cancelled) setState({ data, error: null, loading: false })
      },
      (error: unknown) => {
        if (!cancelled) setState({ data: null, error: error instanceof Error ? error : new Error(String(error)), loading: false })
      },
    )
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- `load` is recreated on every render; the caller lists the real inputs in deps
  }, deps)

  return state
}
