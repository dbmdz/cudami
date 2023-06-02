import { Editor } from '@tiptap/core'
import { Blockquote } from '@tiptap/extension-blockquote';
// import { Bold } from '@tiptap/extension-bold';
// import { BulletList } from '@tiptap/extension-bullet-list';
import { Code } from '@tiptap/extension-code';
import { CodeBlock } from '@tiptap/extension-code-block';
import { Document } from '@tiptap/extension-document';
import { Dropcursor } from '@tiptap/extension-dropcursor';
import { Gapcursor } from '@tiptap/extension-gapcursor';
// import { HardBreak } from '@tiptap/extension-hard-break';
import { Heading } from '@tiptap/extension-heading';
import { History } from '@tiptap/extension-history';
import { HorizontalRule } from '@tiptap/extension-horizontal-rule';
// import { Italic } from '@tiptap/extension-italic';
import { Link } from '@tiptap/extension-link';
// import { ListItem } from '@tiptap/extension-list-item';
// import { OrderedList } from '@tiptap/extension-ordered-list';
import { Paragraph } from '@tiptap/extension-paragraph';
// import { Strike } from '@tiptap/extension-strike';
import { Subscript } from '@tiptap/extension-subscript';
import { Superscript } from '@tiptap/extension-superscript';
import { Text } from '@tiptap/extension-text';
import { Underline } from '@tiptap/extension-underline';

import { CustomBold } from '@cudami/tiptap/extension-custom-bold';
import { CustomBulletList } from '@cudami/tiptap/extension-custom-bulletlist';
import { CustomHardBreak } from '@cudami/tiptap/extension-custom-hardbreak';
import { CustomItalic } from '@cudami/tiptap/extension-custom-italic';
import { CustomOrderedList } from '@cudami/tiptap/extension-custom-orderedlist';
import { CustomListItem } from '@cudami/tiptap/extension-custom-listitem';
import { CustomStrike } from '@cudami/tiptap/extension-custom-strike';

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
        Blockquote,
        // Bold,
        // BulletList,
        Code,
        CodeBlock,
        CustomBold,
        CustomBulletList,
        CustomHardBreak,
        CustomItalic,
        CustomLink.configure({
          openOnClick: false
        }),
        CustomListItem,
        CustomOrderedList,
        CustomStrike,
        Document,
        Dropcursor,
        Gapcursor,
        // HardBreak,
        Heading,
        History,
        HorizontalRule,
        // Italic,
        // ListItem,
        // OrderedList,
        Paragraph,
        // Strike,
        Subscript,
        Superscript,
        Text,
        Underline
      ],
      autofocus: true,
      editable: true,
      injectCSS: true,
      onCreate({ editor }) {
        try {
          editor.commands.setContent(JSON.parse(document.getElementById(idJsonField).value));
        } catch (e) {
          // thrown if field is empty
        }
      },
      onFocus({ editor, event }) {
        // The editor is focused.
        editor.updateMenuButtons(editor);
      },
      onSelectionUpdate({ editor }) {
        // The selection has changed.
        editor.updateMenuButtons(editor);
      },
      onUpdate({ editor }) {
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