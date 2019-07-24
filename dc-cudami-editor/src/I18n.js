import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import de from './locales/de/translation.json';
import en from './locales/en/translation.json';

export default function (locale) {
  i18n.use(initReactI18next).init({
    debug: true,
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
};
