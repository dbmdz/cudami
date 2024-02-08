import sortBy from 'lodash-es/sortBy'

import {Action, ActionTypes} from './actionTypes'
import {State} from './Store'

const Reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case ActionTypes.ADD_LANGUAGE:
      const {language: newLanguage} = action.payload
      return {
        ...state,
        activeLanguage: newLanguage,
        existingLanguages: [...(state.existingLanguages ?? []), newLanguage],
        ...(state.forms && {
          forms: {
            ...state.forms,
            availableLanguages:
              state.forms?.availableLanguages?.filter(
                (lang) => lang.name !== newLanguage,
              ) ?? [],
          },
        }),
      }
    case ActionTypes.REMOVE_LANGUAGE:
      const {language: languageToRemove} = action.payload
      return {
        ...state,
        activeLanguage:
          state.activeLanguage === languageToRemove.name
            ? state.existingLanguages?.[0] ?? ''
            : state.activeLanguage,
        existingLanguages: state.existingLanguages?.filter(
          (lang) => lang != languageToRemove.name,
        ),
        ...(state.forms && {
          forms: {
            ...state.forms,
            availableLanguages: sortBy(
              [...(state.forms.availableLanguages ?? []), languageToRemove],
              'displayName',
            ),
          },
        }),
      }
    case ActionTypes.SET_ACTIVE_LANGUAGE:
      return {
        ...state,
        activeLanguage: action.payload.language,
      }
    case ActionTypes.SET_AVAILABLE_LANGUAGES:
      return {
        ...state,
        ...(state.forms && {
          forms: {
            ...state.forms,
            availableLanguages: action.payload.languages,
          },
        }),
      }
    case ActionTypes.SET_DEFAULT_LANGUAGE:
      return {
        ...state,
        defaultLanguage: action.payload.language,
      }
    case ActionTypes.SET_FEEDBACK_MESSAGE:
      return {
        ...state,
        feedbackMessage: action.payload.message,
      }
    case ActionTypes.TOGGLE_ALL_URL_ALIASES:
      if (!state.forms) {
        throw new Error()
      }
      return {
        ...state,
        forms: {
          ...state.forms,
          showAllUrlAliases: action.payload.value,
        },
      }
    case ActionTypes.TOGGLE_DIALOG:
      return {
        ...state,
        dialogsOpen: {
          ...state.dialogsOpen,
          [action.payload.name]: !state.dialogsOpen[action.payload.name],
        },
      }
    default:
      return state
  }
}

export default Reducer
