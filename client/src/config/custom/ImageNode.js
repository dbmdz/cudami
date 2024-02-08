import {getAttributes} from '../utils'

export default {
  attrs: {
    alignment: {default: null},
    altText: {default: null},
    caption: {default: null},
    linkNewTab: {default: true},
    linkUrl: {default: null},
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
      tag: 'prosemirror-image',
      getAttrs: (dom) =>
        getAttributes(
          [
            'alignment',
            'altText',
            'caption',
            'linkNewTab',
            'linkUrl',
            'resourceId',
            'title',
            'url',
            'width',
          ],
          dom,
        ),
    },
  ],
  toDOM: (node) => ['prosemirror-image', node.attrs],
}
