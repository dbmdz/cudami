import {Dispatch, ReactNode, useReducer} from 'react'
import {createContext} from 'use-context-selector'

import {Message} from '../components/FeedbackMessage'
import {Action} from './actions'
import {FormState, initialFormState} from './FormState'
import {ListState, initialListState} from './ListState'
import Reducer from './Reducer'

interface Context {
  apiContextPath?: string
  dispatch?: Dispatch<Action>
  existingLanguages?: string[]
  state: State
  uiLocale?: string
}

interface Props {
  /** the context path of the surrounding java application */
  apiContextPath: string
  /** the children who use the store */
  children: ReactNode
  /** a list of existing languages for multilanguage fields */
  existingLanguages: string[]
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
  apiContextPath = '/',
  children,
  existingLanguages = [],
  type,
  uiLocale = 'en',
}: Props) => {
  const [state, dispatch] = useReducer(Reducer, {
    ...initialState,
    activeLanguage: existingLanguages[0] ?? '',
    ...(type === 'form' && {forms: initialFormState}),
    ...(type === 'list' && {lists: initialListState}),
  })
  return (
    <Context.Provider
      value={{apiContextPath, dispatch, existingLanguages, state, uiLocale}}
    >
      {children}
    </Context.Provider>
  )
}

export {Context}
export type {State}
export default Store
