import { Editor } from '@tiptap/core'
import Link from '@tiptap/extension-link';
import StarterKit from '@tiptap/starter-kit'
import Subscript from '@tiptap/extension-subscript'
import Superscript from '@tiptap/extension-superscript'
import Underline from '@tiptap/extension-underline'

        const CustomLink = Link.extend({
          addAttributes() {
            return {
              ...this.parent?.(),
              title: {
                default: null
              }
            };
          }
        });

        class MiniEditor extends Editor {
          constructor(idEditorElement, idEditorMenu, idJsonField) {
            super({
              element: document.getElementById(idEditorElement),
              extensions: [
                CustomLink.configure({
                  openOnClick: false
                }),
                StarterKit,
                Subscript,
                Superscript,
                Underline
              ],
              autofocus: true,
              editable: true,
              injectCSS: true,
              onCreate( { editor }) {
                // The editor is ready. Now teak the schema to target semantics:
                editor.schema.nodes.bulletList.name = 'bullet_list';
                editor.schema.nodes.listItem.name = 'list_item';
                editor.schema.nodes.orderedList.name = 'ordered_list';
                
                editor.schema.marks.bold.name = 'strong';
                editor.schema.marks.italic.name = 'em';
                editor.schema.marks.strike.name = 'strikethrough';

                try {
                  editor.commands.setContent(JSON.parse(document.getElementById(idJsonField).value));
                } catch (e) {
                  // thrown if field is empty
                }
              },
              onFocus( { editor, event }) {
                // The editor is focused.
                editor.updateMenuButtons(editor);
              },
              onSelectionUpdate( { editor }) {
                // The selection has changed.
                editor.updateMenuButtons(editor);
              },
              onUpdate( { editor }) {
                // The content has changed.
                editor.updateMenuButtons(editor);
                var json = JSON.stringify(editor.getJSON());
                document.getElementById(idJsonField).value = json;
              }
            });
            this.idEditorMenu = idEditorMenu;
          }

          updateMenuButtons(editor) {
            let editorMenu = document.getElementById(this.idEditorMenu);
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
        }

        export { CustomLink };
        export { MiniEditor };