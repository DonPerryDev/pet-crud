import type { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'
import { Footer } from './Footer'

export function RootLayout(): ReactNode {
  return (
    <div className="relative flex min-h-screen flex-col overflow-x-hidden">
      <div className="layout-container flex h-full grow flex-col">
        <Navbar />
        <Outlet />
        <Footer />
      </div>
    </div>
  )
}
