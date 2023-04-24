import { Mark, mergeAttributes, markInputRule, markPasteRule } from '@tiptap/core';

const starInputRegex = /(?:^|\s)((?:\*\*)((?:[^*]+))(?:\*\*))$/;
const starPasteRegex = /(?:^|\s)((?:\*\*)((?:[^*]+))(?:\*\*))/g;
const underscoreInputRegex = /(?:^|\s)((?:__)((?:[^__]+))(?:__))$/;
const underscorePasteRegex = /(?:^|\s)((?:__)((?:[^__]+))(?:__))/g;

// same as Bold extension: just name and class name changed to match JSON "strong" instead "bold"
// and removed sourcemap comment at bottom
const CustomBold = Mark.create({
    name: 'strong',
    addOptions() {
        return {
            HTMLAttributes: {},
        };
    },
    parseHTML() {
        return [
            {
                tag: 'strong',
            },
            {
                tag: 'b',
                getAttrs: node => node.style.fontWeight !== 'normal' && null,
            },
            {
                style: 'font-weight',
                getAttrs: value => /^(bold(er)?|[5-9]\d{2,})$/.test(value) && null,
            },
        ];
    },
    renderHTML({ HTMLAttributes }) {
        return ['strong', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
    },
    addCommands() {
        return {
            setBold: () => ({ commands }) => {
                return commands.setMark(this.name);
            },
            toggleBold: () => ({ commands }) => {
                return commands.toggleMark(this.name);
            },
            unsetBold: () => ({ commands }) => {
                return commands.unsetMark(this.name);
            },
        };
    },
    addKeyboardShortcuts() {
        return {
            'Mod-b': () => this.editor.commands.toggleBold(),
            'Mod-B': () => this.editor.commands.toggleBold(),
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

export { CustomBold, CustomBold as default, starInputRegex, starPasteRegex, underscoreInputRegex, underscorePasteRegex };

