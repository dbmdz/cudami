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
        getAttrs: function(element) {
          return {
            height: element.getAttribute('height'),
            src: element.getAttribute('src'),
            width: element.getAttribute('width'),
          }
        },
      },
    ],
    toDOM: function(node) {
      return [
        'iframe',
        {
          class: 'editable',
          height: node.attrs.height,
          sandbox: '',
          src: node.attrs.src,
          width: node.attrs.width,
        },
      ]
    },
  },
}
