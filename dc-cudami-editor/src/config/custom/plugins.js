import {Plugin} from 'prosemirror-state'

import IframeView from './IframeView'
import ImageView from './ImageView'
import LinkEditingToolbar from './LinkEditingToolbar'

export const linkEditing = () => {
  return new Plugin({
    view(editorView) {
      return new LinkEditingToolbar(editorView)
    },
  })
}

export const nodeViews = () => {
  return new Plugin({
    props: {
      nodeViews: {
        iframe: (node, view, getPos) => {
          return new IframeView(node, view, getPos)
        },
        image: (node, view, getPos) => {
          return new ImageView(node, view, getPos)
        },
      },
    },
  })
}
