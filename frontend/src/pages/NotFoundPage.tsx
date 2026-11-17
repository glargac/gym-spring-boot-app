import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="card narrow">
      <h1>Page not found</h1>
      <p className="muted">That address does not exist.</p>
      <Link to="/" className="button">
        Back to the start
      </Link>
    </section>
  )
}
