package de.digitalcollections.model.semantic;

import de.digitalcollections.model.UniqueObject;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/** Unique String for tagging objects. */
@SuperBuilder(buildMethodName = "prebuild")
public class Tag extends UniqueObject {

  private String value;

  public Tag() {
    super();
  }

  public Tag(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Tag)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Tag tag = (Tag) o;
    return Objects.equals(value, tag.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Tag{"
        + "value='"
        + value
        + "'"
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + '}';
  }
}
