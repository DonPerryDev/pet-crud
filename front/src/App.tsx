import type { ReactNode } from 'react'
import { AppProviders } from './app/providers'
import { AppRouter } from './app/router'

export function App(): ReactNode {
  return (
    <AppProviders>
      <AppRouter />
    </AppProviders>
  )
}
