import i18n from 'i18next'
import {initReactI18next} from 'react-i18next'

import de from './locales/de/translation.json'
import deLanguageNames from './locales/de/languages.json'
import en from './locales/en/translation.json'
import enLanguageNames from './locales/en/languages.json'

export default function (locale) {
  const instance = i18n.createInstance()
  instance.use(initReactI18next).init({
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false,
      format: function (value, format) {
        if (format === 'capitalize') {
          return value.charAt(0).toUpperCase() + value.slice(1)
        }
        return value
      },
    },
    lng: locale,
    resources: {
      de: {
        ...de,
        ...deLanguageNames,
      },
      en: {
        ...en,
        ...enLanguageNames,
      },
    },
  })
}
