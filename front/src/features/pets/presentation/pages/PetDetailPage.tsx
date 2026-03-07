import { useState } from 'react'
import type { ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useTranslation } from '@/shared/i18n'
import { ConfirmDeleteModal } from '@/shared/ui/ConfirmDeleteModal'
import { usePetDetail } from '../hooks/use-pet-detail'
import { useDeletePet } from '../hooks/use-delete-pet'
import { PetAvatar } from '../components/PetAvatar'

export function PetDetailPage(): ReactNode {
  const { petId } = useParams<{ petId: string }>()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { pet, isLoading, error } = usePetDetail(petId ?? '')
  const [showConfirm, setShowConfirm] = useState(false)

  const { isDeleting, deletePet } = useDeletePet(() => {
    toast.success(t('pets.delete.successMessage'))
    navigate('/pets')
  })

  async function handleConfirmDelete(): Promise<void> {
    if (!pet) return
    try {
      await deletePet(pet.id)
    } catch {
      toast.error(t('pets.delete.errorMessage'))
    }
  }

  if (isLoading) {
    return (
      <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
        <p className="text-sage-muted text-sm">{t('pets.loading')}</p>
      </section>
    )
  }

  if (error || !pet) {
    return (
      <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
        <p className="text-red-500 text-sm">{error ?? 'Pet not found'}</p>
      </section>
    )
  }

  const fields: Array<{ label: string; value: string | null }> = [
    { label: t('pets.detail.owner'), value: pet.owner },
    { label: t('pets.detail.age'), value: `${pet.age} ${t('pets.yearsOld')}` },
    { label: t('pets.detail.breed'), value: pet.breed },
    { label: t('pets.detail.birthdate'), value: pet.birthdate },
    { label: t('pets.detail.weight'), value: pet.weight !== null ? `${pet.weight} kg` : null },
    { label: t('pets.detail.nickname'), value: pet.nickname },
    { label: t('pets.detail.registrationDate'), value: pet.registrationDate },
  ]

  return (
    <>
      <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
        <div className="flex flex-col max-w-[600px] flex-1 gap-8">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <button
                type="button"
                onClick={() => navigate('/pets')}
                className="flex items-center justify-center size-10 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              >
                <span className="material-symbols-outlined text-sage-dark dark:text-white">arrow_back</span>
              </button>
              <h1 className="text-sage-dark dark:text-white text-3xl font-black leading-tight tracking-tight">
                {pet.name}
              </h1>
            </div>
            <button
              type="button"
              onClick={() => setShowConfirm(true)}
              className="flex items-center gap-1.5 h-10 px-4 rounded-full border border-red-200 dark:border-red-800 text-red-500 text-sm font-bold hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
            >
              <span className="material-symbols-outlined text-base">delete</span>
              {t('pets.delete.title')}
            </button>
          </div>

          {/* Card */}
          <div className="flex flex-col bg-white dark:bg-gray-900 rounded-2xl border border-primary/40 shadow-sm overflow-hidden">
            {/* Avatar + species */}
            <div className="flex flex-col items-center gap-3 py-10 bg-primary/5">
              <PetAvatar name={pet.name} species={pet.species} photoUrl={pet.photoUrl} size="lg" />
              <div className="text-center">
                <h2 className="text-sage-dark dark:text-white text-2xl font-bold">{pet.name}</h2>
                <p className="text-sage-muted text-sm mt-0.5">{pet.species}</p>
              </div>
            </div>

            {/* Fields */}
            <dl className="divide-y divide-gray-100 dark:divide-gray-800">
              {fields.map(({ label, value }) => (
                <div key={label} className="flex justify-between items-center px-8 py-4">
                  <dt className="text-sage-muted text-sm font-medium">{label}</dt>
                  <dd className="text-sage-dark dark:text-white text-sm font-semibold text-right">
                    {value ?? <span className="text-sage-muted/60 font-normal">{t('pets.detail.notProvided')}</span>}
                  </dd>
                </div>
              ))}
            </dl>
          </div>
        </div>
      </section>

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
