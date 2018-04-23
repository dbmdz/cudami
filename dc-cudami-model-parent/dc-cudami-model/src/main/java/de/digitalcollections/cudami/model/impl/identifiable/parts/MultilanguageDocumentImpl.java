package de.digitalcollections.cudami.model.impl.identifiable.parts;

import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.prosemirror.model.api.Document;
import java.util.Locale;
import java.util.Map;

public class MultilanguageDocumentImpl implements MultilanguageDocument {

  private Map<Locale, Document> documents;

  @Override
  public Map<Locale, Document> getDocuments() {
    return documents;
  }

  @Override
  public void setDocuments(Map<Locale, Document> documents) {
    this.documents = documents;
  }

}
