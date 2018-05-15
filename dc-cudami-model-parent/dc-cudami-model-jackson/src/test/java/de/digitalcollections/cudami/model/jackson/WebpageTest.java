package de.digitalcollections.cudami.model.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.parts.MultilanguageDocumentImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.prosemirror.model.api.ContentBlock;
import de.digitalcollections.prosemirror.model.api.Document;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HardBreakImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HeadingImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ParagraphImpl;
import de.digitalcollections.prosemirror.model.jackson.ProseMirrorObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WebpageTest extends BaseSerializationTest {

  ObjectMapper mapper;

  @BeforeEach
  public void beforeEach() {
    mapper = new ObjectMapper();
    mapper.registerModule(new CudamiModule());
    ProseMirrorObjectMapper.customize(mapper);
  }

  @Override
  protected ObjectMapper getMapper() {
    return mapper;
  }

  @Test
  public void testSerialisationInBothWays() throws Exception {
    Webpage webpage = constructWebpage();
    checkSerializeDeserialize(webpage);
  }

  // -------------------------------------------------------------------
  private Webpage constructWebpage() {
    List<ContentBlock> contents = new ArrayList<>();

    contents.add(new HeadingImpl(3, "Impressum"));
    contents.add(new HeadingImpl(4, "Bayerische Staatsbibliothek"));
    contents.add(new ParagraphImpl("Ludwigstraße 16"));
    contents.add(new HardBreakImpl());
    contents.add(new HeadingImpl(4, "Gesetzlicher Vertreter:"));
    contents.add(new ParagraphImpl("Generaldirektor Dr. Klaus Ceynowa"));

    Document document = new DocumentImpl();
    document.setContentBlocks(contents);

    Webpage webpage = new WebpageImpl();
    webpage.setLabel(new de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl(Locale.GERMANY, ""));

    MultilanguageDocument mld = new MultilanguageDocumentImpl();
    mld.addDocument(Locale.GERMAN, document);
    webpage.setText(mld);
    return webpage;
  }

}
