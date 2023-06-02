import { Node, mergeAttributes } from '@tiptap/core';

// same as ListItem extension: just name and class name changed to match JSON "list_item" instead "listItem"
// and removed sourcemap comment at bottom
const CustomListItem = Node.create({
  name: 'list_item',
  addOptions() {
    return {
      HTMLAttributes: {},
    };
  },
  content: 'paragraph block*',
  defining: true,
  parseHTML() {
    return [
      {
        tag: 'li',
      },
    ];
  },
  renderHTML({ HTMLAttributes }) {
    return ['li', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
  },
  addKeyboardShortcuts() {
    return {
      Enter: () => this.editor.commands.splitListItem(this.name),
      Tab: () => this.editor.commands.sinkListItem(this.name),
      'Shift-Tab': () => this.editor.commands.liftListItem(this.name),
    };
  },
});

export { CustomListItem, CustomListItem as default };