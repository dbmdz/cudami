import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
//import Underline from '@tiptap/extension-underline'

        window.editor = new Editor({
          element: document.querySelector('#editor'),
          extensions: [
            StarterKit,
//            Underline
          ],
//          content: '<p>Example Text</p>',
          autofocus: true,
          editable: true,
          injectCSS: true
        });
