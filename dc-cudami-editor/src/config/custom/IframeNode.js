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
  parseDOM: [{tag: 'prosemirror-iframe'}],
  toDOM: () => ['prosemirror-iframe'],
}
