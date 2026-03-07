import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from '@/shared/i18n'
import { usePets } from './hooks/use-pets'
import { PetCard } from './components/PetCard'
import { AddPetCard } from './components/AddPetCard'
import { PetSearchBar } from './components/PetSearchBar'

export function PetsPage(): ReactNode {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { pets, isLoading, error } = usePets()

  return (
    <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
      <div className="flex flex-col max-w-[1200px] flex-1 gap-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-sage-dark dark:text-white text-3xl font-black leading-tight tracking-tight">
              {t('pets.title')}
            </h1>
            <p className="text-sage-muted text-sm mt-1">
              {t('pets.subtitle')}
            </p>
          </div>
          <button
            onClick={() => navigate('/pets/new')}
            className="flex cursor-pointer items-center justify-center rounded-full h-10 px-5 bg-primary text-sage-dark text-sm font-bold border border-transparent hover:border-sage-muted transition-all shrink-0"
          >
            <span className="material-symbols-outlined mr-2 text-base">add</span>
            {t('pets.registerNewPet')}
          </button>
        </div>

        {/* Search + Filter */}
        <PetSearchBar />

        {/* Content */}
        {isLoading && (
          <p className="text-sage-muted text-sm">{t('pets.loading')}</p>
        )}

        {error && (
          <p className="text-red-500 text-sm">{error}</p>
        )}

        {!isLoading && !error && pets.length === 0 && (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <span className="material-symbols-outlined text-6xl text-sage-muted/40 mb-4">pets</span>
            <h2 className="text-sage-dark dark:text-white text-xl font-bold mb-2">{t('pets.noPetsTitle')}</h2>
            <p className="text-sage-muted text-sm max-w-[300px]">
              {t('pets.noPetsDescription')}
            </p>
          </div>
        )}

        {!isLoading && !error && pets.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {pets.map((pet) => (
              <PetCard key={pet.id} pet={pet} />
            ))}
            <AddPetCard />
          </div>
        )}
      </div>
    </section>
  )
}
