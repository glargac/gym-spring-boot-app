// Mirrors the backend DTOs (records in com.wellness.*). A string-literal union is the TypeScript
// counterpart of a Java enum: it exists only at compile time, so there is no runtime cost.

export type Role = 'ADMIN' | 'USER'
export type Sex = 'MALE' | 'FEMALE' | 'OTHER'
export type ActivityLevel = 'SEDENTARY' | 'LIGHT' | 'MODERATE' | 'ACTIVE'
export type BmiCategory = 'UNDERWEIGHT' | 'NORMAL' | 'OVERWEIGHT' | 'OBESE'

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
}

export interface RegisterRequest {
  email: string
  password: string
  fullName: string
  birthDate: string // ISO date, yyyy-MM-dd
  sex: Sex
  heightCm: number
  weightKg: number
  activityLevel: ActivityLevel
}

export interface UserResponse {
  id: number
  email: string
  fullName: string
  role: Role
}

export interface Goal {
  code: string
  name: string
  description: string | null
}

export interface DietType {
  code: string
  name: string
  description: string | null
}

export interface TrainingLocation {
  code: string
  name: string
}

export interface PreferencesRequest {
  goalCode: string
  locationCode: string
  dietTypeCode: string
  daysPerWeek: number
  hoursPerDay: number
  excludedFoods: string | null
}

export interface PreferencesResponse extends PreferencesRequest {
  userId: number
}

export interface HealthMetrics {
  bmi: number
  category: BmiCategory
  bmr: number
  tdee: number
  advice: string
}

export interface ExerciseView {
  name: string
  sets: number
  reps: string
}

export interface DayView {
  dayIndex: number
  focus: string
  exercises: ExerciseView[]
}

export interface WorkoutView {
  name: string
  matchedBy: string
  daysPerWeek: number
  days: DayView[]
}

export interface MealView {
  type: string
  description: string
  kcal: number
}

export interface MealPlanView {
  name: string
  kcalAdjust: number
  totalKcal: number
  portionFactor: number
  meals: MealView[]
}

/** workout / mealPlan / targetKcal are null when the backend has nothing suitable; "notes" explains why. */
export interface Recommendation {
  metrics: HealthMetrics
  targetKcal: number | null
  workout: WorkoutView | null
  mealPlan: MealPlanView | null
  notes: string[]
}
