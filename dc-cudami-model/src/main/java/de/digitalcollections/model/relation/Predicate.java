package de.digitalcollections.model.relation;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.text.LocalizedText;
import javax.validation.constraints.NotBlank;
import lombok.experimental.SuperBuilder;

/** Specifies the type of a relation */
@SuperBuilder(buildMethodName = "prebuild")
public class Predicate extends UniqueObject {

  private LocalizedText description;
  private LocalizedText label;

  @NotBlank private String value;

  public Predicate() {
    super();
  }

  public Predicate(String value) {
    this.value = value;
  }

  /**
   * The multilingual, verbose description of the predicate
   *
   * @return the description
   */
  public LocalizedText getDescription() {
    return description;
  }

  /**
   * The multilingual label of the predicate
   *
   * @return multilingual label
   */
  public LocalizedText getLabel() {
    return label;
  }

  /**
   * Value of the predicate, shall be filled in snake_case and lowercase
   *
   * @return value, e.g. <code>is_author_of</code>
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the verbose and multilingual description of the predicate
   *
   * @param description the description
   */
  public void setDescription(LocalizedText description) {
    this.description = description;
  }

  /**
   * Sets the multilingual label
   *
   * @param label the label as LocalizedText
   */
  public void setLabel(LocalizedText label) {
    this.label = label;
  }

  /**
   * Sets the value of the predicate
   *
   * @param value the value, preferrably in snake_case and lowercase, e.g. <code>is_author_of</code>
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{value='"
        + value
        + "', label="
        + label
        + ", description="
        + description
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + "}";
  }
}
