import { useState } from 'react'
import { createPetApi } from '../../infrastructure/pet-api'
import { usePetStore } from '../store/pet-store'

const petApi = createPetApi()

interface UseDeletePetResult {
  isDeleting: boolean
  deletePet: (id: string) => Promise<void>
}

export function useDeletePet(onSuccess: () => void): UseDeletePetResult {
  const [isDeleting, setIsDeleting] = useState(false)
  const removePet = usePetStore((s) => s.removePet)

  async function deletePet(id: string): Promise<void> {
    setIsDeleting(true)
    try {
      await petApi.remove(id)
      removePet(id)
      onSuccess()
    } finally {
      setIsDeleting(false)
    }
  }

  return { isDeleting, deletePet }
}
