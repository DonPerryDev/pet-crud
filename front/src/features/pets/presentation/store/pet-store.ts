import { create } from 'zustand'
import type { PetCardViewModel } from '../types'
import type { SpeciesFilter } from '../types'

interface PetState {
  pets: PetCardViewModel[]
  isLoading: boolean
  error: string | null
  searchQuery: string
  speciesFilter: SpeciesFilter
  setPets: (pets: PetCardViewModel[]) => void
  setLoading: (isLoading: boolean) => void
  setError: (error: string | null) => void
  setSearchQuery: (query: string) => void
  setSpeciesFilter: (filter: SpeciesFilter) => void
  removePet: (id: string) => void
}

export const usePetStore = create<PetState>()((set) => ({
  pets: [],
  isLoading: false,
  error: null,
  searchQuery: '',
  speciesFilter: 'ALL',
  setPets: (pets): void => set({ pets }),
  setLoading: (isLoading): void => set({ isLoading }),
  setError: (error): void => set({ error }),
  setSearchQuery: (searchQuery): void => set({ searchQuery }),
  setSpeciesFilter: (speciesFilter): void => set({ speciesFilter }),
  removePet: (id): void => set((state) => ({ pets: state.pets.filter((p) => p.id !== id) })),
}))
