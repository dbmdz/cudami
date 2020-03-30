export default {
  iframe: {
    attrs: {
      height: {},
      src: {},
      width: {},
    },
    draggable: true,
    group: 'block',
    inline: false,
    parseDOM: [
      {
        tag: 'iframe[src]',
        getAttrs: function (element) {
          return {
            height: element.getAttribute('height'),
            src: element.getAttribute('src'),
            width: element.getAttribute('width'),
          }
        },
      },
    ],
    toDOM: function (node) {
      const {height, src, width} = node.attrs
      return [
        'iframe',
        {
          class: 'editable',
          height,
          sandbox: '',
          src,
          width,
        },
      ]
    },
  },
}
