import { Editor } from '@tiptap/core'
import {Schema} from 'prosemirror-model'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import marks from './marks.js'
import nodes from './nodes.js'

        window.editor = new Editor({
          element: document.querySelector('#editor'),
          extensions: [
            StarterKit,
            Underline
          ],
//          onBeforeCreate({editor}) {
//            // see https://github.com/ueberdosis/tiptap/discussions/2077
//            editor.schema = new Schema({nodes, marks})
//          },
//          content: '<p>Example Text</p>',
          autofocus: true,
          editable: true,
          injectCSS: true,
//          schema: new Schema({nodes, marks})
        });

editor.schema.nodes.bulletList.name = 'bullet_list';
editor.schema.nodes.listItem.name = 'list_item';
editor.schema.nodes.orderedList.name = 'ordered_list';

//        const schema = editor.schema;
//editor.schema = new Schema({nodes, marks});

editor.on('focus', ({ editor }) => {
  updateMenuButtons();
});
editor.on('selectionUpdate', ({ editor }) => {
  updateMenuButtons();
});
editor.on('update', ({ editor }) => {
  updateMenuButtons();
  var json = JSON.stringify(editor.getJSON());
  document.getElementById("contentJSON").value = json;
});

function updateMenuButtons() {
  let editorMenu = document.getElementById('editorMenu');
  var matches = editorMenu.querySelectorAll("*[data-value]");
  for (let i = 0; i < matches.length; i++) {
    var menuItem = matches[i];
    var value = menuItem.getAttribute("data-value");
    if (value === "undo") {
      if (editor.can().undo()) {
        menuItem.removeAttribute('disabled');
      } else {
        menuItem.setAttribute('disabled', true);
      }
    } else if (value === "redo") {
      if (editor.can().redo()) {
        menuItem.removeAttribute('disabled');
      } else {
        menuItem.setAttribute('disabled', true);
      }
    } else {
      if (editor.isActive(value)) {
        menuItem.classList.add("is-active");
      } else {
        menuItem.classList.remove("is-active");
      }
    }
  }
}
