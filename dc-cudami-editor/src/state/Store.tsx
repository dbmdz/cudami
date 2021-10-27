import {ReactNode, createContext, useReducer} from 'react'

import Reducer from './Reducer'

interface Props {
  /* the context path of the surrounding java application */
  apiContextPath: string
  /* the children who use the store */
  children: ReactNode
  /* the defined ui locale */
  uiLocale: string
}

interface State {
  /* the defined default language for multilanguage fields, will be fetched from the api */
  defaultLanguage: string
  /* the open state of the dialogs */
  dialogsOpen: {[key: string]: boolean}
}

const initialState = {
  defaultLanguage: 'en',
  dialogsOpen: {
    addAttachedIdentifiables: false,
    moveAttachedIdentifiable: false,
    removeAttachedIdentifiable: false,
  },
}

const AppContext = createContext({})

const Store = ({apiContextPath = '/', children, uiLocale = 'en'}: Props) => {
  const [state, dispatch] = useReducer(Reducer, initialState)
  return (
    <AppContext.Provider value={{apiContextPath, dispatch, state, uiLocale}}>
      {children}
    </AppContext.Provider>
  )
}

export {AppContext}
export type {State}
export default Store
