import type { ReactNode } from 'react'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'sonner'
import { ThemeInit } from '@/shared/theme'

interface AppProvidersProps {
  children: ReactNode
}

export function AppProviders({ children }: AppProvidersProps): ReactNode {
  return (
    <BrowserRouter>
      <ThemeInit />
      <Toaster position="top-right" richColors />
      {children}
    </BrowserRouter>
  )
}
