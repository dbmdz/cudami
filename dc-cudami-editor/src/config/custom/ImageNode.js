export default {
  image: {
    attrs: {
      altText: {default: null},
      caption: {default: null},
      linkNewTab: {default: false},
      linkUrl: {default: null},
      title: {default: null},
      url: {default: null},
    },
    draggable: true,
    group: 'inline',
    inline: true,
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
      const {altText, caption, linkNewTab, linkUrl, title, url} = node.attrs
      const tags = ['figure']
      const imageTag = ['img', {alt: altText, src: url, title}]
      if (linkUrl) {
        tags.push([
          'a',
          {href: linkUrl, target: linkNewTab ? '_blank' : null},
          imageTag,
        ])
      } else {
        tags.push(imageTag)
      }
      if (caption) {
        tags.push(['figcaption', caption])
      }
      return tags
    },
  },
}
