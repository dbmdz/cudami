package de.digitalcollections.model.identifiable.semantic;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.SuperBuilder;

@SuperBuilder(buildMethodName = "prebuild")
public class Subject extends Identifiable {

  private String subjectType;

  public Subject() {
    super();
  }

  public Subject(LocalizedText label, Set<Identifier> identifiers, String subjectType) {
    this();
    this.label = label;
    this.identifiers = identifiers;
    this.subjectType = subjectType;
  }

  public String getSubjectType() {
    return subjectType;
  }

  @Override
  protected void init() {
    super.init();
    this.type = IdentifiableType.RESOURCE;
  }

  public void setSubjectType(String subjectType) {
    this.subjectType = subjectType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Subject)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Subject subject = (Subject) o;
    return Objects.equals(subjectType, subject.subjectType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), identifiers, label, subjectType);
  }

  @Override
  public String toString() {
    return "Subject{"
        + "identifiers="
        + identifiers
        + ", label="
        + label
        + ", subjectType='"
        + subjectType
        + '\''
        + ", created="
        + created
        + ", lastModified="
        + lastModified
        + ", uuid="
        + uuid
        + '}';
  }

  public abstract static class SubjectBuilder<C extends Subject, B extends SubjectBuilder<C, B>>
      extends IdentifiableBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }

    public B subjectType(String subjectType) {
      this.subjectType = subjectType;
      return self();
    }
  }
}
