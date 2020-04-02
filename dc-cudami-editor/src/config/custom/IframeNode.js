export default {
  iframe: {
    attrs: {
      height: {default: '150px'},
      src: {},
      title: {default: null},
      width: {default: '300px'},
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
            title: element.getAttribute('title'),
            width: element.getAttribute('width'),
          }
        },
      },
    ],
    toDOM: function (node) {
      const {height, src, title, width} = node.attrs
      return [
        'iframe',
        {
          class: 'editable',
          height,
          sandbox: '',
          src,
          title,
          width,
        },
      ]
    },
  },
}
