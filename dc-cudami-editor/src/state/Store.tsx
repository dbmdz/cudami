import {Dispatch, ReactNode, useEffect, useReducer} from 'react'
import {TFunction, useTranslation} from 'react-i18next'
import {createContext} from 'use-context-selector'

import {getAvailableLanguages, getDefaultLanguage} from '../api'
import {DialogNames, FeedbackMessage} from '../types'
import {setAvailableLanguages, setDefaultLanguage} from './actions'
import {Action} from './actionTypes'
import {FormState, initialFormState} from './FormState'
import {ListState, initialListState} from './ListState'
import Reducer from './Reducer'

interface Context {
  apiContextPath: string
  dispatch: Dispatch<Action>
  state: State
  uiLocale?: string
}

interface Props {
  /** the active language for multilanguage fields on start */
  activeLanguage?: string
  /** the context path of the surrounding java application */
  apiContextPath: string
  /** the children who use the store */
  children: ReactNode
  /** a list of existing languages for multilanguage fields */
  existingLanguages?: string[]
  /** the type of the store */
  type: 'form' | 'list'
  /** the defined ui locale */
  uiLocale: string
}

interface State {
  /** the current active language for multilanguage fields, will be changed by clicking the language tabs */
  activeLanguage?: string
  /** the defined default language for multilanguage fields, will be fetched from the api */
  defaultLanguage?: string
  /** the open state of the dialogs */
  dialogsOpen: Record<DialogNames, boolean>
  /** a list of already existing languages for multilanguage fields */
  existingLanguages?: string[]
  /** a feedback message with all the needed information */
  feedbackMessage?: FeedbackMessage
  /** all the state that is only relevant for form components */
  forms?: FormState
  /** all the state that is only relevant for list components */
  lists?: ListState
}

const loadinitialState = async (
  context: string,
  type: string,
  t: TFunction,
  existingLanguages: string[] = [],
) => {
  const defaultLanguage = await getDefaultLanguage(context)
  if (type === 'list') {
    return {
      defaultLanguage,
    }
  }
  const availableLanguages = await getAvailableLanguages(context)
  return {
    defaultLanguage,
    availableLanguages: availableLanguages
      .filter((language) => !existingLanguages.includes(language))
      .map((language) => ({
        displayName: t(`languageNames:${language}`),
        name: language,
      }))
      .sort((a, b) => (a.displayName > b.displayName ? 1 : -1)),
  }
}

const initialState: State = {
  dialogsOpen: {
    addLanguage: false,
    removeLanguage: false,
  },
}

const Context = createContext<Context>({
  apiContextPath: '/',
  dispatch: (_) => {},
  state: initialState,
})

const Store = ({
  activeLanguage,
  apiContextPath = '/',
  children,
  existingLanguages,
  type,
  uiLocale = 'en',
}: Props) => {
  const [state, dispatch] = useReducer(Reducer, {
    ...initialState,
    activeLanguage,
    existingLanguages,
    ...(type === 'form' && {
      existingLanguages: existingLanguages ?? [activeLanguage ?? ''],
      forms: initialFormState,
    }),
    ...(type === 'list' && {
      activeLanguage: existingLanguages?.[0] ?? '',
      lists: initialListState,
    }),
  })
  const {t} = useTranslation()
  useEffect(() => {
    loadinitialState(apiContextPath, type, t, state.existingLanguages).then(
      ({availableLanguages, defaultLanguage}) => {
        dispatch(setDefaultLanguage(defaultLanguage))
        if (availableLanguages) {
          dispatch(setAvailableLanguages(availableLanguages))
        }
      },
    )
  }, [])
  return (
    <Context.Provider value={{apiContextPath, dispatch, state, uiLocale}}>
      {children}
    </Context.Provider>
  )
}

export {Context}
export type {DialogNames, State}
export default Store
