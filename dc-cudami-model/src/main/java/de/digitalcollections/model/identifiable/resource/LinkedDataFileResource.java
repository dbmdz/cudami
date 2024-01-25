package de.digitalcollections.model.identifiable.resource;

import java.net.URI;
import java.util.Objects;
import lombok.experimental.SuperBuilder;

/**
 * This class represents a linked data file resource, referenced through an uri. It may be specified
 * additionally (if applicable) by the context definition and the corresponding (object) type. <br>
 * Examples of LinkedDataFileResources are machine readable resources like Marc, MARC-XML, RDF,
 * IIIF-Manifest, METS, JSON-LD, .... <br>
 * A linked data file resource may be for example of format JSON-LD (mimetype
 * 'application/ld+json'), e.g. an IIIF Presentation Manifest or an Entity Facts person description,
 * what is the first use case being in focus.<br>
 * <br>
 * JSON-LD is designed around the concept of a "context" to provide additional mappings from JSON to
 * an RDF model.<br>
 * <br>
 * The 'context' links object properties in a JSON document to concepts in an ontology.<br>
 * The 'id' is an Internationalized Resource Identifier, represented by an IRI, which allows the
 * Linked Data FileResource to be unambiguously identified by an IRI. In our implementation we use
 * the 'uri' field of FileResource to be the id.<br>
 * The 'type' specifies the type of object described in the document.<br>
 * <br>
 * The 'context' and 'objectType' are optional if no context or objectType is known. <br>
 * Further informations:
 *
 * <ul>
 *   <li><a href="https://en.wikipedia.org/wiki/Linked_data">Linked Data</a>
 *   <li><a href="https://en.wikipedia.org/wiki/JSON-LD">JSON-LD</a>
 *   <li><a
 *       href="https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier">Internationalized
 *       Resource Identifier (IRI)</a>
 *   <li><a href="https://iiif.io/api/presentation/2.1/">IIIF Presentation API</a>
 *   <li><a
 *       href="https://www.culturegraph.org/DE/Service/DigitaleDienste/EntityFacts/entityfacts_node.html">Entity
 *       Facts der Deutschen Nationalbibliothek (DNB)</a>
 * </ul>
 */
@SuperBuilder(buildMethodName = "prebuild")
public class LinkedDataFileResource extends FileResource {

  private URI context;
  private String objectType;

  public LinkedDataFileResource() {
    super();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LinkedDataFileResource)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    LinkedDataFileResource that = (LinkedDataFileResource) o;
    return Objects.equals(context, that.context) && Objects.equals(objectType, that.objectType);
  }

  /**
   * @return the linked data context. The 'context' links object properties in a JSON document to
   *     concepts in an ontology.
   */
  public URI getContext() {
    return context;
  }

  /**
   * @return the object type described in this document
   */
  public String getObjectType() {
    return objectType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), context, objectType);
  }

  @Override
  protected void init() {
    super.init();
    this.fileResourceType = FileResourceType.LINKED_DATA;
  }

  /**
   * @param context set the linked data context to given context
   */
  public void setContext(URI context) {
    this.context = context;
  }

  /**
   * @param objectType set the object type described in this document
   */
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public abstract static class LinkedDataFileResourceBuilder<
          C extends LinkedDataFileResource, B extends LinkedDataFileResourceBuilder<C, B>>
      extends FileResourceBuilder<C, B> {

    @Override
    public C build() {
      C c = prebuild();
      c.init();
      setInternalReferences(c);
      return c;
    }

    public B context(String context) {
      this.context = URI.create(context);
      return self();
    }

    @Override
    public B uri(String uri) {
      return this.uri(URI.create(uri));
    }
  }
}
