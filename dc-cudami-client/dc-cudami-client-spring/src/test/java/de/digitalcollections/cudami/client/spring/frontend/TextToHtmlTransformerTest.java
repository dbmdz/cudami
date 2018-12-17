package de.digitalcollections.cudami.client.spring.frontend;

import de.digitalcollections.model.api.identifiable.parts.structuredcontent.contentblocks.Mark;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.contentblocks.MarkImpl;
import de.digitalcollections.model.impl.identifiable.parts.structuredcontent.contentblocks.TextImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration()
public class TextToHtmlTransformerTest {

  private static TextToHtmlTransformer transformer;

  @BeforeAll
  public static void setUpClass() {
    transformer = new TextToHtmlTransformer();
  }

  @Test
  public void testTransformText() {
    String text = "This is a little text.";

    TextImpl textWithMarks = new TextImpl(text);
    String result = transformer.transform(textWithMarks);
    String expResult = "This is a little text.";
    assertThat(result).isEqualTo(expResult);

    textWithMarks = new TextImpl(text, "code");
    result = transformer.transform(textWithMarks);
    expResult = "<code>This is a little text.</code>";
    assertThat(result).isEqualTo(expResult);

    textWithMarks = new TextImpl(text, "code", "em", "strong");
    result = transformer.transform(textWithMarks);
    expResult = "<code><em><strong>This is a little text.</strong></em></code>";
    assertThat(result).isEqualTo(expResult);

    textWithMarks = new TextImpl(text, "strong", "em", "code");
    result = transformer.transform(textWithMarks);
    expResult = "<strong><em><code>This is a little text.</code></em></strong>";
    assertThat(result).isEqualTo(expResult);
  }

  @Test
  public void testTransformTextWithLink() {
    Mark mark = new MarkImpl("link");
    mark.addAttribute("href", "https://foobar.com");
    mark.addAttribute("target", "_blank");
    mark.addAttribute("title", "Foobar's website");
    TextImpl text = new TextImpl("I am a wonderful link.");
    text.addMark(mark);
    String result = transformer.transform(text);
    String expResult = "<a href='https://foobar.com' title='Foobar's website' target='_blank'>I am a wonderful link.</a>";
    assertThat(result).isEqualTo(expResult);
  }
}
