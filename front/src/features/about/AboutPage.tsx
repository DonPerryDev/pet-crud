import type { ReactNode } from 'react'
import { useTranslation } from '@/shared/i18n'

export function AboutPage(): ReactNode {
  const { t } = useTranslation()

  return (
    <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
      <div className="layout-content-container flex flex-col max-w-[1200px] flex-1">
        <h1 className="text-sage-dark dark:text-white text-4xl font-black leading-tight tracking-tight mb-6">
          {t('about.title')}
        </h1>
        <p className="text-sage-muted dark:text-primary/80 text-lg leading-relaxed max-w-[600px]">
          {t('about.description')}
        </p>
      </div>
    </section>
  )
}
