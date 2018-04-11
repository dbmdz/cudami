(function (prosemirrorState,prosemirrorView,prosemirrorModel,prosemirrorSchemaBasic,prosemirrorSchemaList,prosemirrorExampleSetup) {
'use strict';

// code{
// Mix the nodes from prosemirror-schema-list into the basic schema to
// create a schema with list support.
var mySchema = new prosemirrorModel.Schema({
  nodes: prosemirrorSchemaList.addListNodes(prosemirrorSchemaBasic.schema.spec.nodes, "paragraph block*", "block"),
  marks: prosemirrorSchemaBasic.schema.spec.marks
});

var initialJson = document.querySelector("#content").value;
if (initialJson === null || initialJson === "") {
  initialJson = '{"type":"doc","content":[{"type":"paragraph"}]}';
}
window.view = new prosemirrorView.EditorView(document.querySelector("#editor"), {
  state: prosemirrorState.EditorState.create({
// from HTML:
// doc: prosemirrorModel.DOMParser.fromSchema(mySchema).parse(document.querySelector("#content")),

// from JSON:
    doc: prosemirrorModel.Node.fromJSON(mySchema, JSON.parse(initialJson)),
    plugins: prosemirrorExampleSetup.exampleSetup({schema: mySchema})
  }),
  dispatchTransaction(tr) {
    window.view.updateState(window.view.state.apply(tr));
    //current state as json in text area
    var json = JSON.stringify(window.view.state.doc.toJSON());
    if (json === '{"type":"doc","content":[{"type":"paragraph"}]}') {
      document.querySelector("#content").value = null;
    } else {
      document.querySelector("#content").value = json;
    }
  }
});
// }

}(PM.state,PM.view,PM.model,PM.schema_basic,PM.schema_list,PM.example_setup));