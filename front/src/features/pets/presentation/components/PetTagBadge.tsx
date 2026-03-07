import type { ReactNode } from 'react'
import type { PetTag } from '../types'
import { useTranslation } from '@/shared/i18n'

interface PetTagBadgeProps {
  tag: PetTag
}

const VARIANT_CLASSES: Record<PetTag['variant'], string> = {
  healthy: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  'checkup-due': 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  training: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
}

export function PetTagBadge({ tag }: PetTagBadgeProps): ReactNode {
  const { t } = useTranslation()

  return (
    <span
      className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${VARIANT_CLASSES[tag.variant]}`}
    >
      {t(tag.label)}
    </span>
  )
}
