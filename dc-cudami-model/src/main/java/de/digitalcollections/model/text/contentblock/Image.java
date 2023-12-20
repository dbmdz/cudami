package de.digitalcollections.model.text.contentblock;

/** An image with attributes. */
public class Image extends ContentBlockWithAttributes {

  public Image() {}

  public Image(
      String alignment,
      String altText,
      String caption,
      boolean linkNewTab,
      String linkUrl,
      String resourceId,
      String title,
      String url,
      String width) {
    super();
    addAttribute("alignment", alignment);
    addAttribute("altText", altText);
    addAttribute("caption", caption);
    addAttribute("linkNewTab", linkNewTab);
    addAttribute("linkUrl", linkUrl);
    addAttribute("resourceId", resourceId);
    addAttribute("title", title);
    addAttribute("url", url);
    addAttribute("width", width);
  }
}
