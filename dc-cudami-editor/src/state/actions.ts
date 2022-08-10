import {Message} from '../components/FeedbackMessage'
import {Action, ActionTypes} from './actionTypes'
import {Language} from './FormState'

/** Sets the active language */
export const setActiveLanguage = (language: string): Action<string> => ({
  payload: {
    language,
  },
  type: ActionTypes.SET_ACTIVE_LANGUAGE,
})

/** Sets the available languages */
export const setAvailableLanguages = (
  languages: Language[],
): Action<Language[]> => ({
  payload: {
    languages,
  },
  type: ActionTypes.SET_AVAILABLE_LANGUAGES,
})

/** Sets the default language */
export const setDefaultLanguage = (language: string): Action<string> => ({
  payload: {
    language,
  },
  type: ActionTypes.SET_DEFAULT_LANGUAGE,
})

/** Sets a feedback message */
export const setFeedbackMessage = (
  message: Message | undefined,
): Action<Message | undefined> => ({
  payload: {
    message,
  },
  type: ActionTypes.SET_FEEDBACK_MESSAGE,
})

/** Toggles all url aliases */
export const toggleAllUrlAliases = (value: boolean): Action<boolean> => ({
  payload: {
    value,
  },
  type: ActionTypes.TOGGLE_ALL_URL_ALIASES,
})

/** Toggles a dialog with the given name */
export const toggleDialog = (name: string): Action<string> => ({
  payload: {
    name,
  },
  type: ActionTypes.TOGGLE_DIALOG,
})
