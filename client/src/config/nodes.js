import {footnoteNodes} from '@aeaton/prosemirror-footnotes'
import {nodes} from 'prosemirror-schema-basic'
import {bulletList, listItem, orderedList} from 'prosemirror-schema-list'
import {tableNodes} from 'prosemirror-tables'

import iframe from './custom/IframeNode'
import image from './custom/ImageNode'
import video from './custom/VideoNode'

const listNodes = {
  ordered_list: {
    ...orderedList,
    content: 'list_item+',
    group: 'block',
  },
  bullet_list: {
    ...bulletList,
    content: 'list_item+',
    group: 'block',
  },
  list_item: {
    ...listItem,
    content: 'paragraph block*',
  },
}

export default {
  ...nodes,
  ...listNodes,
  ...tableNodes({
    tableGroup: 'block',
    cellContent: 'block+',
  }),
  ...footnoteNodes,
  // custom nodes
  iframe,
  image,
  video,
}
