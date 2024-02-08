import {getAttributes} from '../utils'

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
  parseDOM: [
    {
      tag: 'prosemirror-video',
      getAttrs: (dom) =>
        getAttributes(
          [
            'alignment',
            'caption',
            'previewUrl',
            'previewResourceId',
            'resourceId',
            'title',
            'url',
            'width',
          ],
          dom,
        ),
    },
  ],
  toDOM: (node) => ['prosemirror-video', node.attrs],
}
