import type { Species } from '../../domain/entities'
import type { TranslationKey } from '@/shared/i18n'

export type PetTagVariant = 'healthy' | 'checkup-due' | 'training'

export interface PetTag {
  label: TranslationKey
  variant: PetTagVariant
}

export interface PetCardViewModel {
  id: string
  name: string
  species: Species
  breed: string | null
  age: number | null
  photoUrl: string | null
  tags: PetTag[]
}
