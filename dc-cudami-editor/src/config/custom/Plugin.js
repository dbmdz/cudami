import {Plugin} from 'prosemirror-state'

import IframeView from './IframeView'
import ImageView from './ImageView'

export default () => {
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
