import type { ReactNode } from 'react'
import { Routes, Route } from 'react-router-dom'
import { RootLayout } from '@/shared/ui/layout/RootLayout'
import { HomePage } from '@/features/home'
import { PetsPage, RegisterPetPage, PetDetailPage } from '@/features/pets'
import { AboutPage } from '@/features/about'

export function AppRouter(): ReactNode {
  return (
    <Routes>
      <Route element={<RootLayout />}>
        <Route index element={<HomePage />} />
        <Route path="pets" element={<PetsPage />} />
        <Route path="pets/new" element={<RegisterPetPage />} />
        <Route path="pets/:petId" element={<PetDetailPage />} />
        <Route path="about" element={<AboutPage />} />
      </Route>
    </Routes>
  )
}
