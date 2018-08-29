(function(prosemirrorState, prosemirrorView, prosemirrorModel, prosemirrorSchemaBasic, prosemirrorSchemaList, prosemirrorExampleSetup, proseMirrorMenu){
  'use strict';

  /* Variables */

  var iframeNodeSpec = {
    attrs: {
      height: {},
      src: {},
      width: {}
    },
    draggable: true,
    group: 'block',
    inline: false,
    parseDOM: [{
      tag: 'iframe[src]',
      getAttrs: function(element){
        return {
          'height': element.getAttribute('height'),
          'src': element.getAttribute('src'),
          'width': element.getAttribute('width')
        };
      }
    }],
    toDOM: function(node){
      return ['iframe', {
        'class': 'editable',
        'height': node.attrs.height,
        'sandbox': '',
        'src': node.attrs.src,
        'width': node.attrs.width
      }];
    }
  };

  // Mix the nodes from prosemirror-schema-list into the basic schema to
  // create a schema with list support.
  var schema = new prosemirrorModel.Schema({
    nodes: prosemirrorSchemaList.addListNodes(
      prosemirrorSchemaBasic.schema.spec.nodes, 'paragraph block*', 'block'
    ).addBefore('image', 'iframe', iframeNodeSpec),
    marks: prosemirrorSchemaBasic.schema.spec.marks
  });

  var iframeDialogTemplate = [
    '<div id="iframe-dialog" class="modal fade" tabindex="-1" role="dialog">',
    '<div class="modal-dialog" role="document">',
    '<div class="modal-content">',
    '<div class="modal-header">',
    '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>',
    '<h4 class="modal-title">Insert iframe</h4>',
    '</div>',
    '<div class="modal-body">',
    '<form>',
    '<div class="form-group">',
    '<input class="form-control" id="iframe-src" placeholder="src" required type="text">',
    '</div>',
    '<div class="form-group">',
    '<input class="form-control" id="iframe-width" placeholder="width" type="text">',
    '</div>',
    '<div class="form-group">',
    '<input class="form-control" id="iframe-height" placeholder="height" type="text">',
    '</div>',
    '<button class="btn btn-primary" id="save" type="submit">Save</button>',
    '</form>',
    '</div>',
    '</div>',
    '</div>',
    '</div>'
  ].join('');

  var menu = prosemirrorExampleSetup.buildMenuItems(schema);
  menu.insertMenu.content.push(new proseMirrorMenu.MenuItem({
    title: 'Insert iframe',
    label: 'Iframe',
    enable: function(state){ return canInsertIframe(state, schema.nodes.iframe); },
    run: function(state, _, view){ openIframeDialog(state, view, schema.nodes.iframe); }
  }));

  var activeLocale;

  var defaultLocale = $('form#editor-form').data('default-locale');

  var invalidLocales = [];

  var editorViews = {};

  var initialEditorJsonString = '{"type":"doc","content":[{"type":"paragraph"}]}';

  /* Functions */

  function canInsertIframe(state, nodeType){
    var $from = state.selection.$from;
    for(var d = $from.depth; d >= 0; d--){
      var index = $from.index(d);
      if($from.node(d).canReplaceWith(index, index, nodeType)){
        return true;
      }
    }
    return false;
  }

  function cleanupJson(){
    $('.content').each(function(_, contentElement){
      var contentJson = JSON.parse($(this).val());
      Object.keys(contentJson.localizedStructuredContent).forEach(function(currentLocale){
        var contentJsonString = JSON.stringify(contentJson.localizedStructuredContent[currentLocale]);
        if(contentJsonString === initialEditorJsonString){
          delete contentJson.localizedStructuredContent[currentLocale];
        }
      });
      if($.isEmptyObject(contentJson.localizedStructuredContent)){
        $(contentElement).val('');
      }else{
        $(contentElement).val(JSON.stringify(contentJson));
      }
    });
  }

  function constructLocaleSwitcher(localeTag, localeName){
    return [
      '<li class="locale-switcher active unsaved">',
      '<a href="#" data-locale="' + localeTag + '">' + localeName + '</a>',
      '</li>'
    ].join('\n');
  }

  function getActiveLocale(){
    return activeLocale;
  }

  function initializeEventBindings(){
    $('#locale-tabs > #locale-adder > a').on('click', function(){
      $('#add-language-dialog').modal('show');
    });

    $('#add-language-dialog #confirm-language-selection').on('click', function(){
      $('#locale-tabs > .locale-switcher').removeClass('active');

      var selectedLocaleTag = $("#add-language-dialog #locale-selector").val();
      var selectedLocaleName = $("#add-language-dialog #locale-selector :selected").text();
      $('#locale-tabs > #locale-adder').before(constructLocaleSwitcher(selectedLocaleTag, selectedLocaleName));

      updateForm(selectedLocaleTag);

      $('#add-language-dialog').modal('hide');

      $("#add-language-dialog #locale-selector").children('option[value="' + selectedLocaleTag + '"]').remove();
    });

    $('#locale-tabs').on('click', '.locale-switcher', function(){
      $('#locale-tabs > .locale-switcher').removeClass('active');
      $(this).addClass('active');

      var selectedLocaleTag = $(this).find('a').data('locale');

      updateForm(selectedLocaleTag);
    });

    $('#editor-form').submit(function(evt){
      evt.preventDefault();
      if(validateForm()){
        cleanupJson();
        $(this)[0].submit();
      }
    });

    $('#continue-editing').on('click', function(){
      $(this).closest('#form-validation-dialog').modal('hide');
    });
  }

  function initializeForm(){
    initializeLabelInForm();
    initializeEditorsInForm();
  }

  function initializeEditorsInForm(){
    $('.content').each(function(_, contentElement){
      if($(this).val() === ''){
        $(this).val('{"localizedStructuredContent":{"' + defaultLocale + '":' + initialEditorJsonString + '}}');
      }
      var contentJson = JSON.parse($(this).val());
      var currentEditor = document.querySelector(
        '.editor[data-content-id=' + contentElement.id + ']'
      );
      var editorView = new prosemirrorView.EditorView(currentEditor, {
        state: prosemirrorState.EditorState.create({
          // from HTML:
          // doc: prosemirrorModel.DOMParser.fromSchema(mySchema).parse(document.querySelector('#content')),

          // from JSON:
          doc: prosemirrorModel.Node.fromJSON(
            schema,
            contentJson.localizedStructuredContent[activeLocale] || JSON.parse(initialEditorJsonString)
          ),
          plugins: prosemirrorExampleSetup.exampleSetup({schema: schema, menuContent: menu.fullMenu})
        }),
        dispatchTransaction: function(tr){
          $('#locale-tabs a[data-locale=' + getActiveLocale() + ']').parent().removeClass('unsaved');
          var currentEditorView = editorViews[this.contentElement.id];
          currentEditorView.updateState(currentEditorView.state.apply(tr));
          // current state as json in text area
          this.contentJson.localizedStructuredContent[getActiveLocale()] = currentEditorView.state.doc.toJSON();
          $(this.contentElement).val(JSON.stringify(this.contentJson));
        }.bind({
          'contentElement': contentElement, 'contentJson': contentJson, 'getActiveLocale': getActiveLocale
        })
      });
      editorViews[contentElement.id] = editorView;
    });
  }

  function initializeLabelInForm(locale){
    var labelContent = $('#label');
    if($(labelContent).val() === ''){
      $(labelContent).val('{"translations":[{"locale":"' + defaultLocale + '","text":""}]}');
    }
    var labelJson = JSON.parse($(labelContent).val());
    var activeTranslation = labelJson.translations[0];
    if(locale){
      activeTranslation = $.grep(labelJson.translations, function(translation){
        return translation.locale === locale;
      })[0];
      if(!activeTranslation){
        activeTranslation = {
          'locale': locale,
          'text': ''
        };
        labelJson.translations.push(activeTranslation);
      }
    }
    activeLocale = activeTranslation.locale;

    $('.label-editor').val(activeTranslation.text).off('input').on('input', function(){
      $('#locale-tabs a[data-locale=' + activeTranslation.locale + ']').parent().removeClass('unsaved');
      $.map(labelJson.translations, function(translation){
        if(translation.locale === activeTranslation.locale){
          translation.text = $('.label-editor').val();
        }
      });
      $('#label').val(JSON.stringify(labelJson));
    });
  };

  function isLabelEmpty(locale){
    var labelJson = JSON.parse($('#label').val());
    var labelInLocale = $.grep(labelJson.translations, function(translation){
        return translation.locale === locale;
    });
    if(labelInLocale.length === 0 || labelInLocale[0].text.trim() === ''){
      return true;
    }
    return false;
  }

  function openIframeDialog(state, view, nodeType){
    $(document.body).append(iframeDialogTemplate);
    if(state.selection instanceof prosemirrorState.NodeSelection && state.selection.node.type === nodeType){
      var attrs = state.selection.node.attrs;
      $('#iframe-dialog #iframe-height').val(attrs.height);
      $('#iframe-dialog #iframe-src').val(attrs.src);
      $('#iframe-dialog #iframe-width').val(attrs.width);
    }
    $('#iframe-dialog form').submit(function(evt){
      evt.preventDefault();
      view.dispatch(view.state.tr.replaceSelectionWith(nodeType.createAndFill({
        'height': $(this).find('#iframe-height').val(),
        'src': $(this).find('#iframe-src').val(),
        'width': $(this).find('#iframe-width').val()
      })));
      view.focus();
      $(this).closest('#iframe-dialog').modal('hide');
    });
    $('#iframe-dialog').on('hidden.bs.modal', function(){
      $(this).remove();
    });
    $('#iframe-dialog').modal('show');
  };

  function updateForm(locale){
    initializeLabelInForm(locale);
    updateEditorsInForm();
  }

  function updateEditorsInForm(){
    var contents = $('.content');
    var newState;
    contents.each(function(_, contentElement){
      var contentJson = JSON.parse($(this).val());
      newState = prosemirrorState.EditorState.create({
        doc: prosemirrorModel.Node.fromJSON(
          schema,
          contentJson.localizedStructuredContent[activeLocale] || JSON.parse(initialEditorJsonString)
        ),
        plugins: prosemirrorExampleSetup.exampleSetup({schema: schema, menuContent: menu.fullMenu})
      });
      editorViews[contentElement.id].updateState(newState);
    });
  }

  function validateForm(){
    invalidLocales = [];
    $('#form-validation-dialog #invalid-locales').empty();
    $('ul#locale-tabs > li.locale-switcher:not(.unsaved)').removeClass('invalid').find('a').each(function(){
      if(isLabelEmpty($(this).data('locale'))){
        $(this).parent().addClass('invalid');
        invalidLocales.push($(this).text());
      }
    });
    if(invalidLocales.length > 0){
      invalidLocales.forEach(function(invalidLocale){
        $('#form-validation-dialog #invalid-locales').append(
          '<li>' + invalidLocale + '</li>'
        );
      });
      $('#form-validation-dialog').modal('show');
      return false;
    }
    return true;
  }

  $(document).ready(function(){
    initializeEventBindings();
    initializeForm();
  });
}(PM.state, PM.view, PM.model, PM.schema_basic, PM.schema_list, PM.example_setup, PM.menu));