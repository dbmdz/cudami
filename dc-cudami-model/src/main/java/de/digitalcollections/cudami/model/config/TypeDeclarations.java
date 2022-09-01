package de.digitalcollections.cudami.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypeDeclarations {

  private List<String> involvementRoles;
  private List<String> manufacturingTypes;
  private List<String> mediaTypes;
  private List<String> tagTypes;

  private List<MainSubType> expressionTypes;
  private List<MainSubType> titleTypes;

  @JsonCreator(mode = Mode.PROPERTIES)
  public TypeDeclarations(
      @JsonProperty(value = "involvementRoles") List<String> involvementRoles,
      @JsonProperty(value = "manufacturingTypes") List<String> manufacturingTypes,
      @JsonProperty(value = "mediaTypes") List<String> mediaTypes,
      @JsonProperty(value = "tagTypes") List<String> tagTypes,
      @JsonProperty(value = "expressionTypes") List<MainSubType> expressionTypes,
      @JsonProperty(value = "titleTypes") List<MainSubType> titleTypes) {
    this.involvementRoles =
        involvementRoles != null ? List.copyOf(involvementRoles) : Collections.emptyList();
    this.manufacturingTypes =
        manufacturingTypes != null ? List.copyOf(manufacturingTypes) : Collections.emptyList();
    this.mediaTypes = mediaTypes != null ? List.copyOf(mediaTypes) : Collections.emptyList();
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

  public List<String> getTagTypes() {
    return List.copyOf(tagTypes);
  }

  public List<MainSubType> getExpressionTypes() {
    return List.copyOf(expressionTypes);
  }

  public List<MainSubType> getTitleTypes() {
    return List.copyOf(titleTypes);
  }

  private List<MainSubType> searchMainSubType(List<MainSubType> types, MainSubType pattern) {
    Optional<MainSubType> result = types.stream().filter(mst -> mst.equals(pattern)).findFirst();
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
  public List<MainSubType> findExpressionTypes(String mainType, String subType) {
    if (mainType == null && subType == null) {
      return Collections.emptyList();
    }
    MainSubType pattern = new MainSubType(mainType, subType);
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
  public List<MainSubType> findTitleTypes(String mainType, String subType) {
    if (mainType == null && subType == null) {
      return Collections.emptyList();
    }
    MainSubType pattern = new MainSubType(mainType, subType);
    return searchMainSubType(titleTypes, pattern);
  }

  public static class MainSubType {

    private final String mainType;
    private final String subType;

    @JsonCreator(mode = Mode.PROPERTIES)
    public MainSubType(
        @JsonProperty(value = "mainType") String mainType,
        @JsonProperty(value = "subType") String subType) {
      this.mainType = mainType;
      this.subType = subType;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof MainSubType)) {
        return false;
      }
      MainSubType other = (MainSubType) obj;
      return Objects.equals(mainType, other.mainType) && Objects.equals(subType, other.subType);
    }

    @Override
    public int hashCode() {
      return (mainType != null ? mainType.hashCode() : 0)
          + (subType != null ? subType.hashCode() : 0)
          + 187;
    }

    public String getMainType() {
      return mainType;
    }

    public String getSubType() {
      return subType;
    }
  }
}
