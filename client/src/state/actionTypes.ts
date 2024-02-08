import {DialogNames, FeedbackMessage, Language} from '../types'

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

export type Action =
  | {
      payload: Record<string, string>
      type: ActionTypes.ADD_LANGUAGE
    }
  | {
      payload: Record<string, Language>
      type: ActionTypes.REMOVE_LANGUAGE
    }
  | {
      payload: Record<string, string>
      type: ActionTypes.SET_ACTIVE_LANGUAGE
    }
  | {
      payload: Record<string, Language[]>
      type: ActionTypes.SET_AVAILABLE_LANGUAGES
    }
  | {
      payload: Record<string, string>
      type: ActionTypes.SET_DEFAULT_LANGUAGE
    }
  | {
      payload: Record<string, FeedbackMessage | undefined>
      type: ActionTypes.SET_FEEDBACK_MESSAGE
    }
  | {
      payload: Record<string, boolean>
      type: ActionTypes.TOGGLE_ALL_URL_ALIASES
    }
  | {
      payload: Record<string, DialogNames>
      type: ActionTypes.TOGGLE_DIALOG
    }
