package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.Locale;
import java.util.UUID;

public class RenderingTemplateBuilder {

  private RenderingTemplate renderingTemplate = new RenderingTemplate();

  public RenderingTemplate build() {
    return renderingTemplate;
  }

  public RenderingTemplateBuilder withName(String name) {
    renderingTemplate.setName(name);
    return this;
  }

  public RenderingTemplateBuilder withUuid(String uuid) {
    renderingTemplate.setUuid(UUID.fromString(uuid));
    return this;
  }

  public RenderingTemplateBuilder withDescription(Locale locale, String text) {
    LocalizedText description = renderingTemplate.getDescription();
    if (description == null) {
      description = new LocalizedText();
    }
    description.setText(locale, text);
    renderingTemplate.setDescription(description);
    return this;
  }

  public RenderingTemplateBuilder withLabel(Locale locale, String localizedLabel) {
    LocalizedText label = renderingTemplate.getLabel();
    if (label == null) {
      label = new LocalizedText();
    }
    label.setText(locale, localizedLabel);
    renderingTemplate.setLabel(label);
    return this;
  }
}
