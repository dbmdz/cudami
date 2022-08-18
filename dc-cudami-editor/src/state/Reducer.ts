import {Action, ActionTypes} from './actionTypes'
import {DialogName, State} from './Store'

const Reducer = (state: State, action: Action<any>): State => {
  switch (action.type) {
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
          [action.payload.type]:
            !state.dialogsOpen[action.payload.type as DialogName],
        },
      }
    default:
      return state
  }
}

export default Reducer
