package de.digitalcollections.model.mappings.html;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.ContentBlockNode;
import de.digitalcollections.model.text.contentblock.ContentBlockNodeWithAttributes;
import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

public class HtmlMapperTest {

  @Test
  public void testHtml2StructuredContentHierarchicalUnorderedList() {
    String html =
        "<ul>"
            + "<li>item 1"
            + "  <ul>"
            + "  <li>item 1.1</li>"
            + "  <li>item 1.2</li>"
            + "  </ul>"
            + "</li>"
            + "<li>item 2</li>"
            + "</ul>";

    StructuredContent sc = HtmlMapper.toStructuredContent(html);
    assertThat(sc.getContentBlocks().size()).isEqualTo(1);
    // TODO better assert
  }

  @Test
  public void testHtml2StructuredContentSimpleUnorderedList() {
    String html = "<ul><li>item 1</li><li>item 2</li></ul>";

    StructuredContent sc = HtmlMapper.toStructuredContent(html);
    assertThat(sc.getContentBlocks().size()).isEqualTo(1);
    // TODO better assert
  }

  @Test
  public void testHtml2StructuredContentSimpleUnorderedListWithLinks() {
    String html =
        "<ul><li>item 1</li><li>Test <a href=\"https://mdz-nbn-resolving.de/view:bsb00009405?page=297\">Erscheinungsjahr: 1956, aufgeführt in ZBLG 22 (1959), Nr.2082</a> Test</li></ul>";

    StructuredContent sc = HtmlMapper.toStructuredContent(html);
    assertThat(sc.getContentBlocks().size()).isEqualTo(1);
    // TODO better assert
  }

  @Test
  public void testHtml2StructuredContentTable() {
    String html =
        "<html><body><table><tr><td colspan=\"1\" data-foo=\"bar\">1.1</td><td colspan=\"2\">1.2</td></tr><tr><td>1.1</td><td>1.2</td></tr></table></body></html>";

    StructuredContent sc = HtmlMapper.toStructuredContent(html, Parser.xmlParser());
    List<ContentBlock> tables = sc.getContentBlocks();
    assertThat(tables.size()).isEqualTo(1);
    List<ContentBlock> tableRows = ((ContentBlockNode) tables.get(0)).getContentBlocks();
    assertThat(tableRows.size()).isEqualTo(2);
    List<ContentBlock> tableCells = ((ContentBlockNode) tableRows.get(0)).getContentBlocks();
    assertThat(tableCells.size()).isEqualTo(2);
    ContentBlockNodeWithAttributes firstTableCell =
        (ContentBlockNodeWithAttributes) tableCells.get(0);
    assertThat(firstTableCell.getAttribute("colspan")).isEqualTo(1);
    assertThat(firstTableCell.getAttribute("rowspan")).isNull();
    assertThat(firstTableCell.getAttribute("colwidth")).isNull();
    assertThat(firstTableCell.getAttribute("data-foo")).isEqualTo("bar");
  }

  @Test
  public void testGetContentBlockForUnsupportedElement() {
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(
            () -> {
              HtmlMapper.getContentBlock(new Element("blabla"));
            });
  }
}
