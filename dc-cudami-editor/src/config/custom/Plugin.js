import {Plugin} from 'prosemirror-state'

import IframeView from './IframeView'

export default () => {
  return new Plugin({
    props: {
      nodeViews: {
        iframe: (node, view, getPos) => {
          return new IframeView(node, view, getPos)
        },
      },
    },
  })
}
