import {Message} from '../components/FeedbackMessage'

export enum ActionTypes {
  SET_ACTIVE_LANGUAGE = 'cudami-editor/SET_ACTIVE_LANGUAGE',
  SET_DEFAULT_LANGUAGE = 'cudami-editor/SET_DEFAULT_LANGUAGE',
  SET_FEEDBACK_MESSAGE = 'cudami-editor/SET_FEEDBACK_MESSAGE',
  TOGGLE_ALL_URL_ALIASES = 'cudami-editor/SHOW_ALL_URL_ALIASES',
  TOGGLE_DIALOG = 'cudami-editor/TOGGLE_DIALOG',
}

type BooleanTypes = ActionTypes.TOGGLE_ALL_URL_ALIASES

interface BooleanAction {
  payload: Record<string, boolean>
  type: BooleanTypes
}

type MessageTypes = ActionTypes.SET_FEEDBACK_MESSAGE

interface MessageAction {
  payload: Record<string, Message | undefined>
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

export type Action = BooleanAction | MessageAction | StringAction
