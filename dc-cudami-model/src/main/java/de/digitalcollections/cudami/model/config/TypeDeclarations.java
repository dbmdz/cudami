package de.digitalcollections.cudami.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.digitalcollections.model.MainSubType;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.identifiable.entity.work.TitleType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypeDeclarations {

  private List<String> involvementRoles;
  private List<String> manufacturingTypes;
  private List<String> mediaTypes;
  private List<String> subjectTypes;
  private List<String> tagTypes;

  private List<ExpressionType> expressionTypes;
  private List<TitleType> titleTypes;

  @JsonCreator(mode = Mode.PROPERTIES)
  public TypeDeclarations(
      @JsonProperty(value = "involvementRoles") List<String> involvementRoles,
      @JsonProperty(value = "manufacturingTypes") List<String> manufacturingTypes,
      @JsonProperty(value = "mediaTypes") List<String> mediaTypes,
      @JsonProperty(value = "subjectTypes") List<String> subjectTypes,
      @JsonProperty(value = "tagTypes") List<String> tagTypes,
      @JsonProperty(value = "expressionTypes") List<ExpressionType> expressionTypes,
      @JsonProperty(value = "titleTypes") List<TitleType> titleTypes) {
    this.involvementRoles =
        involvementRoles != null ? List.copyOf(involvementRoles) : Collections.emptyList();
    this.manufacturingTypes =
        manufacturingTypes != null ? List.copyOf(manufacturingTypes) : Collections.emptyList();
    this.mediaTypes = mediaTypes != null ? List.copyOf(mediaTypes) : Collections.emptyList();
    this.subjectTypes = subjectTypes != null ? List.copyOf(subjectTypes) : Collections.emptyList();
    this.tagTypes = tagTypes != null ? List.copyOf(tagTypes) : Collections.emptyList();

    this.expressionTypes =
        expressionTypes != null ? List.copyOf(expressionTypes) : Collections.emptyList();
    this.titleTypes = titleTypes != null ? List.copyOf(titleTypes) : Collections.emptyList();
  }

  public List<String> getInvolvementRoles() {
    return List.copyOf(involvementRoles);
  }

  public List<String> getManufacturingTypes() {
    return List.copyOf(manufacturingTypes);
  }

  public List<String> getMediaTypes() {
    return List.copyOf(mediaTypes);
  }

  public List<String> getSubjectTypes() {
    return List.copyOf(subjectTypes);
  }

  public List<String> getTagTypes() {
    return List.copyOf(tagTypes);
  }

  public List<ExpressionType> getExpressionTypes() {
    return List.copyOf(expressionTypes);
  }

  public List<TitleType> getTitleTypes() {
    return List.copyOf(titleTypes);
  }

  private <T extends MainSubType> List<T> searchMainSubType(List<T> types, T pattern) {
    Optional<T> result = types.stream().filter(mst -> mst.equals(pattern)).findFirst();
    if (result.isPresent()) {
      return List.of(result.get());
    }
    return types.stream()
        .filter(
            mst ->
                mst.getMainType().equals(pattern.getMainType())
                    || mst.getSubType().equals(pattern.getSubType()))
        .collect(Collectors.toList());
  }

  /**
   * Finds {@code ExpressionType}s that meet the passed main and sub type. If an exact match is not
   * found then all types matching either main or sub type are returned.
   *
   * @param mainType can be {@code null}
   * @param subType can be {@code null}
   * @return list of matching types or an empty list
   */
  public List<ExpressionType> findExpressionTypes(String mainType, String subType) {
    if (mainType == null && subType == null) {
      return Collections.emptyList();
    }
    ExpressionType pattern = new ExpressionType(mainType, subType);
    return searchMainSubType(expressionTypes, pattern);
  }

  /**
   * Finds {@code TitleType}s that meet the passed main and sub type. If an exact match is not found
   * then all types matching either main or sub type are returned.
   *
   * @param mainType can be {@code null}
   * @param subType can be {@code null}
   * @return list of matching types or an empty list
   */
  public List<TitleType> findTitleTypes(String mainType, String subType) {
    if (mainType == null && subType == null) {
      return Collections.emptyList();
    }
    TitleType pattern = new TitleType(mainType, subType);
    return searchMainSubType(titleTypes, pattern);
  }
}
