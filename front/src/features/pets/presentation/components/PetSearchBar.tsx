import type { ReactNode } from 'react'
import { useTranslation } from '@/shared/i18n'
import { usePetStore } from '../store/pet-store'
import type { SpeciesFilter } from '../types'

const SPECIES_FILTERS: { key: SpeciesFilter; labelKey: 'pets.filterAll' | 'pets.filterDogs' | 'pets.filterCats' }[] = [
  { key: 'ALL', labelKey: 'pets.filterAll' },
  { key: 'DOG', labelKey: 'pets.filterDogs' },
  { key: 'CAT', labelKey: 'pets.filterCats' },
]

export function PetSearchBar(): ReactNode {
  const { t } = useTranslation()
  const searchQuery = usePetStore((s) => s.searchQuery)
  const speciesFilter = usePetStore((s) => s.speciesFilter)
  const setSearchQuery = usePetStore((s) => s.setSearchQuery)
  const setSpeciesFilter = usePetStore((s) => s.setSpeciesFilter)

  return (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
      <div className="relative flex-1">
        <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-sage-muted text-xl">
          search
        </span>
        <input
          type="text"
          value={searchQuery}
          onChange={(e): void => setSearchQuery(e.target.value)}
          placeholder={t('pets.searchPlaceholder')}
          className="w-full rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 py-2.5 pl-10 pr-4 text-sm text-sage-dark dark:text-white placeholder:text-sage-muted focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition-colors"
        />
      </div>

      <div className="flex gap-1 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-1">
        {SPECIES_FILTERS.map(({ key, labelKey }) => (
          <button
            key={key}
            onClick={(): void => setSpeciesFilter(key)}
            className={`rounded-md px-3.5 py-1.5 text-sm font-medium transition-colors ${
              speciesFilter === key
                ? 'bg-primary text-sage-dark shadow-sm'
                : 'text-sage-muted hover:text-sage-dark dark:hover:text-white'
            }`}
          >
            {t(labelKey)}
          </button>
        ))}
      </div>
    </div>
  )
}
