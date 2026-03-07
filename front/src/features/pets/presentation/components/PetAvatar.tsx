import type { ReactNode } from 'react'
import type { Species } from '../../domain/entities'

interface PetAvatarProps {
  name: string
  species: Species
  photoUrl: string | null
  size?: 'md' | 'lg'
}

function getInitials(name: string): string {
  return name.charAt(0).toUpperCase()
}

function getSpeciesIcon(species: Species): string {
  return species === 'DOG' ? 'pet_supplies' : 'cruelty_free'
}

export function PetAvatar({ name, species, photoUrl, size = 'md' }: PetAvatarProps): ReactNode {
  const avatarSize = size === 'lg' ? 'size-24 text-4xl' : 'size-16 text-2xl'

  return (
    <div className="relative mx-auto mb-3">
      {photoUrl ? (
        <img
          src={photoUrl}
          alt={name}
          className={`${avatarSize} rounded-full object-cover ring-2 ring-primary/30`}
        />
      ) : (
        <div className={`${avatarSize} rounded-full bg-primary/20 dark:bg-primary/10 flex items-center justify-center text-primary dark:text-primary font-bold`}>
          {getInitials(name)}
        </div>
      )}
      <span className="absolute -bottom-1 -right-1 flex size-7 items-center justify-center rounded-full bg-white dark:bg-gray-800 shadow-sm ring-1 ring-gray-200 dark:ring-gray-700">
        <span className="material-symbols-outlined text-sm text-sage-muted">
          {getSpeciesIcon(species)}
        </span>
      </span>
    </div>
  )
}
