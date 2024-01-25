package de.digitalcollections.model.text.contentblock;

/** A paragraph (can contain text and other content blocks). */
public class Paragraph extends ContentBlockNode {

  public static Builder builder() {
    return new Builder();
  }

  public Paragraph() {}

  public Paragraph(String text) {
    addContentBlock(new Text(text));
  }

  public static class Builder {

    Paragraph paragraph = new Paragraph();

    public Builder addHardBreak() {
      paragraph.addContentBlock(new HardBreak());
      return this;
    }

    public Builder addHeading(int level, String text, String... marks) {
      paragraph.addContentBlock(new Heading(level, text));
      return this;
    }

    public Builder addLink(String text, String href) {
      Text linkText = new Text(text);
      Mark mark = new Mark("link");
      mark.addAttribute("href", href);
      linkText.addMark(mark);
      paragraph.addContentBlock(linkText);
      return this;
    }

    public Builder addLinkWithTitle(String text, String href, String title) {
      Text linkText = new Text(text);
      Mark mark = new Mark("link");
      mark.addAttribute("href", href);
      mark.addAttribute("title", title);
      linkText.addMark(mark);
      paragraph.addContentBlock(linkText);
      return this;
    }

    public Builder addText(String text) {
      paragraph.addContentBlock(new Text(text));
      return this;
    }

    public Builder addText(String text, String... marks) {
      paragraph.addContentBlock(new Text(text, marks));
      return this;
    }

    public Paragraph build() {
      return paragraph;
    }
  }
}
