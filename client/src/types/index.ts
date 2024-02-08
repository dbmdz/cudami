enum DialogNames {
  ADD_LANGUAGE = 'addLanguage',
  REMOVE_LANGUAGE = 'removeLanguage',
}

type LocalizedText = Record<string, string>

interface Language {
  displayName: string
  name: string
}

interface License extends UniqueObject {
  acronym?: string
  label?: LocalizedText
  url?: string
}

interface FeedbackMessage {
  /** the color of the message */
  color?: string
  /** the translation key to use */
  key: string
  /** links that can be used in the translation string */
  links?: string[]
  /** a simple text to display instead of translated content */
  text?: string
  /** an key-value mapping of stuff to be interpolated into the translation string */
  values?: Record<string, unknown>
}

interface RenderingTemplate extends UniqueObject {
  description?: LocalizedText
  label?: LocalizedText
  name?: string
}

interface UniqueObject {
  created?: Date
  lastModified?: Date
  uuid?: string
}

export {DialogNames}
export type {FeedbackMessage, Language, License, RenderingTemplate}
