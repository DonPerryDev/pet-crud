import type { ReactNode } from 'react'
import { HERO_IMAGE_URL } from '@/shared/config/constants'
import { useTranslation } from '@/shared/i18n'

export function HomePage(): ReactNode {
  const { t } = useTranslation()

  return (
    <section className="flex flex-1 justify-center py-10 px-6 lg:px-40">
      <div className="layout-content-container flex flex-col max-w-[1200px] flex-1">
        {/* Hero Section */}
        <div className="@[480px]:px-4">
          <div className="flex flex-col gap-10 py-10 @[864px]:flex-row-reverse @[864px]:items-center">
            {/* Hero Image */}
            <div className="w-full flex-1">
              <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-primary to-sage-muted rounded-xl blur opacity-25 group-hover:opacity-40 transition duration-1000 group-hover:duration-200" />
                <div
                  className="relative w-full bg-center bg-no-repeat aspect-[4/3] bg-cover rounded-xl shadow-2xl"
                  data-alt="A professional high quality photo of a golden retriever and a grey cat sitting together harmoniously"
                  style={{ backgroundImage: `url("${HERO_IMAGE_URL}")` }}
                />
              </div>
            </div>

            {/* Hero Text Content */}
            <div className="flex flex-col gap-8 flex-1 @[864px]:pr-12">
              <div className="flex flex-col gap-4 text-left">
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-primary text-sage-dark uppercase tracking-widest w-fit">{t('home.hero.badge')}</span>
                <h1 className="text-sage-dark dark:text-white text-4xl font-black leading-tight tracking-tight @[480px]:text-6xl">
                  {t('home.hero.titleStart')} <span className="text-sage-muted">{t('home.hero.titleHighlight')}</span>
                </h1>
                <p className="text-sage-muted dark:text-primary/80 text-lg font-normal leading-relaxed max-w-[500px]">
                  {t('home.hero.description')}
                </p>
              </div>
              <div className="flex flex-wrap gap-4">
                <button className="flex min-w-[180px] cursor-pointer items-center justify-center rounded-full h-14 px-8 bg-primary text-sage-dark text-base font-bold shadow-sm hover:shadow-md hover:scale-105 transition-all">
                  <span className="material-symbols-outlined mr-2">dashboard_customize</span>
                  {t('home.hero.managePets')}
                </button>
                <button className="flex min-w-[140px] cursor-pointer items-center justify-center rounded-full h-14 px-8 bg-white border border-primary text-sage-dark text-base font-semibold hover:bg-primary/30 transition-all">
                  {t('home.hero.learnMore')}
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Stats Row */}
        <div className="flex flex-wrap gap-6 p-4 mt-8">
          <div className="flex min-w-[200px] flex-1 flex-col gap-3 rounded-xl p-8 bg-white/60 backdrop-blur-sm border border-primary/40 shadow-sm hover:shadow-md transition-shadow">
            <div className="flex items-center gap-2 text-sage-muted">
              <span className="material-symbols-outlined text-lg">database</span>
              <p className="text-sm font-semibold uppercase tracking-wider">{t('home.stats.petsRegistered')}</p>
            </div>
            <p className="text-sage-dark text-4xl font-black leading-tight">2</p>
          </div>
          <div className="flex min-w-[200px] flex-1 flex-col gap-3 rounded-xl p-8 bg-white/60 backdrop-blur-sm border border-primary/40 shadow-sm hover:shadow-md transition-shadow">
            <div className="flex items-center gap-2 text-sage-muted">
              <span className="material-symbols-outlined text-lg">calendar_today</span>
              <p className="text-sm font-semibold uppercase tracking-wider">{t('home.stats.nextVetVisit')}</p>
            </div>
            <p className="text-sage-dark text-4xl font-black leading-tight">{t('home.stats.tomorrow')}</p>
          </div>
          <div className="flex min-w-[200px] flex-1 flex-col gap-3 rounded-xl p-8 bg-white/60 backdrop-blur-sm border border-primary/40 shadow-sm hover:shadow-md transition-shadow">
            <div className="flex items-center gap-2 text-sage-muted">
              <span className="material-symbols-outlined text-lg">inventory_2</span>
              <p className="text-sm font-semibold uppercase tracking-wider">{t('home.stats.inventoryStatus')}</p>
            </div>
            <p className="text-sage-dark text-4xl font-black leading-tight">{t('home.stats.healthy')}</p>
          </div>
        </div>

        {/* Feature Dashboard Section */}
        <div className="px-4 pb-10">
          <div className="flex items-center justify-between pb-6 pt-12">
            <h2 className="text-sage-dark dark:text-white text-2xl font-bold tracking-tight">{t('home.dashboard.title')}</h2>
            <button className="text-sage-muted text-sm font-bold flex items-center gap-1 hover:text-sage-dark">
              {t('home.dashboard.viewFull')} <span className="material-symbols-outlined">chevron_right</span>
            </button>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Feature 1 */}
            <div className="group flex flex-col gap-4 rounded-xl border border-primary/40 bg-white p-6 shadow-sm hover:border-sage-muted transition-all cursor-pointer">
              <div className="bg-primary size-12 rounded-full flex items-center justify-center text-sage-dark group-hover:bg-sage-dark group-hover:text-primary transition-colors">
                <span className="material-symbols-outlined">monitoring</span>
              </div>
              <div className="flex flex-col gap-1">
                <h3 className="text-sage-dark text-lg font-bold leading-tight">{t('home.dashboard.healthTracking')}</h3>
                <p className="text-sage-muted text-sm font-normal leading-normal">{t('home.dashboard.healthTrackingDesc')}</p>
              </div>
            </div>
            {/* Feature 2 */}
            <div className="group flex flex-col gap-4 rounded-xl border border-primary/40 bg-white p-6 shadow-sm hover:border-sage-muted transition-all cursor-pointer">
              <div className="bg-primary size-12 rounded-full flex items-center justify-center text-sage-dark group-hover:bg-sage-dark group-hover:text-primary transition-colors">
                <span className="material-symbols-outlined">pill</span>
              </div>
              <div className="flex flex-col gap-1">
                <h3 className="text-sage-dark text-lg font-bold leading-tight">{t('home.dashboard.inventory')}</h3>
                <p className="text-sage-muted text-sm font-normal leading-normal">{t('home.dashboard.inventoryDesc')}</p>
              </div>
            </div>
            {/* Feature 3 */}
            <div className="group flex flex-col gap-4 rounded-xl border border-primary/40 bg-white p-6 shadow-sm hover:border-sage-muted transition-all cursor-pointer">
              <div className="bg-primary size-12 rounded-full flex items-center justify-center text-sage-dark group-hover:bg-sage-dark group-hover:text-primary transition-colors">
                <span className="material-symbols-outlined">notification_important</span>
              </div>
              <div className="flex flex-col gap-1">
                <h3 className="text-sage-dark text-lg font-bold leading-tight">{t('home.dashboard.reminders')}</h3>
                <p className="text-sage-muted text-sm font-normal leading-normal">{t('home.dashboard.remindersDesc')}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
