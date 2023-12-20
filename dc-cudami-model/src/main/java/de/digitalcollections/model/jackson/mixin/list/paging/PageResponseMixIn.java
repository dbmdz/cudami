package de.digitalcollections.model.jackson.mixin.list.paging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.security.User;
import de.digitalcollections.model.semantic.Headword;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;

@JsonDeserialize(as = PageResponse.class)
public abstract class PageResponseMixIn<T> extends PageResponse<T> {

  @JsonTypeInfo(use = Id.NAME, property = "objectType", visible = true)
  @JsonSubTypes({
    @Type(value = Article.class, name = "ARTICLE"),
    @Type(value = Collection.class, name = "COLLECTION"),
    @Type(value = CorporateBody.class, name = "CORPORATE_BODY"),
    @Type(value = DigitalObject.class, name = "DIGITAL_OBJECT"),
    @Type(value = Entity.class, name = "ENTITY"),
    @Type(value = EntityRelation.class, name = "ENTITY_RELATION"),
    @Type(value = FamilyName.class, name = "FAMILY_NAME"),
    @Type(value = FileResource.class, name = "FILE_RESOURCE"),
    @Type(value = GeoLocation.class, name = "GEO_LOCATION"),
    @Type(value = GivenName.class, name = "GIVEN_NAME"),
    @Type(value = Headword.class, name = "HEADWORD"),
    @Type(value = HeadwordEntry.class, name = "HEADWORD_ENTRY"),
    @Type(value = HumanSettlement.class, name = "HUMAN_SETTLEMENT"),
    @Type(value = Identifiable.class, name = "IDENTIFIABLE"),
    @Type(value = IdentifierType.class, name = "IDENTIFIER_TYPE"),
    @Type(value = Item.class, name = "ITEM"),
    @Type(value = License.class, name = "LICENSE"),
    @Type(value = Person.class, name = "PERSON"),
    @Type(value = Predicate.class, name = "PREDICATE"),
    @Type(value = Project.class, name = "PROJECT"),
    @Type(value = Publisher.class, name = "PUBLISHER"),
    @Type(value = RenderingTemplate.class, name = "RENDERING_TEMPLATE"),
    @Type(value = Subject.class, name = "SUBJECT"),
    @Type(value = Tag.class, name = "TAG"),
    @Type(value = Topic.class, name = "TOPIC"),
    @Type(value = User.class, name = "USER"),
    @Type(value = Webpage.class, name = "WEBPAGE"),
    @Type(value = Website.class, name = "WEBSITE"),
    @Type(value = Work.class, name = "WORK")
  })
  @Override
  public abstract List<T> getContent();

  @JsonIgnore
  @Override
  public abstract int getPageNumber();

  @JsonIgnore
  @Override
  public abstract int getNumberOfElements();

  @JsonProperty(value = "pageRequest")
  @Override
  public abstract PageRequest getRequest();

  @JsonIgnore
  @Override
  public abstract int getSize();

  @JsonIgnore
  @Override
  public abstract Sorting getSorting();

  @JsonIgnore
  @Override
  public abstract int getTotalPages();

  @JsonIgnore
  @Override
  public abstract boolean isFirst();

  @JsonIgnore
  @Override
  public abstract boolean isLast();
}
