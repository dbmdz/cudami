package de.digitalcollections.model.identifiable.entity.digitalobject;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.versioning.Version;
import de.digitalcollections.model.legal.License;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * A (cultural) digital object, can be a retro digitization of a physical object or a digital native
 * object.
 *
 * <p>A digital object can be related to an {@link Item item}, and it also be part of a parent
 * digital object.
 */
@SuperBuilder(buildMethodName = "prebuild")
public class DigitalObject extends Entity {
  // FIXME We need to specify this! private Availablity availablity;

  /** Details about the creation of the digital object: Who created it, when and where. */
  private CreationInfo creationInfo;

  /** Sorted list of file resources like images, audio files etc. */
  private List<FileResource> fileResources;

  /** The related item (can be null, if not applicable). */
  private Item item;

  /** licence of the digital object. */
  private License license;

  /**
   * Sorted list of links (with description) to machine readable formats like Marc, RDF, METS or
   * IIIF-Manifest.
   */
  private List<LinkedDataFileResource> linkedDataResources;

  /**
   * number of related binary files for the presentation, like scans in a book, photos of an object,
   * audio files for records etc.
   */
  private Integer numberOfBinaryResources;

  /** The parent digital object, if the current one is an embedded one. */
  private DigitalObject parent;

  /**
   * Sorted list of links (with description and MIME type) to human readable formats like a
   * permalink, OPAC/catalogue page, PDF download, ...
   */
  private List<FileResource> renderingResources;

  /** version of the digital object. */
  private Version version;

  /** Default constructor, which also sets the EntityType to {@link EntityType#DIGITAL_OBJECT} */
  public DigitalObject() {
    super();
  }

  public void addFileResource(FileResource fileResource) {
    fileResources.add(fileResource);
  }

  /**
   * @return meta information about the creation of the digital object
   */
  public CreationInfo getCreationInfo() {
    return creationInfo;
  }

  /**
   * @return the sorted list of file resources, like images or audio files
   */
  public List<FileResource> getFileResources() {
    return fileResources;
  }

  /**
   * @return the item, the digital object belongs to. Otherwise, return null.
   */
  public Item getItem() {
    return item;
  }

  /**
   * @return the licence for the digital object (not for the metadata!)
   */
  public License getLicense() {
    return license;
  }

  /**
   * @return the sorted list of links (with description) to machine readable formats
   */
  public List<LinkedDataFileResource> getLinkedDataResources() {
    return linkedDataResources;
  }

  /**
   * @return the number of binary resources for presentation
   */
  public Integer getNumberOfBinaryResources() {
    return numberOfBinaryResources;
  }

  /**
   * @return the parent of the digital object, it available. Otherwise, return null.
   */
  public DigitalObject getParent() {
    return parent;
  }

  /**
   * @return the sorted list of links (with description and MIME type) to human readable formats
   */
  public List<FileResource> getRenderingResources() {
    return renderingResources;
  }

  /**
   * @return the version of the digital object
   */
  public Version getVersion() {
    return version;
  }

  @Override
  protected void init() {
    super.init();
    if (fileResources == null) {
      fileResources = new ArrayList<>(0);
    }
    if (linkedDataResources == null) {
      linkedDataResources = new ArrayList<>(0);
    }
    if (renderingResources == null) {
      renderingResources = new ArrayList<>(0);
    }
  }

  /**
   * Sets the information about the creation of the digital object
   *
   * @param creationInfo the meta information about the creation
   */
  public void setCreationInfo(CreationInfo creationInfo) {
    this.creationInfo = creationInfo;
  }

  /**
   * Sets the sorted list of file resources
   *
   * @param fileResources the sorted list of file resources
   */
  public void setFileResources(List<FileResource> fileResources) {
    this.fileResources = fileResources;
  }

  /**
   * Set the item, the digital object belongs to
   *
   * @param item the item, the digital object belongs to
   */
  public void setItem(Item item) {
    this.item = item;
  }

  /**
   * Sets the licence for the digital object
   *
   * @param license the licence of the digital object
   */
  public void setLicense(License license) {
    this.license = license;
  }

  /**
   * Sets a sorted list of links (with description) to machine readable formats
   *
   * @param linkedDataResources the sorted list of links
   */
  public void setLinkedDataResources(List<LinkedDataFileResource> linkedDataResources) {
    this.linkedDataResources = linkedDataResources;
  }

  /**
   * Sets the number of binary resources for presentation
   *
   * @param numberOfBinaryResources the number of binary resources
   */
  public void setNumberOfBinaryResources(Integer numberOfBinaryResources) {
    this.numberOfBinaryResources = numberOfBinaryResources;
  }

  /**
   * Sets the parent of the digital object
   *
   * @param parent of the digital object
   */
  public void setParent(DigitalObject parent) {
    this.parent = parent;
  }

  /**
   * Sets the sorted list of links (with description and MIME type) to human readable formats
   *
   * @param renderingResources the sorted list of links
   */
  public void setRenderingResources(List<FileResource> renderingResources) {
    this.renderingResources = renderingResources;
  }

  /**
   * Sets the version of the digital object
   *
   * @param version of the digital object
   */
  public void setVersion(Version version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DigitalObject)) return false;
    if (!super.equals(o)) return false;
    DigitalObject that = (DigitalObject) o;
    return Objects.equals(creationInfo, that.creationInfo)
        && Objects.equals(fileResources, that.fileResources)
        && Objects.equals(item, that.item)
        && Objects.equals(license, that.license)
        && Objects.equals(linkedDataResources, that.linkedDataResources)
        && Objects.equals(numberOfBinaryResources, that.numberOfBinaryResources)
        && Objects.equals(parent, that.parent)
        && Objects.equals(renderingResources, that.renderingResources)
        && Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        creationInfo,
        fileResources,
        item,
        license,
        linkedDataResources,
        numberOfBinaryResources,
        parent,
        renderingResources,
        version);
  }

  @Override
  public String toString() {
    return "DigitalObject{"
        + "created="
        + created
        + ", description="
        + description
        + ", identifiers="
        + identifiers
        + ", label="
        + label
        + ", lastModified="
        + lastModified
        + ", localizedUrlAliases="
        + localizedUrlAliases
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", item="
        + item
        + ", parent="
        + parent
        + ", fileResources="
        + fileResources
        + ", linkeddataResources="
        + linkedDataResources
        + ", renderingResources="
        + renderingResources
        + ", numberOfBinaryResources="
        + numberOfBinaryResources
        + ", license="
        + license
        + ", version="
        + version
        + ", creationInfo="
        + creationInfo
        + ", customAttributes="
        + customAttributes
        + ", navDate="
        + navDate
        + ", refId="
        + refId
        + '}';
  }

  public abstract static class DigitalObjectBuilder<
          C extends DigitalObject, B extends DigitalObjectBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B linkedDataFileResource(LinkedDataFileResource linkedDataFileResource) {
      if (linkedDataResources == null) {
        linkedDataResources = new ArrayList<>(0);
      }
      linkedDataResources.add(linkedDataFileResource);
      return self();
    }

    public B renderingResource(FileResource renderingResource) {
      if (renderingResources == null) {
        renderingResources = new ArrayList<>(0);
      }
      renderingResources.add(renderingResource);
      return self();
    }
  }
}
