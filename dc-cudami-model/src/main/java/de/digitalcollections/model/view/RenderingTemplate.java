package de.digitalcollections.model.view;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import lombok.experimental.SuperBuilder;

/**
 * Defines a rendering template to be used in the system implementing this library. It is up to the
 * implementor to add all relevant rendering templates to his system. Each RenderingTemplate can be
 * described by a label, a description and a name.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class RenderingTemplate extends UniqueObject {
  private LocalizedText description;
  private LocalizedText label;
  private String name;

  public RenderingTemplate() {
    super();
  }

  public LocalizedText getDescription() {
    return description;
  }

  public LocalizedText getLabel() {
    return label;
  }

  public String getName() {
    return name;
  }

  public void setDescription(LocalizedText description) {
    this.description = description;
  }

  public void setLabel(LocalizedText label) {
    this.label = label;
  }

  public void setName(String name) {
    this.name = name;
  }

  public abstract static class RenderingTemplateBuilder<
          C extends RenderingTemplate, B extends RenderingTemplateBuilder<C, B>>
      extends UniqueObjectBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B description(Locale locale, String text) {
      LocalizedText description = this.description;
      if (description == null) {
        description = new LocalizedText();
      }
      description.setText(locale, text);
      this.description = description;
      return self();
    }

    public B label(Locale locale, String localizedLabel) {
      LocalizedText label = this.label;
      if (label == null) {
        label = new LocalizedText();
      }
      label.setText(locale, localizedLabel);
      this.label = label;
      return self();
    }

    public B name(String name) {
      this.name = name;
      return self();
    }
  }
}
