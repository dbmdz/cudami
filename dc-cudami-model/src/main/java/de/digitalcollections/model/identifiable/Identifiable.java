package de.digitalcollections.model.identifiable;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

/**
 * An Identifiable is an uniquely identifiable {@link Entity} or {@link FileResource}, having one or
 * more unique {@link Identifier}(s).<br>
 *
 * <ul>
 *   <li>FileResources have one (or more) technical (system wide) Identifiers (e.g. an UUID)
 *   <li>Entities additionally have one (or more) “domain-specific” (system independent) Identifiers
 *       (e.g. GND-ID, VIAF-ID)
 * </ul>
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Identifiable extends UniqueObject {

  protected LocalizedStructuredContent description;
  protected IdentifiableObjectType identifiableObjectType;
  protected Set<Identifier> identifiers;
  protected LocalizedText label;
  protected LocalizedUrlAliases localizedUrlAliases;
  protected ImageFileResource previewImage;
  protected RenderingHintsPreviewImage previewImageRenderingHints;
  protected Set<Subject> subjects;
  protected Set<Tag> tags;
  protected IdentifiableType type;

  public Identifiable() {
    super();
  }

  public void addIdentifier(Identifier identifier) {
    if (identifiers == null) {
      identifiers = new HashSet<>(1);
    }
    identifiers.add(Objects.requireNonNull(identifier));
  }

  public void addLocalizedUrlAlias(UrlAlias urlAlias) {
    if (localizedUrlAliases == null) {
      localizedUrlAliases = new LocalizedUrlAliases(urlAlias);
    } else {
      localizedUrlAliases.add(urlAlias);
    }
  }

  public void addTag(Tag tag) {
    if (tags == null) {
      tags = new HashSet<>(1);
    }
    tags.add(tag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Identifiable)) {
      return false;
    }
    Identifiable that = (Identifiable) o;
    return Objects.equals(uuid, that.uuid)
        && Objects.equals(description, that.description)
        && identifiableObjectType == that.identifiableObjectType
        && Objects.equals(identifiers, that.identifiers)
        && Objects.equals(label, that.label)
        && Objects.equals(localizedUrlAliases, that.localizedUrlAliases)
        && Objects.equals(previewImage, that.previewImage)
        && Objects.equals(previewImageRenderingHints, that.previewImageRenderingHints)
        && Objects.equals(subjects, that.subjects)
        && Objects.equals(tags, that.tags)
        && type == that.type;
  }

  public LocalizedStructuredContent getDescription() {
    return description;
  }

  public IdentifiableObjectType getIdentifiableObjectType() {
    return identifiableObjectType;
  }

  public Identifier getIdentifierByNamespace(String namespace) {
    if (namespace == null || identifiers == null || identifiers.isEmpty()) {
      return null;
    }
    for (Identifier identifier : identifiers) {
      if (namespace.equals(identifier.getNamespace())) {
        return identifier;
      }
    }
    return null;
  }

  public Set<Identifier> getIdentifiers() {
    return identifiers;
  }

  public LocalizedText getLabel() {
    return label;
  }

  public LocalizedUrlAliases getLocalizedUrlAliases() {
    return localizedUrlAliases;
  }

  public ImageFileResource getPreviewImage() {
    return previewImage;
  }

  public RenderingHintsPreviewImage getPreviewImageRenderingHints() {
    return previewImageRenderingHints;
  }

  /**
   * Returns the primary (= currently active) url alias of this Identifiable for a specified
   * language and website. If no related url alias is found the website independent url alias of the
   * given locale is returned (that has been generated during saving from the Identifiable's label).
   *
   * @param locale target locale for retrieving primary url alias
   * @param website target website for retrieving primary url alias
   * @return primary url alias for website and locale or default
   */
  public UrlAlias getPrimaryUrlAlias(Locale locale, Website website) {
    // check if there are any url aliases at all
    if (localizedUrlAliases == null || localizedUrlAliases.isEmpty()) {
      return null;
    }
    List<UrlAlias> urlAliases =
        localizedUrlAliases.get(Locale.forLanguageTag(locale.getLanguage()));
    // check if there are url aliases for the given language
    if (urlAliases == null || urlAliases.isEmpty()) {
      return null;
    }
    Supplier<Stream<UrlAlias>> primaries = () -> urlAliases.stream().filter(u -> u.isPrimary());
    if (website != null) {
      UrlAlias urlAlias =
          primaries
              .get()
              .filter(
                  u -> u.getWebsite() != null && u.getWebsite().getUuid().equals(website.getUuid()))
              .findFirst()
              .orElse(null);
      // given website found, return it
      if (urlAlias != null) {
        return urlAlias;
      }
    }
    // no given website or given website not found, use default alias
    return primaries.get().filter(u -> u.getWebsite() == null).findFirst().orElse(null);
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public IdentifiableType getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        uuid,
        description,
        identifiableObjectType,
        identifiers,
        label,
        localizedUrlAliases,
        previewImage,
        previewImageRenderingHints,
        subjects,
        tags,
        type);
  }

  @Override
  protected void init() {
    super.init();
    if (identifiableObjectType == null)
      identifiableObjectType = IdentifiableObjectType.getByClass(getClass());
    if (identifiers == null) {
      identifiers = new HashSet<>(0);
    }
    if (subjects == null) {
      subjects = new HashSet<>(0);
    }
    if (tags == null) {
      tags = new HashSet<>(0);
    }
  }

  public void removeIdentifier(String namespace) {
    if (identifiers == null || namespace == null || namespace.isBlank()) {
      return;
    }
    identifiers =
        identifiers.stream()
            .filter(i -> !namespace.equals(i.getNamespace()))
            .collect(Collectors.toSet());
  }

  public void setDescription(LocalizedStructuredContent description) {
    this.description = description;
  }

  public void setIdentifiableObjectType(IdentifiableObjectType identifiableObjectType) {
    this.identifiableObjectType = identifiableObjectType;
  }

  public void setIdentifiers(Set<Identifier> identifiers) {
    this.identifiers = identifiers;
  }

  public void setLabel(String label) {
    this.label = new LocalizedText(Locale.ROOT, label);
  }

  public void setLabel(LocalizedText label) {
    this.label = label;
  }

  public void setLocalizedUrlAliases(LocalizedUrlAliases localizedUrlAliases) {
    this.localizedUrlAliases = localizedUrlAliases;
  }

  public void setPreviewImage(ImageFileResource previewImage) {
    this.previewImage = previewImage;
  }

  public void setPreviewImageRenderingHints(RenderingHintsPreviewImage previewImageRenderingHints) {
    this.previewImageRenderingHints = previewImageRenderingHints;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }

  public void setType(IdentifiableType identifiableType) {
    this.type = identifiableType;
  }

  public void addSubject(Subject subject) {
    subjects.add(subject);
  }

  public Set<Subject> getSubjects() {
    return subjects;
  }

  public void setSubjects(Set<Subject> subjects) {
    this.subjects = subjects;
  }

  public abstract static class IdentifiableBuilder<
          C extends Identifiable, B extends IdentifiableBuilder<C, B>>
      extends UniqueObjectBuilder<C, B> {

    public B altText(Locale locale, String text) {
      if (previewImageRenderingHints == null) {
        previewImageRenderingHints = new RenderingHintsPreviewImage();
      }
      LocalizedText altText = previewImageRenderingHints.getAltText();
      if (altText == null) {
        altText = new LocalizedText();
      }
      altText.setText(locale, text);
      previewImageRenderingHints.setAltText(altText);
      return self();
    }

    public B altTextFromLabel() {
      if (previewImageRenderingHints == null) {
        previewImageRenderingHints = new RenderingHintsPreviewImage();
      }
      previewImageRenderingHints.setAltText(this.label);
      return self();
    }

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B description(Locale locale, String text) {
      if (description == null) {
        description = new LocalizedStructuredContent();
      }
      StructuredContent localizedDescription = description.get(locale);
      if (localizedDescription == null) {
        localizedDescription = new StructuredContent();
      }
      ContentBlock paragraph = StringUtils.hasText(text) ? new Paragraph(text) : new Paragraph();
      localizedDescription.addContentBlock(paragraph);
      description.put(locale, localizedDescription);
      return self();
    }

    public B description(String lang, String text) {
      return description(Locale.forLanguageTag(lang), text);
    }

    public B description(LocalizedStructuredContent description) {
      this.description = description;
      return self();
    }

    public B dontOpenPreviewImageInNewWindow() {
      if (previewImageRenderingHints == null) {
        previewImageRenderingHints = new RenderingHintsPreviewImage();
      }
      previewImageRenderingHints.setOpenLinkInNewWindow(false);
      return self();
    }

    public B identifier(Identifier identifier) {
      if (this.identifiers == null) {
        this.identifiers = new HashSet<>(1);
      }
      identifiers.add(identifier);
      return self();
    }

    public B label(Locale locale, String localizedLabel) {
      if (label == null) {
        label = new LocalizedText();
      }
      label.setText(locale, localizedLabel);
      return self();
    }

    public B label(String nonlocalizedLabel) {
      this.label = new LocalizedText(Locale.ROOT, nonlocalizedLabel);
      return self();
    }

    public B label(LocalizedText localizedText) {
      this.label = localizedText;
      return self();
    }

    public B openLinkInNewWindow() {
      return openPreviewImageInNewWindow();
    }

    public B openPreviewImageInNewWindow() {
      if (previewImageRenderingHints == null) {
        previewImageRenderingHints = new RenderingHintsPreviewImage();
      }
      previewImageRenderingHints.setOpenLinkInNewWindow(true);
      return self();
    }

    public B previewImage(ImageFileResource previewImage) {
      this.previewImage = previewImage;
      return self();
    }

    public B previewImage(String url, int width, int height) {
      String[] fileNameParts = url.split("/\\//");
      return previewImage(
          ImageFileResource.builder()
              .filename(fileNameParts[fileNameParts.length - 1])
              .uri(url)
              .width(width)
              .height(height)
              .build());
    }

    public B previewImage(String fileName, String uuid, String uri) {
      return previewImage(
          ImageFileResource.builder().filename(fileName).uri(uri).uuid(uuid).build());
    }

    public B previewImage(String fileName, String uuid, String uri, MimeType mimeType) {
      return previewImage(
          ImageFileResource.builder()
              .uuid(uuid)
              .filename(fileName)
              .uri(uri)
              .mimeType(mimeType)
              .build());
    }

    public B previewImage(
        String fileName, String uuid, String uri, MimeType mimeType, String httpBaseUrl) {
      return previewImage(
          ImageFileResource.builder()
              .uuid(uuid)
              .filename(fileName)
              .uri(uri)
              .mimeType(mimeType)
              .httpBaseUrl(httpBaseUrl)
              .build());
    }

    public B primaryLocalizedUrlAlias(Locale targetLocale, String slug) {
      this.localizedUrlAliases =
          new LocalizedUrlAliases(
              UrlAlias.builder().targetLanguage(targetLocale).slug(slug).isPrimary().build());
      return self();
    }

    public void setInternalReferences(C c) {
      if (identifiers == null) {
        c.setIdentifiers(new HashSet<>(0));
      }

      // For each UrlAlias, the target must be set to an Identifiable with core data of the
      // identifiable
      // (important: not having the UrlAliasses again -> otherwise recursion!)
      if (c.getLocalizedUrlAliases() != null && !c.getLocalizedUrlAliases().isEmpty()) {
        c.getLocalizedUrlAliases()
            .forEach(
                (locale, urlAliasList) -> {
                  urlAliasList.forEach(
                      u ->
                          u.setTarget(
                              Identifiable.builder()
                                  .uuid(c.getUuid())
                                  .identifiableObjectType(c.getIdentifiableObjectType())
                                  .type(c.getType())
                                  .build()));
                });
      }
    }

    public B subject(Subject subject) {
      if (subjects == null) {
        subjects = new HashSet<>(1);
      }
      subjects.add(subject);
      return self();
    }

    public B tag(Tag tag) {
      if (tags == null) {
        tags = new HashSet<>(1);
      }
      tags.add(tag);
      return self();
    }

    public B title(Locale locale, String text) {
      if (previewImageRenderingHints == null) {
        previewImageRenderingHints = new RenderingHintsPreviewImage();
      }
      LocalizedText title = previewImageRenderingHints.getTitle();
      if (title == null) {
        title = new LocalizedText();
      }
      title.setText(locale, text);
      previewImageRenderingHints.setTitle(title);
      return self();
    }

    public B titleFromLabel() {
      if (previewImageRenderingHints == null) {
        previewImageRenderingHints = new RenderingHintsPreviewImage();
      }
      previewImageRenderingHints.setTitle(this.label);
      return self();
    }
  }
}
