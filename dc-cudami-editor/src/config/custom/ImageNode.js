export default {
  image: {
    attrs: {
      alignment: {default: 'left'},
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
        tag: 'figure',
        getAttrs(dom) {
          const caption = dom.querySelector('figcaption')
          const image = dom.querySelector('img')
          const link = dom.querySelector('a[href]')
          const linkAttributes = {}
          if (link) {
            linkAttributes.linkNewTab = link.getAttribute('target') === '_blank'
            linkAttributes.linkUrl = link.getAttribute('href')
          }
          return {
            altText: image.getAttribute('alt'),
            caption: caption?.innerText,
            title: image.getAttribute('title'),
            url: image.getAttribute('src'),
            ...linkAttributes,
          }
        },
      },
    ],
    toDOM(node) {
      const {alignment, altText, caption, title, url, width} = node.attrs
      const tags = [
        'figure',
        {class: `alignment-${alignment} width-${parseInt(width)}`},
        ['img', {alt: altText, src: url, title}],
      ]
      if (caption) {
        tags.push(['figcaption', caption])
      }
      return tags
    },
  },
}
