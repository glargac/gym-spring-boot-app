import type {
  CatalogItem,
  CatalogItemRequest,
  CatalogResource,
  DietType,
  Goal,
  MealPlanAdmin,
  MealPlanRequest,
  PreferencesRequest,
  PreferencesResponse,
  Recommendation,
  RegisterRequest,
  TokenResponse,
  TrainingLocation,
  UserResponse,
  WorkoutTemplateAdmin,
  WorkoutTemplateRequest,
} from './types'

const BASE_URL: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

/** Any non-2xx answer. fieldErrors comes from the backend's {"errors": {field: message}} body (400 validation). */
export class ApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// The API client must not import React or the auth context (that would create a cycle), so the auth layer pushes
// two things into it: the current token, and what to do when the server rejects that token (401).
// The token lives here, set synchronously by the auth layer, because child components fetch data in their
// effects BEFORE the parent's effects run: a value wired up in a parent effect would arrive too late.
let accessToken: string | null = null
let onUnauthorized: () => void = () => {}

export function setAccessToken(token: string | null): void {
  accessToken = token
}

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const token = accessToken
  const headers: Record<string, string> = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let response: Response
  try {
    response = await fetch(BASE_URL + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    // fetch only rejects on network problems (server down, CORS blocked, offline), never on HTTP error codes
    throw new ApiError(0, 'Cannot reach the server. Is the backend running?')
  }

  if (response.status === 204) return undefined as T

  const text = await response.text()
  let data: unknown = null
  try {
    data = text ? JSON.parse(text) : null
  } catch {
    data = null
  }

  if (!response.ok) {
    const problem = (data ?? {}) as { detail?: string; errors?: Record<string, string> }
    // 401 on a call that carried a token = the token expired or was rejected: end the session.
    // (A wrong password on /login carries no token, so it must not trigger this.)
    if (response.status === 401 && token) onUnauthorized()
    throw new ApiError(response.status, problem.detail ?? `Request failed (${response.status})`, problem.errors ?? {})
  }
  return data as T
}

export const api = {
  login: (email: string, password: string) => request<TokenResponse>('POST', '/api/auth/login', { email, password }),
  register: (body: RegisterRequest) => request<UserResponse>('POST', '/api/users', body),

  goals: () => request<Goal[]>('GET', '/api/goals'),
  dietTypes: () => request<DietType[]>('GET', '/api/diet-types'),
  trainingLocations: () => request<TrainingLocation[]>('GET', '/api/training-locations'),

  getPreferences: (userId: number) => request<PreferencesResponse>('GET', `/api/users/${userId}/preferences`),
  savePreferences: (userId: number, body: PreferencesRequest) =>
    request<PreferencesResponse>('PUT', `/api/users/${userId}/preferences`, body),
  recommendation: (userId: number) => request<Recommendation>('GET', `/api/users/${userId}/recommendation`),

  // ---- admin (the server rejects these for non-admins with 403; the UI only hides them) ----
  catalog: (resource: CatalogResource) => request<CatalogItem[]>('GET', `/api/${resource}`),
  saveCatalogItem: (resource: CatalogResource, code: string, body: CatalogItemRequest) =>
    request<CatalogItem>('PUT', `/api/admin/${resource}/${encodeURIComponent(code)}`, body),
  deleteCatalogItem: (resource: CatalogResource, code: string) =>
    request<void>('DELETE', `/api/admin/${resource}/${encodeURIComponent(code)}`),

  workoutTemplates: () => request<WorkoutTemplateAdmin[]>('GET', '/api/admin/workout-templates'),
  workoutTemplate: (id: number) => request<WorkoutTemplateAdmin>('GET', `/api/admin/workout-templates/${id}`),
  createWorkoutTemplate: (body: WorkoutTemplateRequest) =>
    request<WorkoutTemplateAdmin>('POST', '/api/admin/workout-templates', body),
  updateWorkoutTemplate: (id: number, body: WorkoutTemplateRequest) =>
    request<WorkoutTemplateAdmin>('PUT', `/api/admin/workout-templates/${id}`, body),
  deleteWorkoutTemplate: (id: number) => request<void>('DELETE', `/api/admin/workout-templates/${id}`),

  mealPlans: () => request<MealPlanAdmin[]>('GET', '/api/admin/meal-plans'),
  mealPlan: (id: number) => request<MealPlanAdmin>('GET', `/api/admin/meal-plans/${id}`),
  createMealPlan: (body: MealPlanRequest) => request<MealPlanAdmin>('POST', '/api/admin/meal-plans', body),
  updateMealPlan: (id: number, body: MealPlanRequest) =>
    request<MealPlanAdmin>('PUT', `/api/admin/meal-plans/${id}`, body),
  deleteMealPlan: (id: number) => request<void>('DELETE', `/api/admin/meal-plans/${id}`),
}
