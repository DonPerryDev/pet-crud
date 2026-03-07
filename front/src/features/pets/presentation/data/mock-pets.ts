import type { PetCardViewModel } from '../types'

export const MOCK_PETS: PetCardViewModel[] = [
  {
    id: '1',
    name: 'Buddy',
    species: 'DOG',
    breed: 'Golden Retriever',
    age: 3,
    photoUrl: null,
    tags: [
      { label: 'pets.tags.healthy', variant: 'healthy' },
      { label: 'pets.tags.vaccinated', variant: 'healthy' },
    ],
  },
  {
    id: '2',
    name: 'Luna',
    species: 'CAT',
    breed: 'Siamese',
    age: 2,
    photoUrl: null,
    tags: [
      { label: 'pets.tags.checkupDue', variant: 'checkup-due' },
    ],
  },
  {
    id: '3',
    name: 'Max',
    species: 'DOG',
    breed: 'Pug',
    age: 5,
    photoUrl: null,
    tags: [
      { label: 'pets.tags.healthy', variant: 'healthy' },
      { label: 'pets.tags.training', variant: 'training' },
    ],
  },
  {
    id: '4',
    name: 'Thumper',
    species: 'CAT',
    breed: 'Ragdoll',
    age: 1,
    photoUrl: null,
    tags: [
      { label: 'pets.tags.dietaryNeeds', variant: 'checkup-due' },
    ],
  },
]
