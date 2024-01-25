package de.digitalcollections.model.identifiable.entity.manifestation;

import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.text.Title;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.SuperBuilder;

/**
 * From https://web.library.yale.edu/cataloging/music/frbr-wemi-music#work:
 *
 * <p>A work is realized by an expression, which is embodied in a manifestation, which is
 * exemplified by an item.
 *
 * <p>A Manifestation is the physical embodiment (i.e., publication) of an expression of a work that
 * is produced by a person or corporate body.° A publication of an expression is called a
 * manifestation
 *
 * <p>Attributes of a manifestation: title, statement of responsibility, edition, imprint (place,
 * publisher, date), form/extent and dimensions of carrier, terms of availability, mode of access,
 * identifier (if it has one), etc. For sound recordings: playing speed, groove width, kind of
 * cutting, tape configuration, kind of sound, special reproduction characteristic
 *
 * <p>Music can be performed, but only when it is recorded is there a manifestation Work: J.S.
 * Bach's Goldberg variations Expression: June 10, 14-16, 1955 performance by Glen Gould
 * Manifestation 1: the recording on a phonograph record Manifestation 2: a re-release on a compact
 * disc Manifestation 3: a digitization on an MP3 file
 */
@SuperBuilder(buildMethodName = "prebuild")
public class Manifestation extends Entity {

  /**
   * Text describing amount of material or content of manifestation.
   *
   * <p>Examples: "37 Seiten", "1 ungezähltes Blatt Bildtafel, 108 Seiten", "1 Partitur (39
   * Seiten)", "V, 64 S., [12] Bl.", "1 Buchdeckel"
   */
  private String composition;

  /**
   * Text describing physical dimensions of object.
   *
   * <p>Examples: "29,2 x 76,9 x 2,8 cm", "8°", "4", "42 x 31 cm"
   */
  private String dimensions;

  private LinkedHashSet<ExpressionType> expressionTypes;
  private List<EntityRelation> relations;
  private Locale language;
  private String manifestationType;
  private String manufacturingType;
  private LinkedHashSet<String> mediaTypes;
  private LinkedHashSet<Locale> otherLanguages;
  private List<RelationSpecification<Manifestation>> parents;
  private PublicationInfo publicationInfo;
  private DistributionInfo distributionInfo;
  private ProductionInfo productionInfo;

  /**
   * Text describing the scale of object, e.g. of a map.
   *
   * <p>Examples: "[Ca. 1:820 000]"
   */
  private String scale;

  private List<Title> titles;
  private String version;
  private Work work;

  public Manifestation() {
    super();
  }

  public void addParent(RelationSpecification<Manifestation> parent) {
    if (parents == null) {
      parents = new ArrayList<>();
    }
    parents.add(parent);
  }

  public void addRelation(EntityRelation relation) {
    relations.add(relation);
  }

  /**
   * @return Text describing amount of material or content of manifestation.
   */
  public String getComposition() {
    return composition;
  }

  /**
   * @return Text describing physical dimensions of object
   */
  public String getDimensions() {
    return dimensions;
  }

  public LinkedHashSet<ExpressionType> getExpressionTypes() {
    return expressionTypes;
  }

  public List<EntityRelation> getRelations() {
    return relations;
  }

  public Locale getLanguage() {
    return language;
  }

  public String getManifestationType() {
    return manifestationType;
  }

  public String getManufacturingType() {
    return manufacturingType;
  }

  public LinkedHashSet<String> getMediaTypes() {
    return mediaTypes;
  }

  public LinkedHashSet<Locale> getOtherLanguages() {
    return otherLanguages;
  }

  public List<RelationSpecification<Manifestation>> getParents() {
    return parents;
  }

  /**
   * @return the publication information (date and involved publishers, if given)
   */
  public PublicationInfo getPublicationInfo() {
    return publicationInfo;
  }

  /**
   * @return the distribution information (date and involved publishers, if given)
   */
  public DistributionInfo getDistributionInfo() {
    return distributionInfo;
  }

  /**
   * @return the production information (date and involved publishers, if given)
   */
  public ProductionInfo getProductionInfo() {
    return productionInfo;
  }

  /**
   * @return Text describing the scale of object, e.g. of a map
   */
  public String getScale() {
    return scale;
  }

  public List<Title> getTitles() {
    return titles;
  }

  public String getVersion() {
    return version;
  }

  public Work getWork() {
    return work;
  }

  @Override
  protected void init() {
    super.init();
    identifiableObjectType = IdentifiableObjectType.MANIFESTATION;
    if (expressionTypes == null) expressionTypes = new LinkedHashSet<>(0);
    if (mediaTypes == null) mediaTypes = new LinkedHashSet<>(0);
    if (otherLanguages == null) otherLanguages = new LinkedHashSet<>(0);
    if (parents == null) parents = new ArrayList<>(0);
    if (titles == null) titles = new ArrayList<>();
    if (relations == null) relations = new ArrayList<>();
  }

  /**
   * @param composition Text describing amount of material or content of manifestation.
   */
  public void setComposition(String composition) {
    this.composition = composition;
  }

  /**
   * @param dimensions Text describing physical dimensions of object
   */
  public void setDimensions(String dimensions) {
    this.dimensions = dimensions;
  }

  public void setExpressionTypes(LinkedHashSet<ExpressionType> expressionTypes) {
    this.expressionTypes = expressionTypes;
  }

  public void setRelations(List<EntityRelation> relations) {
    this.relations = relations;
  }

  public void setLanguage(Locale language) {
    this.language = language;
  }

  public void setManifestationType(String manifestationType) {
    this.manifestationType = manifestationType;
  }

  public void setManufacturingType(String manufacturingType) {
    this.manufacturingType = manufacturingType;
  }

  public void setMediaTypes(LinkedHashSet<String> mediaTypes) {
    this.mediaTypes = mediaTypes;
  }

  public void setOtherLanguages(LinkedHashSet<Locale> otherLanguages) {
    this.otherLanguages = otherLanguages;
  }

  public void setParents(List<RelationSpecification<Manifestation>> parents) {
    this.parents = parents;
  }

  public void setPublicationInfo(PublicationInfo publicationInfo) {
    this.publicationInfo = publicationInfo;
  }

  public void setDistributionInfo(DistributionInfo distributionInfo) {
    this.distributionInfo = distributionInfo;
  }

  public void setProductionInfo(ProductionInfo productionInfo) {
    this.productionInfo = productionInfo;
  }

  /**
   * @param scale Text describing the scale of object, e.g. of a map
   */
  public void setScale(String scale) {
    this.scale = scale;
  }

  public void setTitles(List<Title> titles) {
    this.titles = titles;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setWork(Work work) {
    this.work = work;
  }

  @Override
  // IMPORTANT: Dump only the UUIDs of the relations, otherwise you'll land
  // in recursion hell!
  public String toString() {
    return "Manifestation{"
        + "composition='"
        + composition
        + '\''
        + ", dimensions='"
        + dimensions
        + '\''
        + ", expressionTypes="
        + expressionTypes
        + ", relations="
        + dumpShortenedRelations(relations)
        + ", language="
        + language
        + ", manifestationType="
        + manifestationType
        + ", manufacturingType="
        + manufacturingType
        + ", mediaTypes="
        + mediaTypes
        + ", otherLanguages="
        + otherLanguages
        + ", parents="
        + parents
        + ", publicationInfo="
        + publicationInfo
        + ", distributionInfo="
        + distributionInfo
        + ", productionInfo="
        + productionInfo
        + ", scale='"
        + scale
        + '\''
        + ", subjects="
        + subjects
        + ", titles="
        + titles
        + ", version='"
        + version
        + '\''
        + ", work="
        + work
        + ", customAttributes="
        + customAttributes
        + ", navDate="
        + navDate
        + ", refId="
        + refId
        + ", notes="
        + notes
        + ", description="
        + description
        + ", identifiableObjectType="
        + identifiableObjectType
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
        + ", tags="
        + tags
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Manifestation)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Manifestation that = (Manifestation) o;
    return Objects.equals(composition, that.composition)
        && Objects.equals(dimensions, that.dimensions)
        && Objects.equals(expressionTypes, that.expressionTypes)
        && Objects.equals(relations, that.relations)
        && Objects.equals(language, that.language)
        && Objects.equals(manifestationType, that.manifestationType)
        && Objects.equals(manufacturingType, that.manufacturingType)
        && Objects.equals(mediaTypes, that.mediaTypes)
        && Objects.equals(otherLanguages, that.otherLanguages)
        && Objects.equals(parents, that.parents)
        && Objects.equals(publicationInfo, that.publicationInfo)
        && Objects.equals(distributionInfo, that.distributionInfo)
        && Objects.equals(productionInfo, that.productionInfo)
        && Objects.equals(scale, that.scale)
        && Objects.equals(titles, that.titles)
        && Objects.equals(version, that.version)
        && Objects.equals(work, that.work);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        composition,
        dimensions,
        expressionTypes,
        relations,
        language,
        manifestationType,
        manufacturingType,
        mediaTypes,
        otherLanguages,
        parents,
        publicationInfo,
        distributionInfo,
        productionInfo,
        scale,
        titles,
        version,
        work);
  }

  /**
   * Since EntityRelations reference Entities, e.g. a Manifestation, we can get into a recursion
   * here.
   *
   * <p>To avoid this, the EntityRelations are dumped in a shortened way by only returning the UUIDs
   * of the entities.
   *
   * @param relations a list of EntityRelations
   * @return A texual representation of the list with subject uuid, predicate and object uuid
   */
  private String dumpShortenedRelations(List<EntityRelation> relations) {
    return "["
        + relations.stream().map(EntityRelation::toShortenedString).collect(Collectors.joining(","))
        + "]";
  }

  public abstract static class ManifestationBuilder<
          C extends Manifestation, B extends ManifestationBuilder<C, B>>
      extends EntityBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B expressionType(ExpressionType type) {
      if (expressionTypes == null) {
        expressionTypes = new LinkedHashSet<>(1);
      }
      expressionTypes.add(type);
      return self();
    }

    public B relation(EntityRelation relation) {
      if (relations == null) {
        relations = new ArrayList<>(1);
      }
      relations.add(relation);
      return self();
    }

    public B manifestationType(String manifestationType) {
      this.manifestationType = manifestationType;
      return self();
    }

    public B mediaType(String mediaType) {
      if (mediaTypes == null) {
        mediaTypes = new LinkedHashSet<>(1);
      }
      mediaTypes.add(mediaType);
      return self();
    }

    public B otherLanguage(Locale lang) {
      if (otherLanguages == null) {
        otherLanguages = new LinkedHashSet<>(1);
      }
      otherLanguages.add(lang);
      return self();
    }

    public B parent(RelationSpecification<Manifestation> parent) {
      if (parents == null) {
        parents = new ArrayList<>(1);
      }
      parents.add(parent);
      return self();
    }

    public B title(Title title) {
      if (titles == null) {
        titles = new ArrayList<>(1);
      }
      titles.add(title);
      return self();
    }
  }
}
