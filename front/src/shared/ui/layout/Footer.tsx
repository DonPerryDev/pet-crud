import type { ReactNode } from 'react'
import { useTranslation } from '@/shared/i18n'

export function Footer(): ReactNode {
  const { t } = useTranslation()

  return (
    <footer className="mt-auto py-10 px-10 border-t border-primary/20 flex flex-col md:flex-row justify-between items-center gap-6 bg-white/50 dark:bg-slate-900/80">
      <div className="flex items-center gap-2">
        <span className="material-symbols-outlined text-sage-muted dark:text-slate-400">pets</span>
        <p className="text-sage-muted dark:text-slate-400 text-sm">{t('footer.copyright')}</p>
      </div>
      <div className="flex gap-8">
        <a className="text-sage-muted dark:text-slate-400 text-sm hover:text-sage-dark dark:hover:text-slate-200 transition-colors" href="#">{t('footer.privacyPolicy')}</a>
        <a className="text-sage-muted dark:text-slate-400 text-sm hover:text-sage-dark dark:hover:text-slate-200 transition-colors" href="#">{t('footer.termsOfService')}</a>
        <a className="text-sage-muted dark:text-slate-400 text-sm hover:text-sage-dark dark:hover:text-slate-200 transition-colors" href="#">{t('footer.support')}</a>
      </div>
    </footer>
  )
}
