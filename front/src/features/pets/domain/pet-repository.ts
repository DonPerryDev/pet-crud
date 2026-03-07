import type { Pet, PetSummary, RegisterPetInput, UpdatePetInput } from './entities'

export interface PetRepository {
  getAll(): Promise<PetSummary[]>
  getById(id: string): Promise<Pet>
  register(input: RegisterPetInput): Promise<Pet>
  update(id: string, input: UpdatePetInput): Promise<Pet>
  remove(id: string): Promise<void>
}
