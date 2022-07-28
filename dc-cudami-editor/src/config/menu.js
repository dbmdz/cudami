import {
  joinUp,
  lift,
  setBlockType,
  toggleMark,
  wrapIn,
} from 'prosemirror-commands'
import {redo, undo} from 'prosemirror-history'
import {wrapInList} from 'prosemirror-schema-list'
import {
  addColumnAfter,
  addColumnBefore,
  addRowAfter,
  addRowBefore,
  deleteColumn,
  deleteRow,
  deleteTable,
  isInTable,
} from 'prosemirror-tables'
import {publish, subscribe, unsubscribe} from 'pubsub-js'

import icons from './icons'
import schema from './schema'
import {markActive} from './utils'

const blockActive =
  (type, attrs = {}) =>
  (state) => {
    const {$from, to, node} = state.selection

    if (node) {
      return node.hasMarkup(type, attrs)
    }

    return to <= $from.end() && $from.parent.hasMarkup(type, attrs)
  }

const canInsert = (type) => (state) => {
  const {$from} = state.selection

  for (let d = $from.depth; d >= 0; d--) {
    const index = $from.index(d)

    if ($from.node(d).canReplaceWith(index, index, type)) {
      return true
    }
  }

  return false
}

const headingLevels = [1, 2, 3, 4, 5, 6]

const headingActive = () => (state) => {
  let active = false
  for (let level of headingLevels) {
    if (blockActive(schema.nodes.heading, {level})(state)) {
      active = true
      break
    }
  }
  return active
}

export default function (t) {
  return {
    history: {
      undo: {
        titleKey: 'history.undo',
        content: icons.undo,
        enable: undo,
        run: undo,
      },
      redo: {
        titleKey: 'history.redo',
        content: icons.redo,
        enable: redo,
        run: redo,
      },
    },
    marks: {
      strong: {
        titleKey: 'marks.strong',
        content: icons.strong,
        active: markActive(schema.marks.strong),
        enable: toggleMark(schema.marks.strong),
        run: toggleMark(schema.marks.strong),
      },
      em: {
        titleKey: 'marks.em',
        content: icons.em,
        active: markActive(schema.marks.em),
        enable: toggleMark(schema.marks.em),
        run: toggleMark(schema.marks.em),
      },
      underline: {
        titleKey: 'marks.underline',
        content: icons.underline,
        active: markActive(schema.marks.underline),
        enable: toggleMark(schema.marks.underline),
        run: toggleMark(schema.marks.underline),
      },
      strikethrough: {
        titleKey: 'marks.strikethrough',
        content: icons.strikethrough,
        active: markActive(schema.marks.strikethrough),
        enable: toggleMark(schema.marks.strikethrough),
        run: toggleMark(schema.marks.strikethrough),
      },
      superscript: {
        titleKey: 'marks.superscript',
        content: icons.superscript,
        active: markActive(schema.marks.superscript),
        enable: toggleMark(schema.marks.superscript),
        run: toggleMark(schema.marks.superscript),
      },
      subscript: {
        titleKey: 'marks.subscript',
        content: icons.subscript,
        active: markActive(schema.marks.subscript),
        enable: toggleMark(schema.marks.subscript),
        run: toggleMark(schema.marks.subscript),
      },
      code: {
        titleKey: 'marks.code',
        content: icons.code,
        active: markActive(schema.marks.code),
        enable: toggleMark(schema.marks.code),
        run: toggleMark(schema.marks.code),
      },
      link: {
        titleKeyActive: 'marks.link.remove',
        titleKeyInactive: 'marks.link.insert',
        contentActive: icons.unlink,
        contentInactive: icons.link,
        active: markActive(schema.marks.link),
        enable: (state) =>
          !state.selection.empty && toggleMark(schema.marks.link)(state),
        run(state, dispatch) {
          if (markActive(schema.marks.link)(state)) {
            toggleMark(schema.marks.link)(state, dispatch)
            return true
          }

          const token = subscribe('editor.add-link', (_msg, data) => {
            if (!data.href) {
              return false
            }
            toggleMark(schema.marks.link, data)(state, dispatch)
            unsubscribe(token)
          })
          publish('editor.show-link-dialog')
        },
      },
    },
    blocks: {
      plain: {
        titleKey: 'blocks.paragraph',
        content: icons.paragraph,
        active: blockActive(schema.nodes.paragraph),
        enable: setBlockType(schema.nodes.paragraph),
        run: setBlockType(schema.nodes.paragraph),
      },
      code_block: {
        titleKey: 'blocks.codeBlock',
        content: icons.code_block,
        active: blockActive(schema.nodes.code_block),
        enable: setBlockType(schema.nodes.code_block),
        run: setBlockType(schema.nodes.code_block),
      },
      heading: {
        titleKey: 'blocks.heading',
        content: icons.heading,
        active: headingActive(),
        children: headingLevels.map((level) => ({
          active: blockActive(schema.nodes.heading, {level}),
          content: t('editor:blocks.headingLevel', {level}),
          enable: setBlockType(schema.nodes.heading, {level}),
          run: setBlockType(schema.nodes.heading, {level}),
        })),
      },
      blockquote: {
        titleKey: 'blocks.blockquote',
        content: icons.blockquote,
        active: blockActive(schema.nodes.blockquote),
        enable: wrapIn(schema.nodes.blockquote),
        run: wrapIn(schema.nodes.blockquote),
      },
      bullet_list: {
        titleKey: 'blocks.bulletList',
        content: icons.bullet_list,
        active: blockActive(schema.nodes.bullet_list),
        enable: wrapInList(schema.nodes.bullet_list),
        run: wrapInList(schema.nodes.bullet_list),
      },
      ordered_list: {
        titleKey: 'blocks.orderedList',
        content: icons.ordered_list,
        active: blockActive(schema.nodes.ordered_list),
        enable: wrapInList(schema.nodes.ordered_list),
        run: wrapInList(schema.nodes.ordered_list),
      },
      lift: {
        titleKey: 'blocks.lift',
        content: icons.lift,
        enable: lift,
        run: lift,
      },
      join_up: {
        titleKey: 'blocks.joinUp',
        content: icons.join_up,
        enable: joinUp,
        run: joinUp,
      },
    },
    insert: {
      image: {
        titleKey: 'insert.image.new',
        content: icons.image,
        enable: canInsert(schema.nodes.image),
        run: (state, dispatch) => {
          const token = subscribe('editor.add-image', (_msg, data) => {
            const image = schema.nodes.image.createAndFill(data)
            dispatch(state.tr.replaceSelectionWith(image))
            unsubscribe(token)
          })
          publish('editor.show-image-dialog')
        },
      },
      video: {
        titleKey: 'insert.video.new',
        content: icons.video,
        enable: canInsert(schema.nodes.video),
        run: (state, dispatch) => {
          const token = subscribe('editor.add-video', (_msg, data) => {
            const video = schema.nodes.video.createAndFill(data)
            dispatch(state.tr.replaceSelectionWith(video))
            unsubscribe(token)
          })
          publish('editor.show-video-dialog')
        },
      },
      hr: {
        titleKey: 'insert.hr',
        content: icons.hr,
        enable: canInsert(schema.nodes.horizontal_rule),
        run: (state, dispatch) => {
          const hr = schema.nodes.horizontal_rule.create()
          dispatch(state.tr.replaceSelectionWith(hr))
        },
      },
      iframe: {
        titleKey: 'insert.iframe.new',
        content: icons.iframe,
        enable: canInsert(schema.nodes.iframe),
        run: (state, dispatch) => {
          const token = subscribe('editor.add-iframe', (_msg, data) => {
            const iframe = schema.nodes.iframe.createAndFill(data)
            dispatch(state.tr.replaceSelectionWith(iframe))
            unsubscribe(token)
          })
          publish('editor.show-iframe-dialog')
        },
      },
      table: {
        titleKey: 'insert.table',
        content: icons.table,
        enable: canInsert(schema.nodes.table),
        run: (state, dispatch) => {
          const token = subscribe('editor.add-table', (_msg, data) => {
            let columnCount = data.columns
            const cells = []
            while (columnCount--) {
              cells.push(schema.nodes.table_cell.createAndFill())
            }

            let rowCount = data.rows
            const rows = []
            while (rowCount--) {
              rows.push(schema.nodes.table_row.createAndFill(null, cells))
            }

            const table = schema.nodes.table.createAndFill(null, rows)
            dispatch(state.tr.replaceSelectionWith(table))
            unsubscribe(token)
          })
          publish('editor.show-table-dialog')
        },
      },
    },
    table: {
      addRowBefore: {
        titleKey: 'table.addRowBefore',
        content: icons.addRowBefore,
        enable: isInTable,
        run: addRowBefore,
      },
      addRowAfter: {
        titleKey: 'table.addRowAfter',
        content: icons.addRowAfter,
        enable: isInTable,
        run: addRowAfter,
      },
      deleteRow: {
        titleKey: 'table.deleteRow',
        content: icons.deleteRow,
        enable: isInTable,
        run: deleteRow,
      },
      addColumnBefore: {
        titleKey: 'table.addColumnBefore',
        content: icons.addColumnBefore,
        enable: isInTable,
        run: addColumnBefore,
      },
      addColumnAfter: {
        titleKey: 'table.addColumnAfter',
        content: icons.addColumnAfter,
        enable: isInTable,
        run: addColumnAfter,
      },
      deleteColumn: {
        titleKey: 'table.deleteColumn',
        content: icons.deleteColumn,
        enable: isInTable,
        run: deleteColumn,
      },
      deleteTable: {
        titleKey: 'table.deleteTable',
        content: icons.deleteTable,
        enable: isInTable,
        run: deleteTable,
      },
    },
  }
}
