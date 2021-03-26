package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.text.contentblock.HardBreak;
import de.digitalcollections.model.text.contentblock.Heading;
import de.digitalcollections.model.text.contentblock.Mark;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;

public class ParagraphBuilder {

  Paragraph paragraph = new Paragraph();

  public ParagraphBuilder addHeading(int level, String text, String... marks) {
    paragraph.addContentBlock(new Heading(level, text));
    return this;
  }

  public ParagraphBuilder addText(String text) {
    paragraph.addContentBlock(new Text(text));
    return this;
  }

  public ParagraphBuilder addText(String text, String... marks) {
    paragraph.addContentBlock(new Text(text, marks));
    return this;
  }

  public ParagraphBuilder addHardBreak() {
    paragraph.addContentBlock(new HardBreak());
    return this;
  }

  public ParagraphBuilder addLink(String text, String href) {
    Text linkText = new Text(text);
    Mark mark = new Mark("link");
    mark.addAttribute("href", href);
    linkText.addMark(mark);
    paragraph.addContentBlock(linkText);
    return this;
  }

  public ParagraphBuilder addLinkWithTitle(String text, String href, String title) {
    Text linkText = new Text(text);
    Mark mark = new Mark("link");
    mark.addAttribute("href", href);
    mark.addAttribute("title", title);
    linkText.addMark(mark);
    paragraph.addContentBlock(linkText);
    return this;
  }

  public Paragraph build() {
    return paragraph;
  }
}
