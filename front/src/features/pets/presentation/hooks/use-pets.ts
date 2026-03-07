import { useEffect, useMemo } from 'react'
import { usePetStore } from '../store/pet-store'
import { createPetApi } from '../../infrastructure/pet-api'
import type { PetSummary } from '../../domain/entities'
import type { PetCardViewModel } from '../types'

const petApi = createPetApi()

function toCardViewModel(pet: PetSummary): PetCardViewModel {
  return {
    id: pet.id,
    name: pet.name,
    species: pet.species,
    breed: pet.breed,
    age: null,
    photoUrl: pet.photoUrl,
    tags: [],
  }
}

interface UsePetsResult {
  pets: PetCardViewModel[]
  isLoading: boolean
  error: string | null
}

export function usePets(): UsePetsResult {
  const allPets = usePetStore((s) => s.pets)
  const isLoading = usePetStore((s) => s.isLoading)
  const error = usePetStore((s) => s.error)
  const searchQuery = usePetStore((s) => s.searchQuery)
  const speciesFilter = usePetStore((s) => s.speciesFilter)
  const setPets = usePetStore((s) => s.setPets)
  const setLoading = usePetStore((s) => s.setLoading)
  const setError = usePetStore((s) => s.setError)

  useEffect(() => {
    setLoading(true)
    setError(null)
    petApi
      .getAll()
      .then((pets) => {
        setPets(pets.map(toCardViewModel))
        setLoading(false)
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load pets')
        setLoading(false)
      })
  }, [setPets, setLoading, setError])

  const pets = useMemo(() => {
    let filtered = allPets

    if (speciesFilter !== 'ALL') {
      filtered = filtered.filter((p) => p.species === speciesFilter)
    }

    const query = searchQuery.toLowerCase().trim()
    if (query) {
      filtered = filtered.filter(
        (p) =>
          p.name.toLowerCase().includes(query) ||
          (p.breed?.toLowerCase().includes(query) ?? false),
      )
    }

    return filtered
  }, [allPets, speciesFilter, searchQuery])

  return { pets, isLoading, error }
}
