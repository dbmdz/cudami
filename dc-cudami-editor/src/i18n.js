import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import de from './locales/de/translation.json';
import deLanguageNames from './locales/de/languages.json';
import en from './locales/en/translation.json';
import enLanguageNames from './locales/en/languages.json';

export default function (locale) {
  const instance = i18n.createInstance();
  instance.use(initReactI18next).init({
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
  instance.addResourceBundle('de', 'languageNames', deLanguageNames);
  instance.addResourceBundle('en', 'languageNames', enLanguageNames);
  return instance;
};
