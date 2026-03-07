import { create } from 'zustand'
import type { Locale } from './types'

const STORAGE_KEY = 'pet-app-locale'

function detectLocale(): Locale {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'en' || stored === 'es') {
    return stored
  }
  const browserLang = navigator.language.slice(0, 2)
  return browserLang === 'es' ? 'es' : 'en'
}

interface I18nState {
  locale: Locale
  setLocale: (locale: Locale) => void
}

export const useI18nStore = create<I18nState>()((set) => ({
  locale: detectLocale(),
  setLocale: (locale): void => {
    localStorage.setItem(STORAGE_KEY, locale)
    set({ locale })
  },
}))
