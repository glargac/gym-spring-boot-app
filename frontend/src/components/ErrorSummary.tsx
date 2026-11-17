interface ErrorSummaryProps {
  formError: string | null
  /** Field errors from the backend. Keys such as "days[0].exercises[1].name" have no input of their own to sit under. */
  errors: Record<string, string>
  /** Which keys are already shown next to their own input (a list, or a test), so they are not repeated here. */
  shownInline?: string[] | ((key: string) => boolean)
}

export function ErrorSummary({ formError, errors, shownInline = [] }: ErrorSummaryProps) {
  const isInline = (key: string) => (typeof shownInline === 'function' ? shownInline(key) : shownInline.includes(key))
  const rest = Object.entries(errors).filter(([key]) => !isInline(key))
  if (!formError && rest.length === 0) return null
  return (
    <div className="form-error" role="alert">
      {formError && <p style={{ margin: 0 }}>{formError}</p>}
      {rest.length > 0 && (
        <ul className="error-list">
          {rest.map(([key, message]) => (
            <li key={key}>
              <code>{key}</code>: {message}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
