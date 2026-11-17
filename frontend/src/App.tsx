import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthProvider'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PreferencesPage } from './pages/PreferencesPage'
import { RecommendationPage } from './pages/RecommendationPage'
import { RegisterPage } from './pages/RegisterPage'
import { AdminCatalogsPage } from './pages/admin/AdminCatalogsPage'
import { AdminLayout } from './pages/admin/AdminLayout'
import { AdminMealPlansPage } from './pages/admin/AdminMealPlansPage'
import { AdminWorkoutsPage } from './pages/admin/AdminWorkoutsPage'
import { MealPlanEditorPage } from './pages/admin/MealPlanEditorPage'
import { WorkoutEditorPage } from './pages/admin/WorkoutEditorPage'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<HomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="preferences" element={<PreferencesPage />} />
              <Route path="recommendation" element={<RecommendationPage />} />
              <Route path="admin" element={<AdminLayout />}>
                <Route index element={<Navigate to="catalogs" replace />} />
                <Route path="catalogs" element={<AdminCatalogsPage />} />
                <Route path="workouts" element={<AdminWorkoutsPage />} />
                <Route path="workouts/new" element={<WorkoutEditorPage />} />
                <Route path="workouts/:id" element={<WorkoutEditorPage />} />
                <Route path="meal-plans" element={<AdminMealPlansPage />} />
                <Route path="meal-plans/new" element={<MealPlanEditorPage />} />
                <Route path="meal-plans/:id" element={<MealPlanEditorPage />} />
              </Route>
            </Route>
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
