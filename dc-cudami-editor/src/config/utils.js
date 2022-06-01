export const getAttributes = (attrs, dom) =>
  Object.fromEntries(attrs.map((attr) => [attr, dom.getAttribute(attr)]))

export const markActive = (type) => (state) => {
  const {from, $from, to, empty} = state.selection

  return empty
    ? type.isInSet(state.storedMarks || $from.marks())
    : state.doc.rangeHasMark(from, to, type)
}
