import {render} from 'react-dom'
import {publish, subscribe, unsubscribe} from 'pubsub-js'

import {createEditButton} from './utils'

class IframeView {
  constructor(node, view, getPos) {
    const {height, src, title, width} = node.attrs
    const dom = document.createElement('prosemirror-iframe')
    dom.setAttribute('class', 'd-inline-block position-relative')
    const iframe = document.createElement('iframe')
    iframe.setAttribute('class', 'editable')
    iframe.setAttribute('height', height)
    iframe.setAttribute('sandbox', '')
    iframe.setAttribute('src', src)
    if (title) {
      iframe.setAttribute('title', title)
    }
    iframe.setAttribute('width', width)
    dom.appendChild(iframe)
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
    const token = subscribe('editor.add-iframe', (_msg, data) => {
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
    publish('editor.show-iframe-modal', this.node.attrs)
  }

  selectNode() {
    this.dom.classList.add('ProseMirror-selectednode')
    const menu = document.createElement('span')
    menu.setAttribute('class', 'contentblock-menu')
    render(createEditButton(this.editContent), menu)
    this.dom.appendChild(menu)
  }
}

export default IframeView
