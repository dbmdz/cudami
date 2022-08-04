import {useContextSelector} from 'use-context-selector'

import {Message} from '../components/FeedbackMessage'
import {Context} from './Store'

/** Gets the active language */
export const getActiveLanguage = (): string =>
  useContextSelector(Context, ({state}) => state.activeLanguage ?? '')

/** Gets the default language */
export const getDefaultLanguage = (): string =>
  useContextSelector(Context, ({state}) => state.defaultLanguage ?? '')

/** Gets the existing languagea */
export const getExistingLanguages = (): string[] =>
  useContextSelector(Context, ({state}) => state.existingLanguages ?? [])

/** Gets a feedback message */
export const getFeedbackMessage = (): Message | undefined =>
  useContextSelector(Context, ({state}) => state.feedbackMessage)

/** Checks if a dialog is open */
export const isDialogOpen = (name: string): boolean =>
  useContextSelector(Context, ({state}) => state.dialogsOpen[name])

export const getShowAllUrlAliases = (): boolean =>
  useContextSelector(
    Context,
    ({state}) => state.forms?.showAllUrlAliases ?? false,
  )
