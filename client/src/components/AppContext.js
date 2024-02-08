import {createContext} from 'react'

export default createContext({
  // the context path of the surrounding java application
  apiContextPath: '/',
  // the defined default language for multilanguage fields, will be fetched from the api
  defaultLanguage: 'en',
  // the defined ui locale
  uiLocale: 'en',
})
