import {Message} from '../components/FeedbackMessage'

export enum ActionTypes {
  SET_ACTIVE_LANGUAGE = 'cudami-editor/SET_ACTIVE_LANGUAGE',
  SET_DEFAULT_LANGUAGE = 'cudami-editor/SET_DEFAULT_LANGUAGE',
  SET_FEEDBACK_MESSAGE = 'cudami-editor/SET_FEEDBACK_MESSAGE',
  TOGGLE_DIALOG = 'cudami-editor/TOGGLE_DIALOG',
}

type MessageTypes = ActionTypes.SET_FEEDBACK_MESSAGE

interface MessageAction {
  payload: Record<string, Message>
  type: MessageTypes
}

type StringTypes =
  | ActionTypes.SET_ACTIVE_LANGUAGE
  | ActionTypes.SET_DEFAULT_LANGUAGE
  | ActionTypes.TOGGLE_DIALOG

interface StringAction {
  payload: Record<string, string>
  type: StringTypes
}

export type Action = MessageAction | StringAction

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
