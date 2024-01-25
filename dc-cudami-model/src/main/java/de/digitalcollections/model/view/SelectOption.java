package de.digitalcollections.model.view;

/** A List item with an identifier, a title, a sub title, a link and a flag for being selected */
public class SelectOption {

  private String id;
  private String link;
  private boolean selected;
  private String subTitle;
  private String title;

  public SelectOption(String id, String title) {
    this.id = id;
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public String getLink() {
    return link;
  }

  public String getSubTitle() {
    return subTitle;
  }

  public String getTitle() {
    return title;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public void setSubTitle(String subTitle) {
    this.subTitle = subTitle;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "{"
        + "id='"
        + id
        + '\''
        + ", title='"
        + title
        + '\''
        + ", subTitle='"
        + subTitle
        + '\''
        + ", link='"
        + link
        + '\''
        + ", selected="
        + selected
        + '}';
  }
}
