import {Message} from '../components/FeedbackMessage'
import {Language} from './FormState'

export enum ActionTypes {
  ADD_LANGUAGE = 'cudami-editor/ADD_LANGUAGE',
  REMOVE_LANGUAGE = 'cudami-editor/REMOVE_LANGUAGE',
  SET_ACTIVE_LANGUAGE = 'cudami-editor/SET_ACTIVE_LANGUAGE',
  SET_AVAILABLE_LANGUAGES = 'cudami-editor/SET_AVAILABLE_LANGUAGES',
  SET_DEFAULT_LANGUAGE = 'cudami-editor/SET_DEFAULT_LANGUAGE',
  SET_FEEDBACK_MESSAGE = 'cudami-editor/SET_FEEDBACK_MESSAGE',
  TOGGLE_ALL_URL_ALIASES = 'cudami-editor/SHOW_ALL_URL_ALIASES',
  TOGGLE_DIALOG = 'cudami-editor/TOGGLE_DIALOG',
}

export type PayloadTypes =
  | boolean
  | string
  | Language
  | Language[]
  | Message
  | undefined

export interface Action<P extends PayloadTypes> {
  payload: Record<string, P>
  type: ActionTypes
}
