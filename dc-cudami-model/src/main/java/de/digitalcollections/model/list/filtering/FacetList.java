package de.digitalcollections.model.list.filtering;

import java.util.LinkedList;
import java.util.List;

public class FacetList {

  List<FacetListItem> items;
  String title;

  public FacetList() {
    init();
  }

  public void addItem(FacetListItem item) {
    items.add(item);
  }

  public List<FacetListItem> getItems() {
    return items;
  }

  public String getTitle() {
    return title;
  }

  protected void init() {
    if (items == null) {
      this.items = new LinkedList<>();
    }
  }

  public void setItems(List<FacetListItem> items) {
    if (items != null) {
      this.items = items;
    }
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "{title='" + title + "', items=" + items + "}";
  }
}
