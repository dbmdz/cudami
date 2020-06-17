import {render} from 'react-dom'
import {publish, subscribe, unsubscribe} from 'pubsub-js'

import {createEditButton} from './utils'

class IframeView {
  constructor(node, view, getPos) {
    const {alignment, altText, caption, title, url, width} = node.attrs
    const dom = document.createElement('prosemirror-image')
    dom.setAttribute('class', `alignment-${alignment} width-${parseInt(width)}`)
    const figure = document.createElement('figure')
    const image = document.createElement('img')
    image.setAttribute('alt', altText)
    image.setAttribute('src', url)
    if (title) {
      image.setAttribute('title', title)
    }
    figure.appendChild(image)
    if (caption) {
      const figcaption = document.createElement('figcaption')
      figcaption.innerText = caption
      figure.appendChild(figcaption)
    }
    dom.appendChild(figure)
    this.dom = dom
    this.getPos = getPos
    this.node = node
    this.view = view
  }

  deselectNode() {
    this.dom.classList.remove('ProseMirror-selectednode')
    this.dom.removeChild(this.dom.lastChild)
  }

  editContent = () => {
    const token = subscribe('editor.add-image', (_msg, data) => {
      const newAttrs = {
        ...this.node.attrs,
        ...data,
      }
      const {dispatch, state} = this.view
      const transaction = state.tr.setNodeMarkup(
        this.getPos(),
        undefined,
        newAttrs
      )
      dispatch(transaction)
      unsubscribe(token)
    })
    publish('editor.show-image-modal', {
      ...this.node.attrs,
      showImageSelector: false,
    })
  }

  selectNode() {
    this.dom.classList.add('ProseMirror-selectednode')
    const menu = document.createElement('span')
    render(createEditButton(this.editContent), menu)
    this.dom.appendChild(menu)
  }
}

export default IframeView
