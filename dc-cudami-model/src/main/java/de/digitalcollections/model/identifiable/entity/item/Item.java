package de.digitalcollections.model.identifiable.entity.item;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.text.LocalizedText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * From https://web.library.yale.edu/cataloging/music/frbr-wemi-music#item:
 *
 * <p>A work is realized by an expression, which is embodied in a manifestation, which is
 * exemplified by an item.
 *
 * <p>An Item is the actual copy of the manifestation that expression takes that is owned by a
 * person or corporate body. It is the only absolutely concrete entity in the FRBR model
 *
 * <p>An item can consist of several distinct physical objects, such as a box set of CDs, or two
 * separately bound volumes with no common sleeve/box that were issued and sold together. Both are
 * considered to be one item.
 *
 * <p>Each copy of Music &amp; Arts 1995 pressing of the recording of Die Zauberflöte by Mozart July
 * 27, 1949 performance by the Konzertvereinigung Wiener Staatsopernchor is an item.
 *
 * <p>Each copy of Sony Classical's 2005 release of the June 10, 14-16, 1955 J.S. Bach's Goldberg
 * variations performed by Glen Gould is an item.
 *
 * <p>Attributes of an item: provenance, location, condition, access restrictions, identifier (if it
 * has one), etc.
 *
 * <p>Dublin Core Fields of an item:
 * https://dublincore.org/resources/userguide/publishing_metadata/#Properties_of_the_legacy_namespace
 * https://www.loc.gov/standards/marcxml/Sandburg/sandburgdc.xml
 *
 * <ul>
 *   <li>title: stored in "label" (may be set without specified locale, but should be set with
 *       locale of language
 *   <li>language
 *   <li>publisher
 *   <li>publicationDate
 *   <li>publicationPlace
 * </ul>
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Item extends Entity {

  private Boolean exemplifiesManifestation;

  private Manifestation manifestation;

  private List<Agent> holders;

  private Item partOfItem;

  public Item() {
    super();
  }

  public LocalizedText getTitle() {
    return getLabel();
  }

  @Override
  protected void init() {
    super.init();
    if (holders == null) {
      holders = new ArrayList<>(0);
    }
  }

  public void setTitle(LocalizedText title) {
    setLabel(title);
  }

  public void setTitle(String title) {
    setLabel(title);
  }

  public Boolean getExemplifiesManifestation() {
    return exemplifiesManifestation;
  }

  public void setExemplifiesManifestation(Boolean exemplifiesManifestation) {
    this.exemplifiesManifestation = exemplifiesManifestation;
  }

  public Manifestation getManifestation() {
    return manifestation;
  }

  public void setManifestation(Manifestation manifestation) {
    this.manifestation = manifestation;
  }

  public List<Agent> getHolders() {
    return holders;
  }

  public void setHolders(List<Agent> holders) {
    this.holders = holders;
  }

  public Item getPartOfItem() {
    return partOfItem;
  }

  public void setPartOfItem(Item partOfItem) {
    this.partOfItem = partOfItem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Item)) {
      return false;
    }
    Item item = (Item) o;
    return super.equals(o)
        && Objects.equals(exemplifiesManifestation, item.exemplifiesManifestation)
        && Objects.equals(manifestation, item.manifestation)
        && Objects.equals(holders, item.holders)
        && Objects.equals(partOfItem, item.partOfItem);
  }

  @Override
  public int hashCode() {
    return super.hashCode()
        + Objects.hash(exemplifiesManifestation, manifestation, holders, partOfItem);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{exemplifiesManifestation="
        + exemplifiesManifestation
        + ", manifestation="
        + manifestation
        + ", holders="
        + holders
        + ", notes="
        + notes
        + ", partOfItem="
        + partOfItem
        + ", customAttributes="
        + customAttributes
        + ", identifiableObjecttype="
        + identifiableObjectType
        + ", navDate="
        + navDate
        + ", refId="
        + refId
        + ", description="
        + description
        + ", identifiers="
        + identifiers
        + ", label="
        + label
        + ", localizedUrlAliases="
        + localizedUrlAliases
        + ", previewImage="
        + previewImage
        + ", previewImageRenderingHints="
        + previewImageRenderingHints
        + ", type="
        + type
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + '}';
  }

  public abstract static class ItemBuilder<C extends Item, B extends ItemBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }
  }
}
