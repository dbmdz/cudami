package de.digitalcollections.model;

import java.util.Objects;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class MainSubType {

  private String mainType;
  private String subType;

  public MainSubType() {}

  /**
   * @param mainType main type is mandatory
   * @param subType optional sub type, can be {@code null}
   */
  public MainSubType(String mainType, String subType) {
    this();
    this.mainType = mainType.toUpperCase();
    this.subType = subType != null ? subType.toUpperCase() : null;
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

  public void setMainType(String mainType) {
    this.mainType = mainType.toUpperCase();
  }

  public void setSubType(String subType) {
    this.subType = subType != null ? subType.toUpperCase() : null;
  }

  @Override
  public String toString() {
    return "MainSubType{"
        + (mainType != null ? "mainType='" + mainType + '\'' : ", mainType=null")
        + (subType != null ? ", subType='" + subType + '\'' : ", subType=null")
        + '}';
  }

  public abstract static class MainSubTypeBuilder<
      C extends MainSubType, B extends MainSubTypeBuilder<C, B>> {

    public B mainType(String mainType) {
      this.mainType = mainType.toUpperCase();
      return self();
    }

    public B subType(String subType) {
      this.subType = subType != null ? subType.toUpperCase() : null;
      return self();
    }
  }
}
