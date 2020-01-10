export default {
  image: {
    attrs: {
      alt: {default: null},
      src: {},
      title: {default: null},
    },
    draggable: true,
    group: 'inline',
    inline: true,
    parseDOM: [
      {
        tag: 'img[src]',
        getAttrs(dom) {
          return {
            src: dom.getAttribute('src'),
            title: dom.getAttribute('title'),
            alt: dom.getAttribute('alt'),
          }
        },
      },
    ],
    toDOM(node) {
      let {alt, src, title} = node.attrs
      return [
        'img',
        {
          alt,
          src,
          title,
        },
      ]
    },
  },
}
