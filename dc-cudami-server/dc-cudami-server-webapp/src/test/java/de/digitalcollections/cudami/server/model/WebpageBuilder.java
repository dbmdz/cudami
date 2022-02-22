package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.view.RenderingHints;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.LocaleUtils;
import org.flywaydb.core.internal.util.StringUtils;

public class WebpageBuilder {

  Webpage webpage = new Webpage();

  public Webpage build() {
    return webpage;
  }

  public WebpageBuilder createdAt(String created) {
    webpage.setCreated(LocalDateTime.parse(created));
    return this;
  }

  public WebpageBuilder lastModifiedAt(String lastModified) {
    webpage.setLastModified(LocalDateTime.parse(lastModified));
    return this;
  }

  public WebpageBuilder setIdentifiers(Set<Identifier> identifiers) {
    webpage.setIdentifiers(identifiers);
    return this;
  }

  public WebpageBuilder withLabel(Locale locale, String localizedLabel) {
    LocalizedText label = webpage.getLabel();
    if (label == null) {
      label = new LocalizedText();
    }
    label.setText(locale, localizedLabel);
    webpage.setLabel(label);
    return this;
  }

  public WebpageBuilder withUuid(String uuidStr) {
    webpage.setUuid(UUID.fromString(uuidStr));
    return this;
  }

  public WebpageBuilder withPublicationStartAt(String publicationStart) {
    webpage.setPublicationStart(LocalDate.parse(publicationStart));
    return this;
  }

  public WebpageBuilder withDescription(String locale, String text) {
    LocalizedStructuredContent description = webpage.getDescription();
    if (description == null) {
      description = new LocalizedStructuredContent();
    }
    StructuredContent localizedDescription = description.get(locale);
    if (localizedDescription == null) {
      localizedDescription = new StructuredContent();
    }
    ContentBlock paragraph = StringUtils.hasText(text) ? new Paragraph(text) : new Paragraph();
    localizedDescription.addContentBlock(paragraph);
    description.put(LocaleUtils.toLocale(locale), localizedDescription);
    webpage.setDescription(description);
    return this;
  }

  public WebpageBuilder shownInNavigation() {
    RenderingHints renderingHints = new RenderingHints();
    renderingHints.setShowInPageNavigation(true);
    webpage.setRenderingHints(renderingHints);
    return this;
  }

  public WebpageBuilder notShownInNavigation() {
    RenderingHints renderingHints = new RenderingHints();
    renderingHints.setShowInPageNavigation(false);
    webpage.setRenderingHints(renderingHints);
    return this;
  }

  public WebpageBuilder withPreviewImage(
      String fileName, String uuid, String uri, MimeType mimeType, String httpBaseUrl) {
    ImageFileResource previewImage =
        new PreviewImageBuilder(uuid)
            .withFileName(fileName)
            .withUri(uri)
            .withMimeType(mimeType)
            .withHttpBaseUrl(httpBaseUrl)
            .build();
    webpage.setPreviewImage(previewImage);
    return this;
  }

  public WebpageBuilder withAltText(Locale locale, String text) {
    RenderingHintsPreviewImage previewImageRenderingHints = webpage.getPreviewImageRenderingHints();
    if (previewImageRenderingHints == null) {
      previewImageRenderingHints = new RenderingHintsPreviewImage();
    }
    previewImageRenderingHints.setAltText(new LocalizedText(locale, text));
    webpage.setPreviewImageRenderingHints(previewImageRenderingHints);
    return this;
  }

  public WebpageBuilder withOpenLinkInNewWindow() {
    RenderingHintsPreviewImage previewImageRenderingHints = webpage.getPreviewImageRenderingHints();
    if (previewImageRenderingHints == null) {
      previewImageRenderingHints = new RenderingHintsPreviewImage();
    }
    previewImageRenderingHints.setOpenLinkInNewWindow(true);
    webpage.setPreviewImageRenderingHints(previewImageRenderingHints);
    return this;
  }

  public WebpageBuilder withTemplateName(String templateName) {
    RenderingHints renderingHints = webpage.getRenderingHints();
    if (renderingHints == null) {
      renderingHints = new RenderingHints();
    }
    renderingHints.setTemplateName(templateName);
    webpage.setRenderingHints(renderingHints);
    return this;
  }

  public WebpageBuilder withChildren(List<Webpage> children) {
    webpage.setChildren(children);
    return this;
  }
}
