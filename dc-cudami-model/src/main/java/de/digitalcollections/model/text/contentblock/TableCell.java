package de.digitalcollections.model.text.contentblock;

/** A table cell of a table. */
public class TableCell extends ContentBlockNodeWithAttributes {

  public TableCell() {}

  public TableCell(String colspan, String rowspan, String colwidth) {
    super();
    addAttribute("colspan", colspan);
    addAttribute("rowspan", rowspan);
    addAttribute("colwidth", colwidth);
  }
}
