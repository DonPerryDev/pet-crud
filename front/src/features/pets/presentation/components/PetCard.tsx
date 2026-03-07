import { useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import type { PetCardViewModel } from '../types'
import { useTranslation } from '@/shared/i18n'
import { ConfirmDeleteModal } from '@/shared/ui/ConfirmDeleteModal'
import { useDeletePet } from '../hooks/use-delete-pet'
import { PetAvatar } from './PetAvatar'
import { PetTagBadge } from './PetTagBadge'

interface PetCardProps {
  pet: PetCardViewModel
}

export function PetCard({ pet }: PetCardProps): ReactNode {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [showConfirm, setShowConfirm] = useState(false)

  const { isDeleting, deletePet } = useDeletePet(() => {
    toast.success(t('pets.delete.successMessage'))
    setShowConfirm(false)
  })

  async function handleConfirmDelete(): Promise<void> {
    try {
      await deletePet(pet.id)
    } catch {
      toast.error(t('pets.delete.errorMessage'))
    }
  }

  return (
    <>
      <div className="group relative flex flex-col items-center rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800/50 p-6 shadow-sm hover:shadow-md hover:border-primary/50 transition-all cursor-pointer">
        {/* Delete button */}
        <button
          type="button"
          onClick={(e) => { e.stopPropagation(); setShowConfirm(true) }}
          className="absolute top-3 right-3 flex items-center justify-center size-8 rounded-full text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all opacity-0 group-hover:opacity-100"
          aria-label={`Delete ${pet.name}`}
        >
          <span className="material-symbols-outlined text-base">delete</span>
        </button>

        <PetAvatar name={pet.name} species={pet.species} photoUrl={pet.photoUrl} />

        <h3 className="text-sage-dark dark:text-white text-lg font-bold leading-tight">
          {pet.name}
        </h3>

        <p className="text-sage-muted text-sm mt-1">
          {pet.breed ?? pet.species}
          {pet.age !== null && <> &middot; {pet.age} {t('pets.yearsOld')}</>}
        </p>

        <div className="flex flex-wrap justify-center gap-1.5 mt-3">
          {pet.tags.map((tag) => (
            <PetTagBadge key={tag.label} tag={tag} />
          ))}
        </div>

        <button
          onClick={() => navigate(`/pets/${pet.id}`)}
          className="mt-4 flex items-center gap-1.5 text-sm font-medium text-primary hover:text-primary/80 transition-colors"
        >
          <span className="material-symbols-outlined text-base">arrow_forward</span>
          {t('pets.viewDetails')}
        </button>
      </div>

      {showConfirm && (
        <ConfirmDeleteModal
          title={t('pets.delete.title')}
          description={`${t('pets.delete.confirmQuestion')} "${pet.name}"? ${t('pets.delete.warning')}`}
          confirmLabel={t('pets.delete.confirm')}
          cancelLabel={t('pets.delete.cancel')}
          isDeleting={isDeleting}
          onConfirm={handleConfirmDelete}
          onCancel={() => setShowConfirm(false)}
        />
      )}
    </>
  )
}
