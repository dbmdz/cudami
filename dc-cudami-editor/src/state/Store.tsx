import {Dispatch, ReactNode, useReducer} from 'react'
import {createContext} from 'use-context-selector'

import {Message} from '../components/FeedbackMessage'
import {Action, PayloadTypes} from './actionTypes'
import {FormState, initialFormState} from './FormState'
import {ListState, initialListState} from './ListState'
import Reducer from './Reducer'

interface Context {
  apiContextPath?: string
  dispatch?: Dispatch<Action<PayloadTypes>>
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
  dialogsOpen: Record<string, boolean>
  /** a list of already existing languages for multilanguage fields */
  existingLanguages?: string[]
  /** a feedback message with all the needed information */
  feedbackMessage?: Message
  /** all the state that is only relevant for form components */
  forms?: FormState
  /** all the state that is only relevant for list components */
  lists?: ListState
}

const initialState: State = {
  dialogsOpen: {
    addAttachedIdentifiables: false,
    moveAttachedIdentifiable: false,
    removeAttachedIdentifiable: false,
  },
}

const Context = createContext<Context>({
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
  return (
    <Context.Provider value={{apiContextPath, dispatch, state, uiLocale}}>
      {children}
    </Context.Provider>
  )
}

export {Context}
export type {State}
export default Store
