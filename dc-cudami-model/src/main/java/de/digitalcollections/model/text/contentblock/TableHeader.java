package de.digitalcollections.model.text.contentblock;

/** A table header of a table. */
public class TableHeader extends ContentBlockNodeWithAttributes {

  public TableHeader() {}

  public TableHeader(String colspan, String rowspan, String colwidth) {
    super();
    addAttribute("colspan", colspan);
    addAttribute("rowspan", rowspan);
    addAttribute("colwidth", colwidth);
  }
}
