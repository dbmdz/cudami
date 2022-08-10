export interface Language {
  displayName: string
  name: string
}

export interface FormState {
  availableLanguages?: Language[]
  showAllUrlAliases: boolean
}

export const initialFormState: FormState = {
  showAllUrlAliases: false,
}
