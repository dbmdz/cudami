import {history} from 'prosemirror-history'
import {dropCursor} from 'prosemirror-dropcursor'
import {gapCursor} from 'prosemirror-gapcursor'
import {columnResizing, tableEditing} from 'prosemirror-tables'
import {footnotes} from '@aeaton/prosemirror-footnotes'

import 'prosemirror-tables/style/tables.css'
import 'prosemirror-gapcursor/style/gapcursor.css'
import '@aeaton/prosemirror-footnotes/style/footnotes.css'

import keys from './keys'
import rules from './rules'
import customPlugin from './custom/Plugin'

export default [
  rules,
  keys,
  footnotes(),
  dropCursor(),
  gapCursor(),
  history(),
  columnResizing(),
  tableEditing(),
  customPlugin(),
]

// for tables
document.execCommand('enableObjectResizing', false, false)
document.execCommand('enableInlineTableEditing', false, false)
