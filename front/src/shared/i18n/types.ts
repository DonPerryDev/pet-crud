export type Locale = 'en' | 'es'

export interface TranslationDictionary {
  navbar: {
    appName: string
    home: string
    pets: string
    aboutUs: string
    login: string
  }
  footer: {
    copyright: string
    privacyPolicy: string
    termsOfService: string
    support: string
  }
  home: {
    hero: {
      badge: string
      titleStart: string
      titleHighlight: string
      description: string
      managePets: string
      learnMore: string
    }
    stats: {
      petsRegistered: string
      nextVetVisit: string
      tomorrow: string
      inventoryStatus: string
      healthy: string
    }
    dashboard: {
      title: string
      viewFull: string
      healthTracking: string
      healthTrackingDesc: string
      inventory: string
      inventoryDesc: string
      reminders: string
      remindersDesc: string
    }
  }
  pets: {
    title: string
    subtitle: string
    registerNewPet: string
    addAnotherPet: string
    addAnotherPetDesc: string
    loading: string
    noPetsTitle: string
    noPetsDescription: string
    searchPlaceholder: string
    filterAll: string
    filterDogs: string
    filterCats: string
    filterOther: string
    viewDetails: string
    yearsOld: string
    tags: {
      healthy: string
      vaccinated: string
      checkupDue: string
      training: string
      dietaryNeeds: string
    }
    delete: {
      title: string
      confirmQuestion: string
      warning: string
      confirm: string
      cancel: string
      successMessage: string
      errorMessage: string
    }
    detail: {
      owner: string
      age: string
      breed: string
      birthdate: string
      weight: string
      nickname: string
      registrationDate: string
      notProvided: string
      backToList: string
    }
    form: {
      title: string
      nameLabel: string
      namePlaceholder: string
      speciesLabel: string
      speciesDog: string
      speciesCat: string
      ageLabel: string
      agePlaceholder: string
      breedLabel: string
      breedPlaceholder: string
      birthdateLabel: string
      weightLabel: string
      weightPlaceholder: string
      nicknameLabel: string
      nicknamePlaceholder: string
      submit: string
      cancel: string
      successMessage: string
      errorMessage: string
      optional: string
    }
  }
  about: {
    title: string
    description: string
  }
}

type DotPath<T> = T extends string
  ? never
  : {
      [K in keyof T & string]: T[K] extends string
        ? K
        : `${K}.${DotPath<T[K]>}`
    }[keyof T & string]

export type TranslationKey = DotPath<TranslationDictionary>
