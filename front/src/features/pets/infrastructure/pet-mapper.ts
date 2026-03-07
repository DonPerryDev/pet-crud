import type { Pet, PetSummary, Species } from '../domain/entities'

export interface PetDto {
  id: string
  name: string
  species: string
  breed: string | null
  age: number
  birthdate: string | null
  weight: number | null
  nickname: string | null
  owner: string
  registrationDate: string
  photoUrl: string | null
}

export interface PetListDto {
  id: string
  name: string
  species: string
  breed: string | null
  photoUrl: string | null
}

export function toPet(dto: PetDto): Pet {
  return {
    id: dto.id,
    name: dto.name,
    species: dto.species as Species,
    breed: dto.breed,
    age: dto.age,
    birthdate: dto.birthdate,
    weight: dto.weight,
    nickname: dto.nickname,
    owner: dto.owner,
    registrationDate: dto.registrationDate,
    photoUrl: dto.photoUrl,
  }
}

export function toPetSummary(dto: PetListDto): PetSummary {
  return {
    id: dto.id,
    name: dto.name,
    species: dto.species as Species,
    breed: dto.breed,
    photoUrl: dto.photoUrl,
  }
}
