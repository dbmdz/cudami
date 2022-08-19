import {DialogNames, FeedbackMessage, Language} from '../types'
import {Action, ActionTypes} from './actionTypes'

/** Adds a language */
export const addLanguage = (language: string): Action => ({
  payload: {
    language,
  },
  type: ActionTypes.ADD_LANGUAGE,
})

/** Adds a language */
export const removeLanguage = (language: Language): Action => ({
  payload: {
    language,
  },
  type: ActionTypes.REMOVE_LANGUAGE,
})

/** Sets the active language */
export const setActiveLanguage = (language: string): Action => ({
  payload: {
    language,
  },
  type: ActionTypes.SET_ACTIVE_LANGUAGE,
})

/** Sets the available languages */
export const setAvailableLanguages = (languages: Language[]): Action => ({
  payload: {
    languages,
  },
  type: ActionTypes.SET_AVAILABLE_LANGUAGES,
})

/** Sets the default language */
export const setDefaultLanguage = (language: string): Action => ({
  payload: {
    language,
  },
  type: ActionTypes.SET_DEFAULT_LANGUAGE,
})

/** Sets a feedback message */
export const setFeedbackMessage = (message?: FeedbackMessage): Action => ({
  payload: {
    message,
  },
  type: ActionTypes.SET_FEEDBACK_MESSAGE,
})

/** Toggles all url aliases */
export const toggleAllUrlAliases = (value: boolean): Action => ({
  payload: {
    value,
  },
  type: ActionTypes.TOGGLE_ALL_URL_ALIASES,
})

/** Toggles a dialog with the given name */
export const toggleDialog = (name: DialogNames): Action => ({
  payload: {
    name,
  },
  type: ActionTypes.TOGGLE_DIALOG,
})
