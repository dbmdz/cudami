import {createContext} from 'react'

export default createContext({
  // the context path of the surrounding java application
  apiContextPath: '/',
  // the defined default language for multilanguage fields, will be fetched from the api
  defaultLanguage: 'en',
  // a flag whether the api calls should be mocked, the mock data is located in /public/__mock__
  mockApi: false,
})
