import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import de from './locales/de/translation.json';
import deLanguageNames from './locales/de/languages.json';
import en from './locales/en/translation.json';
import enLanguageNames from './locales/en/languages.json';

export default function (locale) {
  i18n.use(initReactI18next).init({
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false,
    },
    lng: locale,
    resources: {
      de,
      en
    }
  });
  i18n.addResourceBundle('de', 'languageNames', deLanguageNames);
  i18n.addResourceBundle('en', 'languageNames', enLanguageNames);
};
