import { Mark, mergeAttributes, markInputRule, markPasteRule } from '@tiptap/core';

const starInputRegex = /(?:^|\s)((?:\*)((?:[^*]+))(?:\*))$/;
const starPasteRegex = /(?:^|\s)((?:\*)((?:[^*]+))(?:\*))/g;
const underscoreInputRegex = /(?:^|\s)((?:_)((?:[^_]+))(?:_))$/;
const underscorePasteRegex = /(?:^|\s)((?:_)((?:[^_]+))(?:_))/g;

// same as Italic extension: just name and class name changed to match JSON "em" instead "italic"
// and removed sourcemap comment at bottom
const CustomItalic = Mark.create({
    name: 'em',
    addOptions() {
        return {
            HTMLAttributes: {},
        };
    },
    parseHTML() {
        return [
            {
                tag: 'em',
            },
            {
                tag: 'i',
                getAttrs: node => node.style.fontStyle !== 'normal' && null,
            },
            {
                style: 'font-style=italic',
            },
        ];
    },
    renderHTML({ HTMLAttributes }) {
        return ['em', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
    },
    addCommands() {
        return {
            setItalic: () => ({ commands }) => {
                return commands.setMark(this.name);
            },
            toggleItalic: () => ({ commands }) => {
                return commands.toggleMark(this.name);
            },
            unsetItalic: () => ({ commands }) => {
                return commands.unsetMark(this.name);
            },
        };
    },
    addKeyboardShortcuts() {
        return {
            'Mod-i': () => this.editor.commands.toggleItalic(),
            'Mod-I': () => this.editor.commands.toggleItalic(),
        };
    },
    addInputRules() {
        return [
            markInputRule({
                find: starInputRegex,
                type: this.type,
            }),
            markInputRule({
                find: underscoreInputRegex,
                type: this.type,
            }),
        ];
    },
    addPasteRules() {
        return [
            markPasteRule({
                find: starPasteRegex,
                type: this.type,
            }),
            markPasteRule({
                find: underscorePasteRegex,
                type: this.type,
            }),
        ];
    },
});

export { CustomItalic, CustomItalic as default, starInputRegex, starPasteRegex, underscoreInputRegex, underscorePasteRegex };
