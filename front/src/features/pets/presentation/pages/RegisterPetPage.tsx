import { useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { useTranslation } from '@/shared/i18n'
import { useRegisterPet } from '../hooks/use-register-pet'
import type { Species } from '../../domain/entities'

interface FormState {
  name: string
  species: Species
  age: string
  breed: string
  birthdate: string
  weight: string
  nickname: string
}

const initialState: FormState = {
  name: '',
  species: 'DOG',
  age: '',
  breed: '',
  birthdate: '',
  weight: '',
  nickname: '',
}

export function RegisterPetPage(): ReactNode {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [form, setForm] = useState<FormState>(initialState)

  const { isSubmitting, register } = useRegisterPet(() => {
    toast.success(t('pets.form.successMessage'))
    navigate('/pets')
  })

  function handleChange(field: keyof FormState, value: string): void {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>): Promise<void> {
    e.preventDefault()
    try {
      await register({
        name: form.name.trim(),
        species: form.species,
        age: Number(form.age),
        breed: form.breed.trim() || null,
        birthdate: form.birthdate || null,
        weight: form.weight ? Number(form.weight) : null,
        nickname: form.nickname.trim() || null,
      })
    } catch {
      toast.error(t('pets.form.errorMessage'))
    }
  }

  return (
    <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
      <div className="flex flex-col max-w-[600px] flex-1 gap-8">
        {/* Header */}
        <div className="flex items-center gap-4">
          <button
            type="button"
            onClick={() => navigate('/pets')}
            className="flex items-center justify-center size-10 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          >
            <span className="material-symbols-outlined text-sage-dark dark:text-white">arrow_back</span>
          </button>
          <h1 className="text-sage-dark dark:text-white text-3xl font-black leading-tight tracking-tight">
            {t('pets.form.title')}
          </h1>
        </div>

        {/* Form */}
        <form
          onSubmit={handleSubmit}
          className="flex flex-col gap-6 bg-white dark:bg-gray-900 rounded-2xl border border-primary/40 p-8 shadow-sm"
        >
          {/* Name */}
          <div className="flex flex-col gap-1.5">
            <label className="text-sage-dark dark:text-white text-sm font-bold">
              {t('pets.form.nameLabel')}
            </label>
            <input
              type="text"
              required
              value={form.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder={t('pets.form.namePlaceholder')}
              className="h-11 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 text-sage-dark dark:text-white text-sm placeholder:text-sage-muted/60 focus:outline-none focus:border-primary transition-colors"
            />
          </div>

          {/* Species */}
          <div className="flex flex-col gap-1.5">
            <label className="text-sage-dark dark:text-white text-sm font-bold">
              {t('pets.form.speciesLabel')}
            </label>
            <div className="flex gap-3">
              {(['DOG', 'CAT'] as Species[]).map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => handleChange('species', s)}
                  className={`flex-1 h-11 rounded-xl border-2 text-sm font-bold transition-all ${
                    form.species === s
                      ? 'border-primary bg-primary/10 text-sage-dark dark:text-white'
                      : 'border-gray-200 dark:border-gray-700 text-sage-muted hover:border-primary/50'
                  }`}
                >
                  {s === 'DOG' ? t('pets.form.speciesDog') : t('pets.form.speciesCat')}
                </button>
              ))}
            </div>
          </div>

          {/* Age */}
          <div className="flex flex-col gap-1.5">
            <label className="text-sage-dark dark:text-white text-sm font-bold">
              {t('pets.form.ageLabel')}
            </label>
            <input
              type="number"
              required
              min={0}
              max={50}
              value={form.age}
              onChange={(e) => handleChange('age', e.target.value)}
              placeholder={t('pets.form.agePlaceholder')}
              className="h-11 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 text-sage-dark dark:text-white text-sm placeholder:text-sage-muted/60 focus:outline-none focus:border-primary transition-colors"
            />
          </div>

          {/* Optional fields */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Breed */}
            <div className="flex flex-col gap-1.5">
              <label className="text-sage-dark dark:text-white text-sm font-bold flex items-center gap-2">
                {t('pets.form.breedLabel')}
                <span className="text-sage-muted text-xs font-normal">({t('pets.form.optional')})</span>
              </label>
              <input
                type="text"
                value={form.breed}
                onChange={(e) => handleChange('breed', e.target.value)}
                placeholder={t('pets.form.breedPlaceholder')}
                className="h-11 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 text-sage-dark dark:text-white text-sm placeholder:text-sage-muted/60 focus:outline-none focus:border-primary transition-colors"
              />
            </div>

            {/* Nickname */}
            <div className="flex flex-col gap-1.5">
              <label className="text-sage-dark dark:text-white text-sm font-bold flex items-center gap-2">
                {t('pets.form.nicknameLabel')}
                <span className="text-sage-muted text-xs font-normal">({t('pets.form.optional')})</span>
              </label>
              <input
                type="text"
                value={form.nickname}
                onChange={(e) => handleChange('nickname', e.target.value)}
                placeholder={t('pets.form.nicknamePlaceholder')}
                className="h-11 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 text-sage-dark dark:text-white text-sm placeholder:text-sage-muted/60 focus:outline-none focus:border-primary transition-colors"
              />
            </div>

            {/* Weight */}
            <div className="flex flex-col gap-1.5">
              <label className="text-sage-dark dark:text-white text-sm font-bold flex items-center gap-2">
                {t('pets.form.weightLabel')}
                <span className="text-sage-muted text-xs font-normal">({t('pets.form.optional')})</span>
              </label>
              <input
                type="number"
                min={0}
                step={0.1}
                value={form.weight}
                onChange={(e) => handleChange('weight', e.target.value)}
                placeholder={t('pets.form.weightPlaceholder')}
                className="h-11 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 text-sage-dark dark:text-white text-sm placeholder:text-sage-muted/60 focus:outline-none focus:border-primary transition-colors"
              />
            </div>

            {/* Birthdate */}
            <div className="flex flex-col gap-1.5">
              <label className="text-sage-dark dark:text-white text-sm font-bold flex items-center gap-2">
                {t('pets.form.birthdateLabel')}
                <span className="text-sage-muted text-xs font-normal">({t('pets.form.optional')})</span>
              </label>
              <input
                type="date"
                value={form.birthdate}
                onChange={(e) => handleChange('birthdate', e.target.value)}
                className="h-11 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-4 text-sage-dark dark:text-white text-sm focus:outline-none focus:border-primary transition-colors"
              />
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate('/pets')}
              className="flex-1 h-11 rounded-full border border-gray-200 dark:border-gray-700 text-sage-dark dark:text-white text-sm font-bold hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
            >
              {t('pets.form.cancel')}
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 h-11 rounded-full bg-primary text-sage-dark text-sm font-bold hover:shadow-md hover:scale-[1.02] transition-all disabled:opacity-60 disabled:cursor-not-allowed disabled:scale-100"
            >
              {isSubmitting ? '...' : t('pets.form.submit')}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}
