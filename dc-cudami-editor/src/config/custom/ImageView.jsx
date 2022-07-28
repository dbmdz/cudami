import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {render} from 'react-dom'

import EditButton from '../../components/editor/EditButton'

class ImageView {
  constructor(node, view, getPos) {
    const {alignment, altText, caption, title, url, width} = node.attrs
    const dom = document.createElement('prosemirror-image')
    dom.classList.add(
      'd-inline-block',
      'position-relative',
      `width-${parseInt(width)}`,
    )
    if (alignment) {
      dom.classList.add(`alignment-${alignment}`)
    }
    const figure = document.createElement('figure')
    figure.classList.add('editable', 'mb-0')
    const image = document.createElement('img')
    image.setAttribute('src', url)
    if (altText) {
      image.setAttribute('alt', altText)
    }
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

  editImage = () => {
    const token = subscribe('editor.add-image', (_msg, data) => {
      const {dispatch, state} = this.view
      const transaction = state.tr.setNodeMarkup(this.getPos(), undefined, data)
      dispatch(transaction)
      unsubscribe(token)
    })
    publish('editor.show-image-dialog', {
      attributes: this.node.attrs,
      editing: true,
    })
  }

  selectNode() {
    this.dom.classList.add('ProseMirror-selectednode')
    const menu = document.createElement('span')
    menu.classList.add('contentblock-menu')
    menu.style.zIndex = 1
    render(
      <EditButton onClick={this.editImage} titleKey="insert.image.edit" />,
      menu,
    )
    this.dom.appendChild(menu)
  }
}

export default ImageView
