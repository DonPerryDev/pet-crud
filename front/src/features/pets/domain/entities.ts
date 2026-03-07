export type Species = 'DOG' | 'CAT'

export interface Pet {
  id: string
  name: string
  species: Species
  breed: string | null
  age: number
  birthdate: string | null
  weight: number | null
  nickname: string | null
  owner: string
  registrationDate: string
  photoUrl: string | null
}

export interface PetSummary {
  id: string
  name: string
  species: Species
  breed: string | null
  photoUrl: string | null
}

export interface RegisterPetInput {
  name: string
  species: Species
  age: number
  breed?: string | null
  birthdate?: string | null
  weight?: number | null
  nickname?: string | null
}

export interface UpdatePetInput {
  name: string
  species: Species
  age: number
  breed?: string | null
  birthdate?: string | null
  weight?: number | null
  nickname?: string | null
}
