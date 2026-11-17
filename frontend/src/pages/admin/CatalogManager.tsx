import { useState, type FormEvent } from 'react'
import { api, ApiError } from '../../api/client'
import type { CatalogResource } from '../../api/types'
import { ConfirmDelete } from '../../components/ConfirmDelete'
import { Field } from '../../components/Field'
import { useFetch } from '../../hooks/useFetch'

interface CatalogManagerProps {
  title: string
  resource: CatalogResource
  withDescription: boolean
}

const EMPTY = { code: '', name: '', description: '' }

/** One reusable list + add/edit form. The three catalogs only differ in their URL and whether they have a description. */
export function CatalogManager({ title, resource, withDescription }: CatalogManagerProps) {
  const [version, setVersion] = useState(0) // bumping it re-runs the fetch below
  const { data, error, loading } = useFetch(() => api.catalog(resource), [resource, version])

  const [form, setForm] = useState(EMPTY)
  const [editingCode, setEditingCode] = useState<string | null>(null) // null = adding a new item
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  function startEdit(code: string, name: string, description: string | null | undefined) {
    setEditingCode(code)
    setForm({ code, name, description: description ?? '' })
    setErrors({})
    setMessage(null)
  }

  function reset() {
    setEditingCode(null)
    setForm(EMPTY)
    setErrors({})
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setErrors({})
    setMessage(null)
    setBusy(true)
    try {
      // The server wants codes like LOSE_WEIGHT; normalising here saves the admin a round trip.
      const code = form.code.trim().toUpperCase().replace(/\s+/g, '_')
      await api.saveCatalogItem(resource, code, {
        name: form.name.trim(),
        description: withDescription ? form.description.trim() || null : null,
      })
      reset()
      setVersion((v) => v + 1)
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) setErrors(err.fieldErrors)
      else setMessage(err instanceof Error ? err.message : 'Something went wrong')
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete(code: string) {
    setMessage(null)
    try {
      await api.deleteCatalogItem(resource, code)
      if (editingCode === code) reset()
      setVersion((v) => v + 1)
    } catch (err) {
      // 409 = still referenced by a user, template or plan: the server's message explains it
      setMessage(err instanceof Error ? err.message : 'Could not delete')
    }
  }

  return (
    <section className="card">
      <h2>{title}</h2>

      {loading && <p className="muted">Loading…</p>}
      {error && <p className="form-error">{error.message}</p>}

      {data && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                {withDescription && <th>Description</th>}
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {data.map((item) => (
                <tr key={item.code}>
                  <td>
                    <code>{item.code}</code>
                  </td>
                  <td>{item.name}</td>
                  {withDescription && <td className="muted">{item.description}</td>}
                  <td className="row-actions">
                    <button type="button" onClick={() => startEdit(item.code, item.name, item.description)}>
                      Edit
                    </button>
                    <ConfirmDelete onConfirm={() => handleDelete(item.code)} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {message && (
        <p className="form-error" role="alert">
          {message}
        </p>
      )}

      <form onSubmit={handleSubmit} noValidate className="inline-form">
        <h3>{editingCode ? `Edit ${editingCode}` : 'Add new'}</h3>
        <div className={withDescription ? 'grid-3' : 'grid-2'}>
          <Field label="Code" error={errors.code} hint={editingCode ? 'The code cannot be changed' : 'Capitals, digits and _ (e.g. KEEP_FIT)'}>
            {(p) => (
              <input {...p} value={form.code} disabled={editingCode !== null} onChange={(e) => setForm({ ...form, code: e.target.value })} required />
            )}
          </Field>
          <Field label="Name" error={errors.name}>
            {(p) => <input {...p} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />}
          </Field>
          {withDescription && (
            <Field label="Description" error={errors.description}>
              {(p) => <input {...p} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />}
            </Field>
          )}
        </div>
        <div className="actions">
          <button type="submit" className="primary" disabled={busy || !form.code.trim() || !form.name.trim()}>
            {busy ? 'Saving…' : editingCode ? 'Save changes' : 'Add'}
          </button>
          {editingCode && (
            <button type="button" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>
    </section>
  )
}
