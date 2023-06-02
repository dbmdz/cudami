import { Node, mergeAttributes } from '@tiptap/core';

// same as HardBreak extension: just name and class name changed to match JSON "hard_break" instead "hardBreak"
// and removed sourcemap comment at bottom
const CustomHardBreak = Node.create({
  name: 'hard_break',
  addOptions() {
    return {
      keepMarks: true,
      HTMLAttributes: {},
    };
  },
  inline: true,
  group: 'inline',
  selectable: false,
  parseHTML() {
    return [
      { tag: 'br' },
    ];
  },
  renderHTML({ HTMLAttributes }) {
    return ['br', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes)];
  },
  renderText() {
    return '\n';
  },
  addCommands() {
    return {
      setHardBreak: () => ({ commands, chain, state, editor, }) => {
        return commands.first([
          () => commands.exitCode(),
          () => commands.command(() => {
            const { selection, storedMarks } = state;
            if (selection.$from.parent.type.spec.isolating) {
              return false;
            }
            const { keepMarks } = this.options;
            const { splittableMarks } = editor.extensionManager;
            const marks = storedMarks
              || (selection.$to.parentOffset && selection.$from.marks());
            return chain()
              .insertContent({ type: this.name })
              .command(({ tr, dispatch }) => {
                if (dispatch && marks && keepMarks) {
                  const filteredMarks = marks
                    .filter(mark => splittableMarks.includes(mark.type.name));
                  tr.ensureMarks(filteredMarks);
                }
                return true;
              })
              .run();
          }),
        ]);
      },
    };
  },
  addKeyboardShortcuts() {
    return {
      'Mod-Enter': () => this.editor.commands.setHardBreak(),
      'Shift-Enter': () => this.editor.commands.setHardBreak(),
    };
  },
});

export { CustomHardBreak, CustomHardBreak as default };