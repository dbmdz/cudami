import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'

        window.editor = new Editor({
          element: document.querySelector('#editor'),
          extensions: [
            StarterKit,
            Underline
          ],
//          content: '<p>Example Text</p>',
          autofocus: true,
          editable: true,
          injectCSS: true
        });

editor.on('focus', ({ editor }) => {
  updateMenuButtons();
});
editor.on('selectionUpdate', ({ editor }) => {
  updateMenuButtons();
});
editor.on('update', ({ editor }) => {
  updateMenuButtons();
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

function syncContent() {
//        var json = JSON.stringify(window.view.state.doc.toJSON());
  var json = JSON.stringify(editor.getJSON());
  document.getElementById("contentJSON").value = json;
}