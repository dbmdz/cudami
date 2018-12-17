package de.digitalcollections.cudami.client.spring.frontend;

import com.google.common.collect.Lists;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.contentblocks.Mark;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.contentblocks.Text;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TextToHtmlTransformer {

  public String transform(Text text) {
    StringBuilder result = new StringBuilder();
    List<Mark> marks = text.getMarks();
    if (marks != null) {
      marks.forEach((mark) -> {
        String markType = mark.getType();
        if (markType.equals("link")) {
          result.append("<a");
          mark.getAttributes().entrySet().forEach((attribute) -> {
            result.append(" ");
            result.append(attribute.getKey()).append("='");
            result.append(attribute.getValue()).append("'");
          });
          result.append(">");
        } else {
          result.append("<").append(markType).append(">");
        }
      });
    }
    result.append(text.getText());
    if (marks != null) {
      Lists.reverse(marks).forEach((mark) -> {
        String markType = mark.getType();
        if (markType.equals("link")) {
          result.append("</a>");
        } else {
          result.append("</").append(markType).append(">");
        }
      });
    }
    return result.toString();
  }
}
