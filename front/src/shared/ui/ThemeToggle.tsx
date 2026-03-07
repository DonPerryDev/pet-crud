import type { ReactNode } from 'react'
import { useTheme } from '@/shared/theme'
import type { Theme } from '@/shared/theme'

const nextTheme: Record<Theme, Theme> = {
  light: 'dark',
  dark: 'system',
  system: 'light',
}

const themeIcon: Record<Theme, string> = {
  light: 'light_mode',
  dark: 'dark_mode',
  system: 'computer',
}

const themeLabel: Record<Theme, string> = {
  light: 'Light',
  dark: 'Dark',
  system: 'System',
}

export function ThemeToggle(): ReactNode {
  const { theme, setTheme } = useTheme()

  return (
    <button
      type="button"
      onClick={(): void => setTheme(nextTheme[theme])}
      className="flex items-center justify-center size-10 rounded-full hover:bg-primary/20 transition-colors"
      aria-label={`Theme: ${themeLabel[theme]}. Click to switch.`}
      title={themeLabel[theme]}
    >
      <span className="material-symbols-outlined text-xl">
        {themeIcon[theme]}
      </span>
    </button>
  )
}
