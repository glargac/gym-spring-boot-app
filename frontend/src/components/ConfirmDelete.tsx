import { useState } from 'react'

interface ConfirmDeleteProps {
  onConfirm: () => Promise<void>
  label?: string
}

/**
 * Two-step delete done inline in the page. We avoid window.confirm(): it is a blocking browser dialog that cannot
 * be styled, is awkward for assistive tech, and freezes automated browser tooling.
 */
export function ConfirmDelete({ onConfirm, label = 'Delete' }: ConfirmDeleteProps) {
  const [asking, setAsking] = useState(false)
  const [busy, setBusy] = useState(false)

  if (!asking) {
    return (
      <button type="button" className="danger-text" onClick={() => setAsking(true)}>
        {label}
      </button>
    )
  }

  async function confirm() {
    setBusy(true)
    try {
      await onConfirm()
    } finally {
      setBusy(false)
      setAsking(false)
    }
  }

  return (
    <span className="confirm">
      <span className="muted small">Sure?</span>
      <button type="button" className="danger" disabled={busy} onClick={confirm}>
        {busy ? 'Deleting…' : 'Yes, delete'}
      </button>
      <button type="button" onClick={() => setAsking(false)} disabled={busy}>
        Cancel
      </button>
    </span>
  )
}
