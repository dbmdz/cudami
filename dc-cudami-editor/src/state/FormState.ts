import {Language} from '../types'

export interface FormState {
  availableLanguages?: Language[]
  showAllUrlAliases: boolean
}

export const initialFormState: FormState = {
  showAllUrlAliases: false,
}
