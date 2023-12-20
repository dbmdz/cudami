package de.digitalcollections.model.jackson.identifiable.entity.manifestation;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.jackson.BaseJsonSerializationTest;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.text.TitleType;
import de.digitalcollections.model.text.contentblock.ContentBlock;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.time.TimeValue;
import de.digitalcollections.model.time.TimeValueRange;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The Manifestation")
public class ManifestationTest extends BaseJsonSerializationTest {

  private LocalizedStructuredContent createNote(String noteText) {
    LocalizedStructuredContent localizedStructuredContent = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    ContentBlock contentBlock = new Paragraph(noteText);
    structuredContent.addContentBlock(contentBlock);
    localizedStructuredContent.put(Locale.GERMAN, structuredContent);

    return localizedStructuredContent;
  }

  private Manifestation createObject() {
    // Later, because series extends work
    UUID parentUUID = UUID.fromString("8a9c3c34-c36c-4671-8f2f-9d96a5fc32e4");
    Manifestation parentManifestation =
        Manifestation.builder().uuid(parentUUID).label("Parent").build();

    RelationSpecification<Manifestation> parent =
        RelationSpecification.<Manifestation>builder()
            .title("Parent")
            .sortKey("sortme")
            .subject(parentManifestation)
            .build();

    Manifestation manifestation =
        Manifestation.builder()
            .manifestationType("SINGLE")
            .identifier(Identifier.builder().namespace("foo").id("bar").build())
            .label(Locale.GERMAN, "Zimmer-Gymnastik ohne Geräte")
            .composition("1 Partitur (11 Seiten)")
            .dimensions("26,5 x 70 x 2 cm")
            .scale("[Ca. 1:820 000]")
            .note(createNote("Plattendruck"))
            .note(createNote("Pr. 54 kr"))
            .version("2. Auflage")
            .relations(
                List.of(
                    EntityRelation.builder()
                        .subject(Person.builder().label(Locale.GERMAN, "Arnold Hiller").build())
                        .predicate("is_author_of")
                        .build()))
            .publicationInfo(
                PublicationInfo.builder()
                    .datePresentation("2020")
                    .navDateRange(
                        new LocalDateRange(
                            LocalDate.parse("2020-04-28"), LocalDate.parse("2020-04-28")))
                    .timeValueRange(
                        new TimeValueRange(
                            new TimeValue(
                                2020,
                                0,
                                0,
                                0,
                                0,
                                0,
                                TimeValue.PREC_YEAR,
                                0,
                                0,
                                0,
                                TimeValue.CM_GREGORIAN_PRO),
                            new TimeValue(
                                2020,
                                0,
                                0,
                                0,
                                0,
                                0,
                                TimeValue.PREC_YEAR,
                                0,
                                0,
                                0,
                                TimeValue.CM_GREGORIAN_PRO)))
                    .publishers(
                        List.of(
                            buildPublisher("Karl Ranseier", List.of("Köln")),
                            buildPublisher("Hans Dampf", List.of("Frankfurt", "München")),
                            buildPublisher(null, List.of("München", "Berlin")),
                            buildPublisher("Max Moritz", null)))
                    .build())
            .navDate("2022-08-30")
            .language(Locale.GERMAN)
            .otherLanguages(new LinkedHashSet<>(List.of(Locale.ITALIAN)))
            .manufacturingType("PRINT")
            .expressionTypes(
                new LinkedHashSet<>(
                    List.of( // list ensures order
                        ExpressionType.builder().mainType("TEXT").subType("PRINT").build(),
                        ExpressionType.builder().mainType("TEXT").subType("HANDWRITING").build())))
            .tag(Tag.builder().value("tag-value").build())
            .subjects(
                new LinkedHashSet<>(
                    List.of(
                        Subject.builder()
                            .subjectType("type")
                            .identifier(
                                Identifier.builder().namespace("namespace3").id("id3").build())
                            .label(new LocalizedText(Locale.GERMAN, "Subject B"))
                            .build(),
                        Subject.builder()
                            .subjectType("type")
                            .identifier(
                                Identifier.builder().namespace("namespace1").id("id1").build())
                            .identifier(
                                Identifier.builder().namespace("namespace2").id("id2").build())
                            .label(new LocalizedText(Locale.GERMAN, "Subject A"))
                            .build())))
            .mediaTypes(new LinkedHashSet<>(List.of("Buch", "CD")))
            .titles(
                List.of(
                    Title.builder()
                        .titleType(TitleType.builder().mainType("main").subType("main").build())
                        .text(
                            LocalizedText.builder()
                                .text(Locale.GERMAN, "Titel")
                                .text(
                                    new Locale.Builder()
                                        .setLanguage("zh")
                                        .setScript("hani")
                                        .build(),
                                    "圖註八十一難經辨眞")
                                .build())
                        .textLocaleOfOriginalScript(
                            new Locale.Builder().setLanguage("zh").setScript("hani").build())
                        .build(),
                    Title.builder()
                        .titleType(TitleType.builder().mainType("main").subType("sub").build())
                        .text(new LocalizedText(Locale.GERMAN, "Ein Test"))
                        .build()))
            .parent(parent)
            .build();
    return manifestation;
  }

  private Publisher buildPublisher(String personName, List<String> cityNames) {
    List<String> presentationParts = new ArrayList<>();
    if (cityNames != null) {
      presentationParts.add(cityNames.stream().collect(Collectors.joining(", ")));
    }
    if (personName != null) {
      presentationParts.add(personName);
    }

    return Publisher.builder()
        .agent(
            personName != null
                ? Person.builder().label(personName).title(Locale.GERMAN, personName).build()
                : null)
        .locations(
            cityNames != null
                ? cityNames.stream()
                    .map(c -> HumanSettlement.builder().label(c).title(Locale.GERMAN, c).build())
                    .collect(Collectors.toList())
                : null)
        .build();
  }

  @DisplayName("can be serialized and deserialized")
  @Test
  public void testSerializeDeserialize() throws Exception {
    Manifestation manifestation = createObject();
    checkSerializeDeserialize(
        manifestation,
        "serializedTestObjects/identifiable/entity/manifestation/Manifestation.json");
  }

  @DisplayName("only dumps uuids of relations in toString to avoid recursion")
  @Test
  public void testToString() {
    Person author = Person.builder().uuid(UUID.randomUUID()).build();
    Manifestation manifestation = Manifestation.builder().uuid(UUID.randomUUID()).build();
    EntityRelation entityRelation =
        EntityRelation.builder().subject(author).predicate("foo").object(manifestation).build();
    manifestation.setRelations(List.of(entityRelation));

    String actual = manifestation.toString();

    String expected =
        "Manifestation{composition='null', dimensions='null', expressionTypes=[], relations=[EntityRelation{subject="
            + author.getUuid()
            + ", predicate='foo', object="
            + manifestation.getUuid()
            + "}], language=null, manifestationType=null, manufacturingType=null, mediaTypes=[], otherLanguages=[], parents=[], publicationInfo=null, distributionInfo=null, productionInfo=null, scale='null', subjects=[], "
            + "titles=[], version='null', work=null, customAttributes=null, navDate=null, refId=0, notes=[], description=null, identifiableObjectType=MANIFESTATION, identifiers=[], label=null, localizedUrlAliases=null, previewImage=null, previewImageRenderingHints=null, tags=[], type=ENTITY, created=null, lastModified=null, uuid="
            + manifestation.getUuid()
            + "}";
    assertThat(actual).isEqualTo(expected);
  }
}
