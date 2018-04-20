package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.prosemirror.model.api.Document;
import java.util.Locale;
import java.util.Map;
import de.digitalcollections.cudami.model.api.identifiable.resource.MultilanguageDocument;

public class MultilanguageDocumentImpl extends ResourceImpl implements MultilanguageDocument {

  private Map<Locale, Document> documents;

  public MultilanguageDocumentImpl() {
    super();
    this.resourceType = ResourceType.MULTILANGUAGE_DOCUMENT;
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
