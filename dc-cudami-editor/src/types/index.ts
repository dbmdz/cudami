enum DialogNames {
  ADD_LANGUAGE = 'addLanguage',
  REMOVE_LANGUAGE = 'removeLanguage',
}

interface License extends UniqueObject {
  acronym?: string
  label?: Record<string, string>
  url?: string
}

interface UniqueObject {
  created?: Date
  lastModified?: Date
  uuid?: string
}

export {DialogNames}
export type {License}
