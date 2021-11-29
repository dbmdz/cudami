import {Message} from '../components/FeedbackMessage'
import {Action, ActionTypes} from './actionTypes'

/** Sets the active language */
export const setActiveLanguage = (language: string): Action => ({
  payload: {
    language,
  },
  type: ActionTypes.SET_ACTIVE_LANGUAGE,
})

/** Sets the default language */
export const setDefaultLanguage = (language: string): Action => ({
  payload: {
    language,
  },
  type: ActionTypes.SET_DEFAULT_LANGUAGE,
})

/** Sets a feedback message */
export const setFeedbackMessage = (message: Message): Action => ({
  payload: {
    message,
  },
  type: ActionTypes.SET_FEEDBACK_MESSAGE,
})

/** Toggles a dialog with the given name */
export const toggleDialog = (name: string): Action => ({
  payload: {
    name,
  },
  type: ActionTypes.TOGGLE_DIALOG,
})
