import type { PetRepository } from '../domain/pet-repository'
import type { Pet, PetSummary, RegisterPetInput, UpdatePetInput } from '../domain/entities'
import type { PetDto, PetListDto } from './pet-mapper'
import { toPet, toPetSummary } from './pet-mapper'
import { httpClient } from '@/shared/lib/httpClient'

export function createPetApi(): PetRepository {
  return {
    async getAll(): Promise<PetSummary[]> {
      const data = await httpClient.get<PetListDto[]>('/api/pets')
      return data.map(toPetSummary)
    },

    async getById(id: string): Promise<Pet> {
      const data = await httpClient.get<PetDto>(`/api/pets/${id}/detail`)
      return toPet(data)
    },

    async register(input: RegisterPetInput): Promise<Pet> {
      const data = await httpClient.post<PetDto>('/api/pets', input)
      return toPet(data)
    },

    async update(id: string, input: UpdatePetInput): Promise<Pet> {
      const data = await httpClient.put<PetDto>(`/api/pets/${id}`, input)
      return toPet(data)
    },

    async remove(id: string): Promise<void> {
      await httpClient.delete(`/api/pets/${id}`)
    },
  }
}
