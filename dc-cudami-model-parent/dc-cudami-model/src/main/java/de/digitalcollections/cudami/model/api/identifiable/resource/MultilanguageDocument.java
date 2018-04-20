package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.prosemirror.model.api.Document;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * MultilanguageDocument is used for text content.
 */
public interface MultilanguageDocument extends Resource {

  /**
   * @return (multilingual) text content
   */
  Map<Locale, Document> getDocuments();

  /**
   * @param documents the (multilingual) text content
   */
  void setDocuments(Map<Locale, Document> documents);

  default void addDocument(Locale locale, Document document) {
    if (getDocuments() == null) {
      setDocuments(new HashMap<>());
    }
    getDocuments().put(locale, document);
  }
}
