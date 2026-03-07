import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from '@/shared/i18n'

export function AddPetCard(): ReactNode {
  const { t } = useTranslation()
  const navigate = useNavigate()

  return (
    <button
      onClick={() => navigate('/pets/new')}
      className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 dark:border-gray-600 bg-transparent p-6 text-center hover:border-primary hover:bg-primary/5 dark:hover:bg-primary/10 transition-all cursor-pointer min-h-[240px]"
    >
      <span className="material-symbols-outlined text-4xl text-sage-muted/60 mb-2">
        add_circle
      </span>
      <span className="text-sage-dark dark:text-white text-sm font-bold">
        {t('pets.addAnotherPet')}
      </span>
      <span className="text-sage-muted text-xs mt-1">
        {t('pets.addAnotherPetDesc')}
      </span>
    </button>
  )
}
