import { CatalogManager } from './CatalogManager'

export function AdminCatalogsPage() {
  return (
    <div className="stack">
      <p className="muted">
        These lists feed the preference form and every plan. An item that a user, template or plan still uses cannot be deleted.
      </p>
      <CatalogManager title="Goals" resource="goals" withDescription />
      <CatalogManager title="Diet types" resource="diet-types" withDescription />
      <CatalogManager title="Training locations" resource="training-locations" withDescription={false} />
    </div>
  )
}
