function previewImageDialog(language, modalTitle) {
  // init modal dialog
  const previewImageJson = $("input#previewImage-json").val();
  if (previewImageJson) {
    const image = JSON.parse(previewImageJson);
    $("#selectImageDialog-fileresource").hide();
  }
  const previewImageRenderingHintsJson = $("input#previewImageRenderingHints-json").val();
  if (previewImageRenderingHintsJson) {
    const hints = JSON.parse(previewImageRenderingHintsJson);
    $("#selectImageDialog input[name='hints-caption']").val(hints.caption?.[language]);
    $("#selectImageDialog input[name='hints-title']").val(hints.title?.[language]);
    $("#selectImageDialog input[name='hints-alttext']").val(hints.altText?.[language]);
    $("#selectImageDialog input[name='hints-targetLink']").val(hints.targetLink);
    $("#selectImageDialog input[name='hints-openLinkInNewWindow'][value='" + hints.openLinkInNewWindow + "']").prop("checked",true);
  } else {
    $("#selectImageDialog input[name='hints-caption']").val("");
    $("#selectImageDialog input[name='hints-title']").val("");
    $("#selectImageDialog input[name='hints-alttext']").val("");
    $("#selectImageDialog input[name='hints-targetLink']").val("");
    $("#selectImageDialog input[name='hints-openLinkInNewWindow'][value='true']").prop("checked",true);
  }
  
  const modalDialog = $('#selectImageDialog');
  $(modalDialog).find('.modal-title').text(modalTitle);
  const btnOk = $(modalDialog).find('.modal-footer .btn-primary');
  $(btnOk).attr('onclick', "setPreviewImage('" + language + "');");
  $(modalDialog).modal('show');
}

function removePreviewImage() {
  $("input#previewImage-json").val("");
  $("input#previewImageRenderingHints-json").val("");
  renderPreviewImage();
}

function renderPreviewImage(fieldLanguage) {
  const previewImageJson = $("input#previewImage-json").val();
  const previewImageSections = $('.preview-image-section');
  if (previewImageJson) {
    const previewImageSection = $('#preview-image-' + fieldLanguage);
    const image = JSON.parse(previewImageJson);
    const width = 200;
    const url = image ? getImageUrl(image, width + ',') : '/images/no-image.png';

    const previewImageRenderingHintsJson = $("input#previewImageRenderingHints-json").val();
    let altText, caption, title = "";
    if (previewImageRenderingHintsJson) {
      const hints = JSON.parse(previewImageRenderingHintsJson);
      altText = image ? hints.altText?.[fieldLanguage] ?? image.filename : 'noPreviewImage';
      caption = hints.caption?.[fieldLanguage];
      title = hints.title?.[fieldLanguage];
    }
    altText = altText ? altText : "";
    caption = caption ? caption : "";
    title = title ? title : "";

    let previewImageHtml =
      `<figure class='mb-0 mx-auto' style='maxWidth: ${width}px;'>
                           <img alt='${altText}' class='img-fluid mw-100' src='${url}' title='${title}' />
                           <figcaption class='figure-caption'>${caption}</figcaption>
                         </figure>`;

    $(previewImageSection).find(".card-body .image").html(previewImageHtml);
    $(previewImageSections).find(".card-body .actions .add").hide();
    $(previewImageSections).find(".card-body .actions .edit").show();
  } else {
    $(previewImageSections).find(".card-body .image").html("");
    $(previewImageSections).find(".card-body .actions .add").show();
    $(previewImageSections).find(".card-body .actions .edit").hide();
  }
}

function setPreviewImage(fieldLanguage) {
  const modalDialog = $('#selectImageDialog');
  $(modalDialog).modal('hide');

  // set previewImageRenderingHints-json
  const formElement = $(modalDialog).find('#image-dialog-form').get(0);
  const formData = new FormData(formElement);
  let formJson = formDataToJson(formData);

  let hints;
  let previewImageRenderingHintsJson = $("input#previewImageRenderingHints-json").val();
  if (previewImageRenderingHintsJson) {
    hints = JSON.parse(previewImageRenderingHintsJson);
  } else {
    hints = {};
  }
  if (!hints.caption) {
    hints.caption = {};
  }
  if (!hints.altText) {
    hints.altText = {};
  }
  if (!hints.title) {
    hints.title = {};
  }
  hints.caption[fieldLanguage] = formJson["hints-caption"];
  hints.altText[fieldLanguage] = formJson["hints-alttext"];
  hints.title[fieldLanguage] = formJson["hints-title"];
  hints.targetLink = formJson["hints-targetLink"];
  hints.openLinkInNewWindow = formJson["hints-openLinkInNewWindow"];

  let jsonHints = JSON.stringify(hints);
  $("input#previewImageRenderingHints-json").val(jsonHints);

  // set previewImage-json
  let imageFileResourceJson = $("input#imageFileResource-json").val();
  if (imageFileResourceJson) {
    let imageFileResource = JSON.parse(imageFileResourceJson);
    // add data from modal input to File Resource
    imageFileResource.filename = formJson["fr-filename"][0];
    $("input#previewImage-json").val(JSON.stringify(imageFileResource));
  }

  // render preview image
  renderPreviewImage(fieldLanguage);
}
