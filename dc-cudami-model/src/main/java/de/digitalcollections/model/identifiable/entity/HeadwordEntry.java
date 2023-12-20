package de.digitalcollections.model.identifiable.entity;

import de.digitalcollections.model.semantic.Headword;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * The textual body to a specified headword (encyclopedia) or lemma (dictionary).
 *
 * <p>"A headword, lemma, or catchword is the word under which a set of related dictionary or
 * encyclopaedia entries appears. The headword is used to locate the entry, and dictates its
 * alphabetical position. Depending on the size and nature of the dictionary or encyclopedia, the
 * entry may include alternative meanings of the word, its etymology, pronunciation and inflections,
 * compound words or phrases that contain the headword, and encyclopedic information about the
 * concepts represented by the word." (Wikipedia, https://en.wikipedia.org/wiki/Headword)
 *
 * @see Headword
 */
@SuperBuilder(buildMethodName = "prebuild")
public class HeadwordEntry extends Article {

  private Headword headword;

  public HeadwordEntry() {
    super();
  }

  public HeadwordEntry(Headword headword) {
    this();
    this.headword = headword;
  }

  public Headword getHeadword() {
    return headword;
  }

  @Override
  protected void init() {
    super.init();
  }

  public void setHeadword(Headword headword) {
    this.headword = headword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HeadwordEntry)) return false;
    if (!super.equals(o)) return false;
    HeadwordEntry that = (HeadwordEntry) o;
    return Objects.equals(headword, that.headword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), headword);
  }

  public abstract static class HeadwordEntryBuilder<
          C extends HeadwordEntry, B extends HeadwordEntryBuilder<C, B>>
      extends ArticleBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      return c;
    }
  }
}
