package de.digitalcollections.model.text.contentblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A text (with optional marks for emphasizing etc.). */
public class Text extends ContentBlock {

  private List<Mark> marks;
  private String text;

  public Text() {}

  public Text(String text) {
    this.text = text;
  }

  public Text(String text, String... marks) {
    this(text);
    if (marks != null) {
      for (String markStr : marks) {
        Mark mark = new Mark(markStr);
        addMark(mark);
      }
    }
  }

  public void addMark(Mark mark) {
    if (marks == null) {
      marks = new ArrayList<>(0);
    }

    marks.add(mark);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Text)) {
      return false;
    }
    Text text1 = (Text) o;
    return Objects.equals(text, text1.text) && Objects.equals(marks, text1.marks);
  }

  public List<Mark> getMarks() {
    return marks;
  }

  public String getText() {
    return text;
  }

  @Override
  public int hashCode() {

    return Objects.hash(text, marks);
  }

  public void setMarks(List<Mark> marks) {
    this.marks = marks;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{text='"
        + text
        + "', marks="
        + marks
        + ", hashCode="
        + hashCode()
        + "}";
  }
}
