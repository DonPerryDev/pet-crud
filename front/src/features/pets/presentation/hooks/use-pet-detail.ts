import { useState, useEffect } from 'react'
import { createPetApi } from '../../infrastructure/pet-api'
import type { Pet } from '../../domain/entities'

const petApi = createPetApi()

interface UsePetDetailResult {
  pet: Pet | null
  isLoading: boolean
  error: string | null
}

export function usePetDetail(id: string): UsePetDetailResult {
  const [pet, setPet] = useState<Pet | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setIsLoading(true)
    setError(null)
    petApi
      .getById(id)
      .then((data) => {
        setPet(data)
        setIsLoading(false)
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load pet')
        setIsLoading(false)
      })
  }, [id])

  return { pet, isLoading, error }
}
