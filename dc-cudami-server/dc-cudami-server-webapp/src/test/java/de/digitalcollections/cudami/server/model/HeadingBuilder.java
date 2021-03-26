package de.digitalcollections.cudami.server.model;

import de.digitalcollections.model.text.contentblock.Heading;
import de.digitalcollections.model.text.contentblock.Text;

public class HeadingBuilder {

  Heading heading = new Heading();

  public Heading build() {
    return heading;
  }

  public HeadingBuilder setLevel(int level) {
    heading.addAttribute("level", level);
    return this;
  }

  public HeadingBuilder setText(String text, String... marks) {
    heading.addContentBlock(new Text(text, marks));
    return this;
  }
}
