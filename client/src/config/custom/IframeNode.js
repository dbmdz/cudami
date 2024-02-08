import {getAttributes} from '../utils'

export default {
  attrs: {
    height: {default: '150px'},
    src: {default: null},
    title: {default: null},
    width: {default: '300px'},
  },
  draggable: true,
  group: 'block',
  inline: false,
  parseDOM: [
    {
      tag: 'prosemirror-iframe',
      getAttrs: (dom) =>
        getAttributes(['height', 'src', 'title', 'width'], dom),
    },
  ],
  toDOM: (node) => ['prosemirror-iframe', node.attrs],
}
