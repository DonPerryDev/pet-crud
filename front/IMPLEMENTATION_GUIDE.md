# Implementation Guide

Step-by-step suggestions for building out each feature. Tackle them in any order — each section is self-contained.

---

## 1. Tailwind CSS + Custom Theme

The UI already uses Tailwind classes but Tailwind isn't installed yet.

```bash
npm install -D tailwindcss @tailwindcss/vite
```

**`vite.config.ts`** — add the Tailwind plugin:
```ts
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  // ...
})
```

**`src/index.css`** — import Tailwind and define custom theme colors:
```css
@import 'tailwindcss';

@theme {
  --color-primary: #c8e6c9;
  --color-sage-dark: #2e4a3e;
  --color-sage-muted: #6b8f7b;
  --color-background-dark: #1a2e23;
}
```

**`index.html`** — add Material Symbols font in `<head>`:
```html
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />
```

---

## 2. HTTP Client (`shared/lib/httpClient`)

Create a shared Axios (or ky) instance with interceptors.

```bash
npm install axios
```

```ts
// src/shared/lib/httpClient.ts
import axios from 'axios'

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
})

// Add auth interceptor when JWT auth is ready
// httpClient.interceptors.request.use((config) => {
//   const token = localStorage.getItem('token')
//   if (token) config.headers.Authorization = `Bearer ${token}`
//   return config
// })
```

Then update `features/pets/infrastructure/pet-api.ts` to use it.

---

## 3. TanStack Query

```bash
npm install @tanstack/react-query
```

**`src/app/providers.tsx`** — wrap with `QueryClientProvider`:
```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000 } },
})

export function AppProviders({ children }: AppProvidersProps): ReactNode {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Toaster position="top-right" richColors />
        {children}
      </BrowserRouter>
    </QueryClientProvider>
  )
}
```

**`features/pets/presentation/hooks/use-pets.ts`** — replace Zustand reads:
```ts
import { useQuery } from '@tanstack/react-query'
import { createPetApi } from '../../infrastructure/pet-api'

const repo = createPetApi()

export function usePets() {
  return useQuery({ queryKey: ['pets'], queryFn: () => repo.getAll() })
}
```

---

## 4. Pets Feature — Full CRUD

With httpClient and TanStack Query in place:

1. **Implement `pet-api.ts`** — uncomment the real HTTP calls, remove void stubs
2. **Add mutations** in a new `use-pet-mutations.ts` hook:
   ```ts
   export function useRegisterPet() {
     const queryClient = useQueryClient()
     return useMutation({
       mutationFn: (input: RegisterPetInput) => repo.register(input),
       onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pets'] }),
     })
   }
   ```
3. **Build UI components** in `presentation/components/`:
   - `PetCard.tsx` — individual pet display
   - `PetForm.tsx` — register/edit form (React Hook Form + Zod)
   - `PetList.tsx` — grid of PetCards
4. **Wire into `PetsPage.tsx`** — compose list + form + empty state

---

## 5. React Hook Form + Zod

```bash
npm install react-hook-form zod @hookform/resolvers
```

```ts
// features/pets/domain/schemas.ts
import { z } from 'zod'

export const registerPetSchema = z.object({
  name: z.string().min(1, 'Name is required').max(50),
  species: z.enum(['DOG', 'CAT']),
  breed: z.string().min(1, 'Breed is required'),
  birthDate: z.string().date('Invalid date'),
})

export type RegisterPetFormValues = z.infer<typeof registerPetSchema>
```

---

## 6. Auth Feature

Create `features/auth/` with the same 3-layer structure:

```
features/auth/
├── domain/
│   ├── entities.ts        # User, AuthTokens
│   └── auth-repository.ts # login, register, refreshToken
├── infrastructure/
│   └── auth-api.ts        # JWT storage, httpClient interceptors
├── presentation/
│   ├── LoginPage.tsx
│   ├── hooks/useAuth.ts
│   └── components/AuthGuard.tsx
└── index.ts
```

The `AuthGuard` wraps protected routes in `app/router.tsx`.

---

## 7. Testing

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom
```

**`vite.config.ts`** — add test config:
```ts
export default defineConfig({
  // ...
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
  },
})
```

Start with domain layer tests (pure functions, zero mocking needed):
```ts
// features/pets/domain/__tests__/entities.test.ts
import { describe, it, expect } from 'vitest'
// test filterBySpecies, validation logic, etc.
```

---

## 8. Shared UI Components

Build reusable components in `shared/ui/` as needs arise:
- `Button.tsx` — variants (primary, outline, ghost), sizes
- `Input.tsx` — label, error message, forwarded ref
- `Modal.tsx` — portal-based dialog
- `Card.tsx` — consistent card container
- `Spinner.tsx` — loading indicator

Keep them generic. Feature-specific components stay in `features/<name>/presentation/components/`.
