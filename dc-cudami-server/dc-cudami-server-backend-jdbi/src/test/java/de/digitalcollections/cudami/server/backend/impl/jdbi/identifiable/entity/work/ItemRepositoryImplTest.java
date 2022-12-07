package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Gender;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {ItemRepositoryImpl.class})
@DisplayName("The Item Repository Test")
public class ItemRepositoryImplTest
    extends AbstractIdentifiableRepositoryImplTest<ItemRepositoryImpl> {

  @Autowired CorporateBodyRepository corporateBodyRepository;

  @Autowired
  @Qualifier("agentRepository")
  AgentRepository<Agent> agentRepository;

  @Autowired PersonRepository personRepository;

  private DigitalObjectRepository digitalObjectRepository;

  @BeforeEach
  void setup(
      @Autowired Jdbi jdbi,
      @Autowired DigitalObjectRepositoryImpl digitalObjectRepository,
      @Autowired WorkRepositoryImpl workRepository,
      @Autowired @Qualifier("agentRepository") AgentRepositoryImpl<Agent> agentRepository,
      @Autowired CudamiConfig config) {
    this.digitalObjectRepository = digitalObjectRepository;
    repo =
        new ItemRepositoryImpl(
            jdbi, digitalObjectRepository, workRepository, agentRepository, config);
  }

  @Test
  @DisplayName("can save an item")
  public void saveItem() {
    Item item = Item.builder().label(Locale.GERMAN, "Item").build();
    saveAndAssertTimestampsAndEqualityToSaveable(item);
  }

  @Test
  @DisplayName("can update an item")
  void testUpdate() {
    Item item = Item.builder().label(Locale.GERMAN, "Item").build();
    repo.save(item);

    Agent holder =
        agentRepository.save(
            Agent.builder()
                .label(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
                .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
                .build());

    item.setLabel("changed test");
    item.setHolders(List.of(holder));

    Item beforeUpdate = createDeepCopy(item);
    updateAndAssertUpdatedLastModifiedTimestamp(item);
    assertInDatabaseIsEqualToUpdateable(item, beforeUpdate, Function.identity());
  }

  @Test
  @DisplayName("Save one holder")
  void saveAndRetrieveOneHolder() {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    Agent holder0 = corporateBodyRepository.save((CorporateBody) holders.get(0));
    assertThat(holders.get(0).getUuid()).isNotNull();

    Item enclosingItem =
        Item.builder()
            .label(Locale.GERMAN, "Gesamt-Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Sig")
            .title(Locale.GERMAN, "Ein Gesamt-Buchtitel")
            .build();
    Item savedEnclosingItem = repo.save(enclosingItem);

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .partOfItem(savedEnclosingItem)
            .build();

    Item storedItem = repo.save(item);
    Item retrievedItem = repo.getByUuid(storedItem.getUuid());
    assertThat(storedItem).isEqualTo(retrievedItem);
    assertThat(retrievedItem.getHolders().size()).isEqualTo(1);
    assertThat(retrievedItem.getHolders().get(0)).isEqualTo(holder0);
    assertThat(retrievedItem.getPartOfItem()).isNotNull();
    assertThat(retrievedItem.getPartOfItem().getUuid()).isEqualTo(savedEnclosingItem.getUuid());
  }

  @Test
  @DisplayName("Save two holders")
  void saveAndRetrieveTwoHolders() {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "Some Amazing Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    List<Agent> holdersInDb =
        List.of(
            corporateBodyRepository.save((CorporateBody) holders.get(0)),
            corporateBodyRepository.save((CorporateBody) holders.get(1)));

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .build();

    Item storedItem = repo.save(item);
    Item retrievedItem = repo.getByUuid(storedItem.getUuid());
    assertThat(storedItem).isEqualTo(retrievedItem);
    assertThat(retrievedItem.getHolders().size()).isEqualTo(2);
    assertThat(retrievedItem.getHolders()).containsAll(holdersInDb);
  }

  @Test
  @DisplayName("returns holder(s) as agents only with UUID and label and no other fields")
  void returnHoldersAsAgents() {
    CorporateBody holder1 =
        corporateBodyRepository.save(
            CorporateBody.builder()
                .label("ACME Inc.")
                .identifier("foobar", "42")
                .homepageUrl("https://www.digitale-sammlungen.de/")
                .build());
    Person holder2 =
        personRepository.save(
            Person.builder()
                .label("Karl Ranseier")
                .identifier("gnd", "-1")
                .gender(Gender.MALE)
                .description(Locale.GERMAN, "Der erfolgloseste Entwickler aller Zeiten")
                .build());

    Item item = Item.builder().label("Test-Item").holders(List.of(holder1, holder2)).build();

    Item persisted = repo.getByUuid(repo.save(item).getUuid());

    assertThat(persisted.getHolders()).hasSize(2);

    Agent itemPersistedAgent1 = persisted.getHolders().get(0);
    assertThat(itemPersistedAgent1.getUuid()).isNotNull();
    assertThat(itemPersistedAgent1.getLabel()).isNotNull();
    assertThat(itemPersistedAgent1.getIdentifiers()).isEmpty();

    Agent itemPersistedAgent2 = persisted.getHolders().get(1);
    assertThat(itemPersistedAgent2.getUuid()).isNotNull();
    assertThat(itemPersistedAgent2.getLabel()).isNotNull();
    assertThat(itemPersistedAgent2.getIdentifiers()).isEmpty();

    Agent agent1 = agentRepository.getByUuid(itemPersistedAgent1.getUuid());
    assertThat(agent1).isEqualTo(holder1);

    Agent agent2 = agentRepository.getByUuid(itemPersistedAgent2.getUuid());
    assertThat(agent2).isEqualTo(holder2);
  }

  @Test
  @DisplayName("Update one of two holders")
  void updateHolders() {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "Some Amazing Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    Person person =
        Person.builder()
            .label("Karl Ranseier")
            .gender(Gender.MALE)
            .description(Locale.GERMAN, "Der erfolgloseste Entwickler aller Zeiten")
            .build();
    List<Agent> holdersInDb =
        List.of(
            corporateBodyRepository.save((CorporateBody) holders.get(0)),
            corporateBodyRepository.save((CorporateBody) holders.get(1)),
            personRepository.save(person));

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .build();

    Item storedItem = repo.save(item);
    Item retrievedItem = repo.getByUuid(storedItem.getUuid());

    retrievedItem.setHolders(List.of(holdersInDb.get(0), holdersInDb.get(2)));
    repo.update(retrievedItem);
    Item updatedItem = repo.getByUuid(retrievedItem.getUuid());

    assertThat(updatedItem.getHolders().size()).isEqualTo(2);
    assertThat(updatedItem.getHolders()).contains(holdersInDb.get(0), holdersInDb.get(2));
  }

  @Test
  @DisplayName("return holder and part_of_item uuids in the search result")
  void testSetHolderAndPartofItemUuidInSearchResult() {
    CorporateBody holder =
        corporateBodyRepository.save(
            CorporateBody.builder()
                .label(Locale.GERMAN, "A Company")
                .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
                .build());
    Item parentItem = repo.save(Item.builder().label("parent").build());
    Item expectedItem =
        repo.save(
            Item.builder()
                .partOfItem(parentItem)
                .label("testSetHolderAndPartofItemUuidInSearchResult")
                .holders(List.of(holder))
                .build());

    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber(0)
            .pageSize(100)
            .searchTerm("testSetHolderAndPartofItemUuidInSearchResult")
            .build();

    PageResponse<Item> actualPageResponse = repo.find(pageRequest);

    assertThat(actualPageResponse.getTotalElements()).isEqualTo(1);
    Item actualItem = actualPageResponse.getContent().get(0);
    expectedItem.setPartOfItem(Item.builder().uuid(expectedItem.getPartOfItem().getUuid()).build());
    assertThat(actualItem).isEqualTo(expectedItem);
  }

  @Test
  @DisplayName("can filter by the is_part_of uuid")
  void testIsPartOfFiltering() {
    Item parentItem = repo.save(Item.builder().label("parent").build());
    Item expectedItem = repo.save(Item.builder().partOfItem(parentItem).label("expected").build());

    PageRequest pageRequest =
        PageRequest.builder()
            .pageNumber(0)
            .pageSize(100)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("part_of_item.uuid")
                            .isEquals(parentItem.getUuid())
                            .build())
                    .build())
            .build();
    PageResponse<Item> actualPageResponse = repo.find(pageRequest);

    Item actualItem = actualPageResponse.getContent().get(0);

    assertThat(actualItem).isEqualTo(expectedItem);
  }

  @Test
  @DisplayName("can return an empty set of connected digital objects for an null item")
  void digitalObjectsForNullItem() {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    assertThat(repo.findDigitalObjects(null, pageRequest)).isEmpty();
  }

  @Test
  @DisplayName("can return an empty set of connected digital objects for an nonexisting item")
  void digitalObjectsForNonexistingItem() {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    assertThat(repo.findDigitalObjects(UUID.randomUUID(), pageRequest)).isEmpty();
  }

  @Test
  @DisplayName(
      "can return an empty set of connected digital objects for an item which has no digital objects connected to it")
  void digitalObjectsForItemWithoutDigitalObjects() {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    Item item = repo.save(Item.builder().label("item without digital objects").build());
    DigitalObject digitalObject =
        digitalObjectRepository.save(
            DigitalObject.builder().label("digital object without item").build());

    assertThat(repo.findDigitalObjects(item.getUuid(), pageRequest)).isEmpty();
  }

  @Test
  @DisplayName("can return digital objects connected to an item")
  void digitalObjectsForItem() {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    Item item1 = repo.save(Item.builder().label("item1 with two digitalObject2").build());
    Item item2 = repo.save(Item.builder().label("item2 with one digitalObject").build());
    DigitalObject digitalObject1 =
        digitalObjectRepository.save(
            DigitalObject.builder().label("digital object 1 for item1").item(item1).build());
    DigitalObject digitalObject2 =
        digitalObjectRepository.save(
            DigitalObject.builder().label("digital object 2 for item1").item(item1).build());
    DigitalObject digitalObject3 =
        digitalObjectRepository.save(
            DigitalObject.builder().label("digital object 1 for item2").item(item2).build());

    PageResponse<DigitalObject> actual = repo.findDigitalObjects(item1.getUuid(), pageRequest);
    assertThat(actual.getContent()).containsExactlyInAnyOrder(digitalObject1, digitalObject2);
  }

  @Test
  @DisplayName("can use paging on retrieval of digital objects connected to an item")
  void pagedDigitalObjectsForItem() {
    PageRequest pageRequest = PageRequest.builder().pageSize(1).pageNumber(0).build();
    Item item = repo.save(Item.builder().label("item1 with two digitalObject2").build());
    DigitalObject digitalObject1 =
        digitalObjectRepository.save(
            DigitalObject.builder().label("digital object 1 for item1").item(item).build());
    DigitalObject digitalObject2 =
        digitalObjectRepository.save(
            DigitalObject.builder().label("digital object 2 for item1").item(item).build());

    PageResponse<DigitalObject> actual = repo.findDigitalObjects(item.getUuid(), pageRequest);
    assertThat(actual.getContent()).hasSize(1);
  }
}
