import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function HomePage() {
  const { session } = useAuth()
  return (
    <section className="hero">
      <h1>A workout and a meal plan that fit your life.</h1>
      <p className="muted lead">
        Tell us your goal, where you train and what you eat. We calculate your energy needs and build a plan around them.
      </p>
      <div className="actions">
        {session ? (
          <Link to="/recommendation" className="button primary">
            See my plan
          </Link>
        ) : (
          <>
            <Link to="/register" className="button primary">
              Get started
            </Link>
            <Link to="/login" className="button">
              I already have an account
            </Link>
          </>
        )}
      </div>
    </section>
  )
}
