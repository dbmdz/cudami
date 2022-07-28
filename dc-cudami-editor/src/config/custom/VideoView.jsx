import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {render} from 'react-dom'

import EditButton from '../../components/editor/EditButton'

class VideoView {
  constructor(node, view, getPos) {
    const {alignment, caption, previewUrl, title, url, width} = node.attrs
    const dom = document.createElement('prosemirror-video')
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
    const video = document.createElement('video')
    video.setAttribute('controls', '')
    video.setAttribute('preload', 'metadata')
    video.setAttribute('src', url)
    if (previewUrl) {
      video.setAttribute('poster', previewUrl)
    }
    if (title) {
      video.setAttribute('title', title)
    }
    figure.appendChild(video)
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

  editVideo = () => {
    const token = subscribe('editor.add-video', (_msg, data) => {
      const {dispatch, state} = this.view
      const transaction = state.tr.setNodeMarkup(this.getPos(), undefined, data)
      dispatch(transaction)
      unsubscribe(token)
    })
    publish('editor.show-video-dialog', {
      attributes: this.node.attrs,
      editing: true,
    })
  }

  selectNode() {
    this.dom.classList.add('ProseMirror-selectednode')
    const menu = document.createElement('span')
    menu.classList.add('contentblock-menu')
    render(
      <EditButton onClick={this.editVideo} titleKey="insert.video.edit" />,
      menu,
    )
    this.dom.appendChild(menu)
  }
}

export default VideoView
