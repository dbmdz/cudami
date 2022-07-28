import {publish, subscribe, unsubscribe} from 'pubsub-js'
import {render} from 'react-dom'

import EditButton from '../../components/editor/EditButton'
import {markActive} from '../utils'

class LinkEditingToolbar {
  constructor(editorView) {
    this.view = editorView
    const toolbar = document.createElement('div')
    toolbar.classList.add('d-none', 'link-editing-toolbar', 'position-absolute')
    render(
      <EditButton onClick={this.editLink} titleKey="marks.link.edit" />,
      toolbar,
    )
    this.toolbar = toolbar
  }

  destroy() {
    this.toolbar.remove()
  }

  editLink = () => {
    const token = subscribe('editor.add-link', (_msg, data) => {
      const {link, selection, view} = this
      view.dispatch(
        view.state.tr.removeMark(selection.from, selection.to, link),
      )
      link.attrs = data
      view.dispatch(view.state.tr.addMark(selection.from, selection.to, link))
      unsubscribe(token)
    })
    publish('editor.show-link-dialog', {
      attributes: this.link.attrs,
      editing: true,
    })
  }

  update(view, lastState) {
    const {dom, state} = view

    // Don't do anything if the document/selection didn't change
    if (
      lastState &&
      lastState.doc.eq(state.doc) &&
      lastState.selection.eq(state.selection)
    ) {
      return
    }

    // Hide the toolbar if the selection is empty or it's not a link
    const linkActive = markActive(state.config.schema.marks.link)(state)
    if (state.selection.empty || !linkActive) {
      this.toolbar.classList.add('d-none')
      return
    }

    // Extract the link mark and set them as class attribute
    const {from, to} = state.selection
    const position = state.doc.resolve(from)
    const start = position.parent.childAfter(position.parentOffset)
    const link = start.node?.marks?.find((mark) => mark.type.name === 'link')
    if (!link) {
      return
    }
    this.link = link

    // Also set the current selection as class attribute, it is later needed to update the mark
    this.selection = state.selection

    // Add the toolbar to the dom if it was not alredy appended
    const toolbar = dom.parentNode.querySelector('.link-editing-toolbar')
    if (!toolbar) {
      dom.parentNode.appendChild(this.toolbar)
    }

    // Show the toolbar if we have a link
    this.toolbar.classList.remove('d-none')

    // These are in screen coordinates
    const startCoordinates = view.coordsAtPos(from)
    const endCoordinates = view.coordsAtPos(to)

    // The box in which the toolbar is positioned, to use as base
    const box = this.toolbar.offsetParent.getBoundingClientRect()

    // Update the toolbar's position
    this.toolbar.style.left = `${endCoordinates.left - box.left}px`
    this.toolbar.style.bottom = `${box.bottom - startCoordinates.top}px`
  }
}

export default LinkEditingToolbar
