import 'prosemirror-tables/style/tables.css'
import 'prosemirror-gapcursor/style/gapcursor.css'
import '@aeaton/prosemirror-footnotes/style/footnotes.css'

import {footnotes} from '@aeaton/prosemirror-footnotes'
import {dropCursor} from 'prosemirror-dropcursor'
import {gapCursor} from 'prosemirror-gapcursor'
import {history} from 'prosemirror-history'
import {tableEditing} from 'prosemirror-tables'

import {linkEditing, nodeViews} from './custom/plugins'
import keys from './keys'
import rules from './rules'

export default [
  rules,
  keys,
  footnotes(),
  dropCursor(),
  gapCursor(),
  history(),
  tableEditing(),
  // custom plugins
  linkEditing(),
  nodeViews(),
]

// for tables
document.execCommand('enableObjectResizing', false, false)
document.execCommand('enableInlineTableEditing', false, false)
