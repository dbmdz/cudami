package de.digitalcollections.model.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.agent.FamilyName;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Article;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Event;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.manifestation.DistributionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.manifestation.ProductionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.relation.EntityToFileResourceRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.identifiable.relation.IdentifiableToEntityRelation;
import de.digitalcollections.model.identifiable.relation.IdentifiableToFileResourceRelation;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.jackson.mixin.UniqueObjectMixIn;
import de.digitalcollections.model.jackson.mixin.geo.CoordinateLocationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifiableMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifierMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.IdentifierTypeMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.NodeMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.agent.FamilyNameMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.agent.GivenNameMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.alias.LocalizedUrlAliasesMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.alias.UrlAliasMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.ArticleMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.CollectionMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EntityMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.EventMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.HeadwordEntryMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.ProjectMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.TopicMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.WebsiteMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.agent.AgentMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.agent.CorporateBodyMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.agent.FamilyMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.agent.PersonMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.digitalobject.DigitalObjectMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.geo.location.GeoLocationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.geo.location.HumanSettlementMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.geo.location.MountainMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.item.ItemMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation.DistributionInfoMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation.ExpressionTypeMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation.ManifestationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation.ProductionInfoMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation.PublicationInfoMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.manifestation.PublisherMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.relation.EntityRelationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.relation.EntityToFileResourceRelationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.entity.work.WorkMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.relation.IdentifiableToEntityRelationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.relation.IdentifiableToFileResourceRelationMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.ApplicationFileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.AudioFileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.FileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.ImageFileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.LinkedDataFileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.TextFileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.resource.VideoFileResourceMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.semantic.SubjectMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.versioning.VersionMixIn;
import de.digitalcollections.model.jackson.mixin.identifiable.web.WebpageMixIn;
import de.digitalcollections.model.jackson.mixin.legal.LicenseMixIn;
import de.digitalcollections.model.jackson.mixin.list.ListRequestMixIn;
import de.digitalcollections.model.jackson.mixin.list.ListResponseMixIn;
import de.digitalcollections.model.jackson.mixin.list.buckets.BucketMixIn;
import de.digitalcollections.model.jackson.mixin.list.buckets.BucketObjectsRequestMixIn;
import de.digitalcollections.model.jackson.mixin.list.buckets.BucketObjectsResponseMixIn;
import de.digitalcollections.model.jackson.mixin.list.buckets.BucketsRequestMixIn;
import de.digitalcollections.model.jackson.mixin.list.buckets.BucketsResponseMixIn;
import de.digitalcollections.model.jackson.mixin.list.filtering.FilterCriteriaMixIn;
import de.digitalcollections.model.jackson.mixin.list.filtering.FilterCriterionMixIn;
import de.digitalcollections.model.jackson.mixin.list.filtering.FilteringMixIn;
import de.digitalcollections.model.jackson.mixin.list.paging.PageRequestMixIn;
import de.digitalcollections.model.jackson.mixin.list.paging.PageResponseMixIn;
import de.digitalcollections.model.jackson.mixin.list.sorting.OrderMixIn;
import de.digitalcollections.model.jackson.mixin.list.sorting.SortingMixIn;
import de.digitalcollections.model.jackson.mixin.relation.PredicateMixIn;
import de.digitalcollections.model.jackson.mixin.security.UserMixIn;
import de.digitalcollections.model.jackson.mixin.semantic.HeadwordMixIn;
import de.digitalcollections.model.jackson.mixin.semantic.TagMixIn;
import de.digitalcollections.model.jackson.mixin.text.LocalizedStructuredContentMixIn;
import de.digitalcollections.model.jackson.mixin.text.LocalizedTextMixIn;
import de.digitalcollections.model.jackson.mixin.text.StructuredContentMixIn;
import de.digitalcollections.model.jackson.mixin.text.TitleMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.BlockquoteMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.BulletListMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.CodeBlockMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.ContentBlockMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.HeadingMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.IFrameMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.ImageMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.ListItemMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.MarkMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.OrderedListMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.ParagraphMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.TableCellMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.TableHeaderMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.TableMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.TableRowMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.TextMixIn;
import de.digitalcollections.model.jackson.mixin.text.contentblock.VideoMixIn;
import de.digitalcollections.model.jackson.mixin.time.TimeValueMixIn;
import de.digitalcollections.model.jackson.mixin.view.BreadcrumbNavigationMixIn;
import de.digitalcollections.model.jackson.mixin.view.RenderingHintsMixIn;
import de.digitalcollections.model.jackson.mixin.view.RenderingHintsPreviewImageMixIn;
import de.digitalcollections.model.jackson.mixin.view.RenderingTemplateMixIn;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.list.ListRequest;
import de.digitalcollections.model.list.ListResponse;
import de.digitalcollections.model.list.buckets.Bucket;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.list.filtering.FilterCriteria;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.security.User;
import de.digitalcollections.model.semantic.Headword;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.text.contentblock.Blockquote;
import de.digitalcollections.model.text.contentblock.BulletList;
import de.digitalcollections.model.text.contentblock.CodeBlock;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Heading;
import de.digitalcollections.model.text.contentblock.IFrame;
import de.digitalcollections.model.text.contentblock.Image;
import de.digitalcollections.model.text.contentblock.ListItem;
import de.digitalcollections.model.text.contentblock.Mark;
import de.digitalcollections.model.text.contentblock.OrderedList;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Table;
import de.digitalcollections.model.text.contentblock.TableCell;
import de.digitalcollections.model.text.contentblock.TableHeader;
import de.digitalcollections.model.text.contentblock.TableRow;
import de.digitalcollections.model.text.contentblock.Text;
import de.digitalcollections.model.text.contentblock.Video;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.RenderingHints;
import de.digitalcollections.model.view.RenderingHintsPreviewImage;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.Locale;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

public class DigitalCollectionsModelModule extends SimpleModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalCollectionsModelModule.class);

  public DigitalCollectionsModelModule() {
    super();

    // Just use Locale's toLanguageTag and forLanguageTag for serializing/deserializing it
    addSerializer(new StdDelegatingSerializer(Locale.class, toString(Locale::toLanguageTag)));
    addKeySerializer(Locale.class, new LocaleKeySerializer());
    addDeserializer(
        Locale.class, new StdDelegatingDeserializer<>(fromString(Locale::forLanguageTag)));
    addKeyDeserializer(Locale.class, new LocaleKeyDeserializer());

    // Just use MimeType's getTypeName and String constructor for serializing/deserializing it
    addSerializer(new StdDelegatingSerializer(MimeType.class, toString(MimeType::getTypeName)));
    addDeserializer(
        MimeType.class, new StdDelegatingDeserializer<>(fromString(MimeType::fromTypename)));
  }

  @Override
  public String getModuleName() {
    return "Cudami DigitalCollections Model jackson module";
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);

    LOGGER.info("Using Cudami DigitalCollectionsModelModule");

    // agent
    context.setMixInAnnotations(Agent.class, AgentMixIn.class);
    context.setMixInAnnotations(CorporateBody.class, CorporateBodyMixIn.class);
    context.setMixInAnnotations(Family.class, FamilyMixIn.class);
    context.setMixInAnnotations(FamilyName.class, FamilyNameMixIn.class);
    context.setMixInAnnotations(GivenName.class, GivenNameMixIn.class);
    context.setMixInAnnotations(Person.class, PersonMixIn.class);

    // geo
    context.setMixInAnnotations(CoordinateLocation.class, CoordinateLocationMixIn.class);
    context.setMixInAnnotations(GeoLocation.class, GeoLocationMixIn.class);
    context.setMixInAnnotations(HumanSettlement.class, HumanSettlementMixIn.class);
    context.setMixInAnnotations(Mountain.class, MountainMixIn.class);

    // work
    context.setMixInAnnotations(Work.class, WorkMixIn.class);

    // manifestation and expression
    context.setMixInAnnotations(DistributionInfo.class, DistributionInfoMixIn.class);
    context.setMixInAnnotations(ExpressionType.class, ExpressionTypeMixIn.class);
    context.setMixInAnnotations(Manifestation.class, ManifestationMixIn.class);
    context.setMixInAnnotations(ProductionInfo.class, ProductionInfoMixIn.class);
    context.setMixInAnnotations(Publisher.class, PublisherMixIn.class);
    context.setMixInAnnotations(PublicationInfo.class, PublicationInfoMixIn.class);
    context.setMixInAnnotations(DistributionInfo.class, DistributionInfoMixIn.class);

    // item
    context.setMixInAnnotations(Item.class, ItemMixIn.class);

    // semantic
    context.setMixInAnnotations(Headword.class, HeadwordMixIn.class);
    context.setMixInAnnotations(HeadwordEntry.class, HeadwordEntryMixIn.class);
    context.setMixInAnnotations(Subject.class, SubjectMixIn.class);
    context.setMixInAnnotations(Tag.class, TagMixIn.class);

    // other
    context.setMixInAnnotations(ApplicationFileResource.class, ApplicationFileResourceMixIn.class);
    context.setMixInAnnotations(Article.class, ArticleMixIn.class);
    context.setMixInAnnotations(AudioFileResource.class, AudioFileResourceMixIn.class);
    context.setMixInAnnotations(Blockquote.class, BlockquoteMixIn.class);
    context.setMixInAnnotations(BreadcrumbNavigation.class, BreadcrumbNavigationMixIn.class);
    context.setMixInAnnotations(Bucket.class, BucketMixIn.class);
    context.setMixInAnnotations(BucketObjectsRequest.class, BucketObjectsRequestMixIn.class);
    context.setMixInAnnotations(BucketObjectsResponse.class, BucketObjectsResponseMixIn.class);
    context.setMixInAnnotations(BucketsRequest.class, BucketsRequestMixIn.class);
    context.setMixInAnnotations(BucketsResponse.class, BucketsResponseMixIn.class);
    context.setMixInAnnotations(BulletList.class, BulletListMixIn.class);
    context.setMixInAnnotations(CodeBlock.class, CodeBlockMixIn.class);
    context.setMixInAnnotations(Collection.class, CollectionMixIn.class);
    context.setMixInAnnotations(ContentBlock.class, ContentBlockMixIn.class);
    context.setMixInAnnotations(DigitalObject.class, DigitalObjectMixIn.class);
    context.setMixInAnnotations(Entity.class, EntityMixIn.class);
    context.setMixInAnnotations(EntityRelation.class, EntityRelationMixIn.class);
    context.setMixInAnnotations(
        EntityToFileResourceRelation.class, EntityToFileResourceRelationMixIn.class);
    context.setMixInAnnotations(Event.class, EventMixIn.class);
    context.setMixInAnnotations(FileResource.class, FileResourceMixIn.class);
    context.setMixInAnnotations(FilterCriteria.class, FilterCriteriaMixIn.class);
    context.setMixInAnnotations(FilterCriterion.class, FilterCriterionMixIn.class);
    context.setMixInAnnotations(Filtering.class, FilteringMixIn.class);
    context.setMixInAnnotations(Heading.class, HeadingMixIn.class);
    context.setMixInAnnotations(Identifiable.class, IdentifiableMixIn.class);
    context.setMixInAnnotations(
        IdentifiableToEntityRelation.class, IdentifiableToEntityRelationMixIn.class);
    context.setMixInAnnotations(
        IdentifiableToFileResourceRelation.class, IdentifiableToFileResourceRelationMixIn.class);
    context.setMixInAnnotations(Identifier.class, IdentifierMixIn.class);
    context.setMixInAnnotations(IdentifierType.class, IdentifierTypeMixIn.class);
    context.setMixInAnnotations(IFrame.class, IFrameMixIn.class);
    context.setMixInAnnotations(Image.class, ImageMixIn.class);
    context.setMixInAnnotations(ImageFileResource.class, ImageFileResourceMixIn.class);
    context.setMixInAnnotations(License.class, LicenseMixIn.class);
    context.setMixInAnnotations(LinkedDataFileResource.class, LinkedDataFileResourceMixIn.class);
    context.setMixInAnnotations(ListItem.class, ListItemMixIn.class);
    context.setMixInAnnotations(ListRequest.class, ListRequestMixIn.class);
    context.setMixInAnnotations(ListResponse.class, ListResponseMixIn.class);
    context.setMixInAnnotations(
        LocalizedStructuredContent.class, LocalizedStructuredContentMixIn.class);
    context.setMixInAnnotations(LocalizedText.class, LocalizedTextMixIn.class);
    context.setMixInAnnotations(LocalizedUrlAliases.class, LocalizedUrlAliasesMixIn.class);
    context.setMixInAnnotations(Mark.class, MarkMixIn.class);
    context.setMixInAnnotations(Node.class, NodeMixIn.class);
    context.setMixInAnnotations(Order.class, OrderMixIn.class);
    context.setMixInAnnotations(OrderedList.class, OrderedListMixIn.class);
    context.setMixInAnnotations(PageRequest.class, PageRequestMixIn.class);
    context.setMixInAnnotations(PageResponse.class, PageResponseMixIn.class);
    context.setMixInAnnotations(Paragraph.class, ParagraphMixIn.class);
    context.setMixInAnnotations(Predicate.class, PredicateMixIn.class);
    context.setMixInAnnotations(Project.class, ProjectMixIn.class);
    context.setMixInAnnotations(RenderingHints.class, RenderingHintsMixIn.class);
    context.setMixInAnnotations(
        RenderingHintsPreviewImage.class, RenderingHintsPreviewImageMixIn.class);
    context.setMixInAnnotations(RenderingTemplate.class, RenderingTemplateMixIn.class);
    context.setMixInAnnotations(Sorting.class, SortingMixIn.class);
    context.setMixInAnnotations(StructuredContent.class, StructuredContentMixIn.class);
    context.setMixInAnnotations(Table.class, TableMixIn.class);
    context.setMixInAnnotations(TableCell.class, TableCellMixIn.class);
    context.setMixInAnnotations(TableHeader.class, TableHeaderMixIn.class);
    context.setMixInAnnotations(TableRow.class, TableRowMixIn.class);
    context.setMixInAnnotations(Text.class, TextMixIn.class);
    context.setMixInAnnotations(TextFileResource.class, TextFileResourceMixIn.class);
    context.setMixInAnnotations(TimeValue.class, TimeValueMixIn.class);
    context.setMixInAnnotations(Topic.class, TopicMixIn.class);
    context.setMixInAnnotations(UniqueObject.class, UniqueObjectMixIn.class);
    context.setMixInAnnotations(UrlAlias.class, UrlAliasMixIn.class);
    context.setMixInAnnotations(User.class, UserMixIn.class);
    context.setMixInAnnotations(Version.class, VersionMixIn.class);
    context.setMixInAnnotations(Video.class, VideoMixIn.class);
    context.setMixInAnnotations(VideoFileResource.class, VideoFileResourceMixIn.class);
    context.setMixInAnnotations(Webpage.class, WebpageMixIn.class);
    context.setMixInAnnotations(Website.class, WebsiteMixIn.class);
    context.setMixInAnnotations(Title.class, TitleMixIn.class);
  }

  /** Helper function to create Converter from lambda * */
  private <T> Converter<String, T> fromString(Function<String, ? extends T> fun) {
    return new StdConverter<String, T>() {
      @Override
      public T convert(String value) {
        return fun.apply(value);
      }
    };
  }

  /** Helper function to create Converter from lambda * */
  private <T> Converter<T, String> toString(Function<T, String> fun) {
    return new StdConverter<T, String>() {
      @Override
      public String convert(T value) {
        return fun.apply(value);
      }
    };
  }
}
