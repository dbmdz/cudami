export enum ActionTypes {
  SET_DEFAULT_LANGUAGE = 'cudami-editor/SET_DEFAULT_LANGUAGE',
  TOGGLE_DIALOG = 'cudami-editor/TOGGLE_DIALOG',
}

export interface Action {
  payload: {[key: string]: string}
  type: ActionTypes
}

/**
 * Sets the default language
 *
 * @param {String} language
 * @return the action
 */
export function setDefaultLanguage(language: string): Action {
  return {
    payload: {
      language,
    },
    type: ActionTypes.SET_DEFAULT_LANGUAGE,
  }
}

/**
 * Toggles a dialog with the given name
 *
 * @param {String} name
 * @return the action
 */
export function toggleDialog(name: string): Action {
  return {
    payload: {
      name,
    },
    type: ActionTypes.TOGGLE_DIALOG,
  }
}
