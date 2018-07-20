package de.digitalcollections.cudami.model.impl.identifiable.parts;

import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.prosemirror.model.api.Document;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ParagraphImpl;
import java.util.Locale;
import java.util.Map;

public class MultilanguageDocumentImpl implements MultilanguageDocument {

  private Map<Locale, Document> documents;

  public MultilanguageDocumentImpl() {
  }

  // FIXME: use service to get empty document or move logic to javascript
  @Deprecated
  public MultilanguageDocumentImpl(Locale locale) {
    this();
    Document document = new DocumentImpl();
    document.addContentBlock(new ParagraphImpl());
    addDocument(locale, document);
  }

  @Override
  public Map<Locale, Document> getDocuments() {
    return documents;
  }

  @Override
  public void setDocuments(Map<Locale, Document> documents) {
    this.documents = documents;
  }

}
