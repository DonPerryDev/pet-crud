import { useCallback } from 'react'
import { useI18nStore } from './i18n-store'
import { en } from './locales/en'
import { es } from './locales/es'
import type { Locale, TranslationDictionary, TranslationKey } from './types'

const dictionaries: Record<Locale, TranslationDictionary> = { en, es }

function resolve(dict: TranslationDictionary, key: string): string {
  const parts = key.split('.')
  let current: unknown = dict
  for (const part of parts) {
    if (current === null || current === undefined || typeof current !== 'object') {
      return key
    }
    current = (current as Record<string, unknown>)[part]
  }
  return typeof current === 'string' ? current : key
}

interface UseTranslationReturn {
  t: (key: TranslationKey) => string
  locale: Locale
  setLocale: (locale: Locale) => void
}

export function useTranslation(): UseTranslationReturn {
  const locale = useI18nStore((s) => s.locale)
  const setLocale = useI18nStore((s) => s.setLocale)
  const dict = dictionaries[locale]

  const t = useCallback(
    (key: TranslationKey): string => resolve(dict, key),
    [dict],
  )

  return { t, locale, setLocale }
}
