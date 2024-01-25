package de.digitalcollections.model.identifiable.relation;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An IdentifiableToFileResourceRelation describes the relation between an identifiable and a
 * fileresource (subject being related to object in terms of predicate)
 */
public class IdentifiableToFileResourceRelation {

  public static Builder builder() {
    return new Builder();
  }

  private FileResource object;
  private String predicate;
  private Identifiable subject;

  private List<String> additionalPredicates;

  public IdentifiableToFileResourceRelation() {}

  public IdentifiableToFileResourceRelation(
      Identifiable subject, String predicate, FileResource object) {
    this.subject = subject;
    this.predicate = predicate;
    this.object = object;
  }

  public FileResource getObject() {
    return object;
  }

  public String getPredicate() {
    return predicate;
  }

  public Identifiable getSubject() {
    return subject;
  }

  public void setObject(FileResource objectFileResource) {
    this.object = objectFileResource;
  }

  public void setPredicate(String predicate) {
    this.predicate = predicate;
  }

  public void setSubject(Identifiable subjectEntity) {
    this.subject = subjectEntity;
  }

  public List<String> getAdditionalPredicates() {
    return additionalPredicates;
  }

  public void setAdditionalPredicates(List<String> additionalPredicates) {
    this.additionalPredicates = additionalPredicates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IdentifiableToFileResourceRelation)) {
      return false;
    }
    IdentifiableToFileResourceRelation that = (IdentifiableToFileResourceRelation) o;
    return Objects.equals(object, that.object)
        && Objects.equals(predicate, that.predicate)
        && Objects.equals(subject, that.subject)
        && Objects.equals(additionalPredicates, that.additionalPredicates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(object, predicate, subject, additionalPredicates);
  }

  @Override
  public String toString() {
    return "IdentifiableToFileResourceRelation{"
        + "object="
        + object
        + ", predicate='"
        + predicate
        + '\''
        + ", subject="
        + subject
        + ", additionalPredicates="
        + additionalPredicates
        + '}';
  }

  /**
   * @return A texual representation with subject uuid, predicate and object uuid
   */
  public String toShortenedString() {
    return String.format(
        "IdentifiableToFileResourceRelation{subject=%s, predicate='%s', object=%s}",
        subject != null ? subject.getUuid() : null,
        predicate,
        object != null ? object.getUuid() : null);
  }

  public static class Builder {

    IdentifiableToFileResourceRelation relation = new IdentifiableToFileResourceRelation();

    public IdentifiableToFileResourceRelation build() {
      return relation;
    }

    public Builder object(FileResource objectFileResource) {
      relation.setObject(objectFileResource);
      return this;
    }

    public Builder predicate(String predicate) {
      relation.setPredicate(predicate);
      return this;
    }

    public Builder subject(Identifiable subjectEntity) {
      relation.setSubject(subjectEntity);
      return this;
    }

    public Builder additionalPredicate(String additionalPredicate) {
      if (relation.additionalPredicates == null) {
        relation.additionalPredicates = new ArrayList<>(1);
      }
      relation.additionalPredicates.add(additionalPredicate);
      return this;
    }

    public Builder additionalPredicates(List<String> additionalPredicates) {
      relation.setAdditionalPredicates(additionalPredicates);
      return this;
    }
  }
}
