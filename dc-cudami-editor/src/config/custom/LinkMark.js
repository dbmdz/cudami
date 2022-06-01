import {getAttributes} from '../utils'

export default {
  attrs: {
    href: {default: null},
    title: {default: null},
  },
  inclusive: false,
  parseDOM: [
    {
      tag: 'a[href]',
      getAttrs: (dom) => getAttributes(['href', 'title'], dom),
    },
  ],
  toDOM: (node) => ['a', node.attrs],
}
