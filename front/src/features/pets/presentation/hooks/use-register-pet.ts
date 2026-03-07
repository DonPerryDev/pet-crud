import { useState } from 'react'
import { createPetApi } from '../../infrastructure/pet-api'
import type { RegisterPetInput } from '../../domain/entities'

const petApi = createPetApi()

interface UseRegisterPetResult {
  isSubmitting: boolean
  register: (input: RegisterPetInput) => Promise<void>
}

export function useRegisterPet(onSuccess: () => void): UseRegisterPetResult {
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function register(input: RegisterPetInput): Promise<void> {
    setIsSubmitting(true)
    try {
      await petApi.register(input)
      onSuccess()
    } finally {
      setIsSubmitting(false)
    }
  }

  return { isSubmitting, register }
}
