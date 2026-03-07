import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import { PROFILE_IMAGE_URL } from '@/shared/config/constants'
import { useTranslation } from '@/shared/i18n'
import type { TranslationKey } from '@/shared/i18n'
import { ThemeToggle } from '../ThemeToggle'

const navLinks: ReadonlyArray<{ to: string; labelKey: TranslationKey }> = [
  { to: '/', labelKey: 'navbar.home' },
  { to: '/pets', labelKey: 'navbar.pets' },
  { to: '/about', labelKey: 'navbar.aboutUs' },
]

export function Navbar(): ReactNode {
  const { t, locale, setLocale } = useTranslation()

  return (
    <header className="sticky top-0 z-50 flex items-center justify-between whitespace-nowrap bg-white/80 dark:bg-background-dark/80 backdrop-blur-md border-b border-solid border-primary/20 px-10 py-4 lg:px-40">
      <NavLink to="/" className="flex items-center gap-3">
        <div className="bg-primary p-2 rounded-full flex items-center justify-center">
          <span className="material-symbols-outlined text-sage-dark">pets</span>
        </div>
        <h2 className="text-sage-dark dark:text-white text-xl font-bold tracking-tight">{t('navbar.appName')}</h2>
      </NavLink>

      <nav className="hidden md:flex flex-1 justify-center gap-12">
        {navLinks.map(({ to, labelKey }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }): string =>
              isActive
                ? 'text-sage-dark dark:text-primary text-sm font-semibold hover:text-sage-muted transition-colors relative after:content-[\'\'] after:absolute after:-bottom-1 after:left-0 after:w-full after:h-0.5 after:bg-sage-muted'
                : 'text-sage-muted dark:text-primary/70 text-sm font-medium hover:text-sage-dark transition-colors'
            }
          >
            {t(labelKey)}
          </NavLink>
        ))}
      </nav>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={(): void => setLocale(locale === 'en' ? 'es' : 'en')}
          className="flex items-center justify-center h-8 px-3 rounded-full text-xs font-bold uppercase tracking-wider hover:bg-primary/20 transition-colors"
          aria-label={`Switch language to ${locale === 'en' ? 'Espa\u00F1ol' : 'English'}`}
        >
          {locale === 'en' ? 'ES' : 'EN'}
        </button>
        <ThemeToggle />
        <button className="hidden sm:flex min-w-[100px] cursor-pointer items-center justify-center rounded-full h-10 px-5 bg-primary text-sage-dark text-sm font-bold border border-transparent hover:border-sage-muted transition-all">
          {t('navbar.login')}
        </button>
        <div
          className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10 border-2 border-primary"
          data-alt="User profile picture of a person smiling"
          style={{ backgroundImage: `url("${PROFILE_IMAGE_URL}")` }}
        />
      </div>
    </header>
  )
}
