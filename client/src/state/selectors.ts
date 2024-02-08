import {useContextSelector} from 'use-context-selector'

import {DialogNames, FeedbackMessage, Language} from '../types'
import {Context} from './Store'

/** Gets the active language */
export const getActiveLanguage = (): string =>
  useContextSelector(Context, ({state}) => state.activeLanguage ?? '')

export const getAvailableLanguages = (): Language[] =>
  useContextSelector(
    Context,
    ({state}) => state.forms?.availableLanguages ?? [],
  )

/** Gets the default language */
export const getDefaultLanguage = (): string =>
  useContextSelector(Context, ({state}) => state.defaultLanguage ?? '')

/** Gets the open state of the dialogs */
export const getDialogsOpen = (): Record<DialogNames, boolean> =>
  useContextSelector(Context, ({state}) => state.dialogsOpen)

/** Gets the existing languagea */
export const getExistingLanguages = (): string[] =>
  useContextSelector(Context, ({state}) => state.existingLanguages ?? [])

/** Gets a feedback message */
export const getFeedbackMessage = (): FeedbackMessage | undefined =>
  useContextSelector(Context, ({state}) => state.feedbackMessage)

/** Checks if all url aliases should be shown */
export const getShowAllUrlAliases = (): boolean =>
  useContextSelector(
    Context,
    ({state}) => state.forms?.showAllUrlAliases ?? false,
  )
