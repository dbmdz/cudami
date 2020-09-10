export default {
  link: {
    attrs: {
      href: {default: null},
      title: {default: null},
    },
    inclusive: false,
    parseDOM: [
      {
        tag: 'a[href]',
        getAttrs(dom) {
          return {
            href: dom.getAttribute('href'),
            title: dom.getAttribute('title'),
          }
        },
      },
    ],
    toDOM(node) {
      let {href, title} = node.attrs
      return ['a', {href, title}, 0]
    },
  },
}
