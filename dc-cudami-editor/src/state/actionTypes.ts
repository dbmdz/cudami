import {Message} from '../components/FeedbackMessage'

export enum ActionTypes {
  SET_ACTIVE_LANGUAGE = 'cudami-editor/SET_ACTIVE_LANGUAGE',
  SET_DEFAULT_LANGUAGE = 'cudami-editor/SET_DEFAULT_LANGUAGE',
  SET_FEEDBACK_MESSAGE = 'cudami-editor/SET_FEEDBACK_MESSAGE',
  TOGGLE_ALL_URL_ALIASES = 'cudami-editor/SHOW_ALL_URL_ALIASES',
  TOGGLE_DIALOG = 'cudami-editor/TOGGLE_DIALOG',
}

export type PayloadTypes = boolean | string | Message | undefined

export interface Action<P extends PayloadTypes> {
  payload: Record<string, P>
  type: ActionTypes
}
