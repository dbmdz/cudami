export default {
  attrs: {
    alignment: {default: null},
    caption: {default: null},
    previewUrl: {default: null},
    previewResourceId: {default: null},
    resourceId: {default: null},
    title: {default: null},
    url: {default: null},
    width: {default: '33%'},
  },
  draggable: true,
  group: 'block',
  inline: false,
  parseDOM: [{tag: 'prosemirror-video'}],
  toDOM: () => ['prosemirror-video'],
}
