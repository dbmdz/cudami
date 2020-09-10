import React from 'react'
import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {render} from 'react-dom'

import EditButton from '../../components/EditButton'

class IframeView {
  constructor(node, view, getPos) {
    const {height, src, title, width} = node.attrs
    const dom = document.createElement('prosemirror-iframe')
    dom.classList.add('d-inline-block', 'position-relative')
    dom.style.height = height
    dom.style.width = width
    const iframe = document.createElement('iframe')
    iframe.classList.add('editable')
    iframe.setAttribute('height', '100%')
    iframe.setAttribute('sandbox', '')
    iframe.setAttribute('src', src)
    iframe.setAttribute('width', '100%')
    if (title) {
      iframe.setAttribute('title', title)
    }
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
    publish('editor.show-iframe-modal', {...this.node.attrs, editing: true})
  }

  selectNode() {
    this.dom.classList.add('ProseMirror-selectednode')
    const menu = document.createElement('span')
    menu.classList.add('contentblock-menu')
    render(<EditButton onClick={this.editContent} />, menu)
    this.dom.appendChild(menu)
  }
}

export default IframeView
