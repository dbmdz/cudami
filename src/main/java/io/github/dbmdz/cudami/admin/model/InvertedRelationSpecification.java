package io.github.dbmdz.cudami.admin.model;

import de.digitalcollections.model.UniqueObject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;

/**
 * While the RelationSpecification of dc-model is object-centric, this inverted
 * RelationSpecification is subject-centric.
 */
@SuppressFBWarnings
public class InvertedRelationSpecification<U extends UniqueObject> {

  private String title;

  private String sortKey;
  private U object;

  public InvertedRelationSpecification() {}

  public InvertedRelationSpecification(String title, String sortKey, U object) {
    this.title = title;
    this.sortKey = sortKey;
    this.object = object;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSortKey() {
    return sortKey;
  }

  public void setSortKey(String sortKey) {
    this.sortKey = sortKey;
  }

  public U getObject() {
    return object;
  }

  public void setObject(U object) {
    this.object = object;
  }

  @Override
  public String toString() {
    return "InvertedRelationSpecification{"
        + "title='"
        + title
        + '\''
        + ", sortKey='"
        + sortKey
        + '\''
        + ", object="
        + object
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InvertedRelationSpecification<?> that)) {
      return false;
    }
    return Objects.equals(title, that.title)
        && Objects.equals(sortKey, that.sortKey)
        && Objects.equals(object.getUuid(), that.object.getUuid());
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, sortKey, object.getUuid());
  }
}
