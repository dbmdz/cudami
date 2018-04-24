(function(prosemirrorState, prosemirrorView, prosemirrorModel, prosemirrorSchemaBasic, prosemirrorSchemaList, prosemirrorExampleSetup){
  'use strict';

  // Mix the nodes from prosemirror-schema-list into the basic schema to
  // create a schema with list support.
  var mySchema = new prosemirrorModel.Schema({
    nodes: prosemirrorSchemaList.addListNodes(
      prosemirrorSchemaBasic.schema.spec.nodes, "paragraph block*", "block"
    ),
    marks: prosemirrorSchemaBasic.schema.spec.marks
  });

  var contents = $(".content");
  var editorViews = {};
  contents.each(function(index, contentElement){
    var contentJson = JSON.parse($(this).val());
    $.each(contentJson.documents, function(language, content){
      var currentEditor = document.querySelector(
        '.editor[data-content-id=' + contentElement.id + ']' +
        '[data-content-language=' + language + ']'
      );
      var editorView = new prosemirrorView.EditorView(currentEditor, {
        state: prosemirrorState.EditorState.create({
          // from HTML:
          // doc: prosemirrorModel.DOMParser.fromSchema(mySchema).parse(document.querySelector("#content")),

          // from JSON:
          doc: prosemirrorModel.Node.fromJSON(mySchema, content),
          plugins: prosemirrorExampleSetup.exampleSetup({schema: mySchema})
        }),
        dispatchTransaction: function(tr){
          var currentEditorView = editorViews[this.contentElement.id + '+' + language];
          currentEditorView.updateState(currentEditorView.state.apply(tr));
          // current state as json in text area
          this.contentJson.documents[this.language] = currentEditorView.state.doc.toJSON();
          $(this.contentElement).val(JSON.stringify(this.contentJson));
        }.bind({
          'contentElement': contentElement, 'contentJson': contentJson, 'language': language
        })
      });
      editorViews[contentElement.id + '+' + language] = editorView;
    });
  });
}(PM.state, PM.view, PM.model, PM.schema_basic, PM.schema_list, PM.example_setup));