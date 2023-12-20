package de.digitalcollections.model.text.contentblock;

/** An iframe element. */
public class IFrame extends ContentBlockWithAttributes {

  public IFrame() {}

  public IFrame(String src, String width, String height, String title) {
    super();
    addAttribute("height", height);
    addAttribute("src", src);
    addAttribute("title", title);
    addAttribute("width", width);
  }
}
