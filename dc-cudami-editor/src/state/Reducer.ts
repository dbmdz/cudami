import {Action, ActionTypes} from './actions'
import {State} from './Store'

const Reducer = (state: State, {payload, type}: Action) => {
  switch (type) {
    case ActionTypes.SET_DEFAULT_LANGUAGE:
      return {
        ...state,
        defaultLanguage: payload.language,
      }
    case ActionTypes.TOGGLE_DIALOG:
      return {
        ...state,
        dialogsOpen: {
          ...state.dialogsOpen,
          [payload.name]: !state.dialogsOpen[payload.name],
        },
      }
    default:
      return state
  }
}

export default Reducer
