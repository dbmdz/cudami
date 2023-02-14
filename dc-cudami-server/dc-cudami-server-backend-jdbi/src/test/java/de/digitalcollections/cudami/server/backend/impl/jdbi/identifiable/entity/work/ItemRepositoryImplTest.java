package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
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
      @Autowired @Qualifier("agentRepository") AgentRepositoryImpl<Agent> agentRepository,
      @Autowired CudamiConfig config) {
    this.digitalObjectRepository = digitalObjectRepository;
    repo = new ItemRepositoryImpl(jdbi, digitalObjectRepository, agentRepository, config);
  }

  @Test
  @DisplayName("can save an item")
  public void saveItem() throws RepositoryException {
    Item item = Item.builder().label(Locale.GERMAN, "Item").build();
    saveAndAssertTimestampsAndEqualityToSaveable(item);
  }

  @Test
  @DisplayName("can update an item")
  void testUpdate() throws RepositoryException {
    Item item = Item.builder().label(Locale.GERMAN, "Item").build();
    repo.save(item);

    Agent holder =
        Agent.builder()
            .label(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .build();

    agentRepository.save(holder);

    item.setLabel("changed test");
    item.setHolders(List.of(holder));

    Item beforeUpdate = createDeepCopy(item);
    updateAndAssertUpdatedLastModifiedTimestamp(item);
    assertInDatabaseIsEqualToUpdateable(item, beforeUpdate, Function.identity());
  }

  @Test
  @DisplayName("Save one holder")
  void saveAndRetrieveOneHolder() throws RepositoryException {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    CorporateBody holder0 = (CorporateBody) holders.get(0);

    corporateBodyRepository.save(holder0);
    assertThat(holders.get(0).getUuid()).isNotNull();

    Item enclosingItem =
        Item.builder()
            .label(Locale.GERMAN, "Gesamt-Buch")
            .exemplifiesManifestation(false)
            .title(Locale.GERMAN, "Ein Gesamt-Buchtitel")
            .build();
    repo.save(enclosingItem);

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .partOfItem(enclosingItem)
            .build();

    repo.save(item);
    Item retrievedItem = repo.getByUuid(item.getUuid());
    // do not expect too much ;-)
    item.setPartOfItem(
        Item.builder().uuid(enclosingItem.getUuid()).label(enclosingItem.getLabel()).build());
    assertThat(item).isEqualTo(retrievedItem);
    assertThat(retrievedItem.getHolders().size()).isEqualTo(1);
    assertThat(retrievedItem.getHolders().get(0)).isEqualTo(holder0);
    assertThat(retrievedItem.getPartOfItem()).isNotNull();
    assertThat(retrievedItem.getPartOfItem().getUuid()).isEqualTo(enclosingItem.getUuid());
    assertThat(retrievedItem.getPartOfItem().getLabel()).isNotNull();
    assertThat(retrievedItem.getPartOfItem().getLabel()).isEqualTo(enclosingItem.getLabel());
  }

  @Test
  @DisplayName("Save two holders")
  void saveAndRetrieveTwoHolders() throws RepositoryException {
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

    CorporateBody holder0 = (CorporateBody) holders.get(0);
    corporateBodyRepository.save(holder0);
    CorporateBody holder1 = (CorporateBody) holders.get(1);
    corporateBodyRepository.save(holder1);

    List<Agent> holdersInDb = List.of(holder0, holder1);

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .build();

    repo.save(item);
    Item retrievedItem = repo.getByUuid(item.getUuid());
    assertThat(retrievedItem).isEqualTo(item);
    assertThat(retrievedItem.getHolders().size()).isEqualTo(2);
    assertThat(retrievedItem.getHolders()).containsAll(holdersInDb);
  }

  @Test
  @DisplayName(
      "returns holder(s) as concrete agents but only with UUID and label and no other fields")
  void returnHoldersAsAgents() throws RepositoryException {
    CorporateBody holder1 =
        CorporateBody.builder()
            .label("ACME Inc.")
            .homepageUrl("https://www.digitale-sammlungen.de/")
            .build();
    corporateBodyRepository.save(holder1);

    Person holder2 =
        Person.builder()
            .label("Karl Ranseier")
            .gender(Gender.MALE)
            .description(Locale.GERMAN, "Der erfolgloseste Entwickler aller Zeiten")
            .build();
    personRepository.save(holder2);

    Item item = Item.builder().label("Test-Item").holders(List.of(holder1, holder2)).build();
    repo.save(item);

    Item persisted = repo.getByUuid(item.getUuid());

    assertThat(persisted.getHolders()).hasSize(2);

    Agent itemPersistedAgent1 = persisted.getHolders().get(0);
    assertThat(itemPersistedAgent1).isExactlyInstanceOf(CorporateBody.class);
    assertThat(itemPersistedAgent1.getUuid()).isNotNull();
    assertThat(itemPersistedAgent1.getLabel()).isNotNull();
    assertThat(itemPersistedAgent1.getIdentifiers()).isEmpty();

    Agent itemPersistedAgent2 = persisted.getHolders().get(1);
    assertThat(itemPersistedAgent2).isExactlyInstanceOf(Person.class);
    assertThat(itemPersistedAgent2.getUuid()).isNotNull();
    assertThat(itemPersistedAgent2.getLabel()).isNotNull();
    assertThat(itemPersistedAgent2.getIdentifiers()).isEmpty();

    CorporateBody agent1 = corporateBodyRepository.getByUuid(itemPersistedAgent1.getUuid());
    assertThat(agent1).isEqualTo(holder1);

    Person agent2 = personRepository.getByUuid(itemPersistedAgent2.getUuid());
    assertThat(agent2).isEqualTo(holder2);
  }

  @Test
  @DisplayName("Update one of two holders")
  void updateHolders() throws RepositoryException {
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
    personRepository.save(person);
    CorporateBody holder0 = (CorporateBody) holders.get(0);
    corporateBodyRepository.save(holder0);
    CorporateBody holder1 = (CorporateBody) holders.get(1);
    corporateBodyRepository.save(holder1);

    List<Agent> holdersInDb = List.of(holder0, holder1, person);

    Item item =
        Item.builder()
            .label(Locale.GERMAN, "Ein Buch")
            .exemplifiesManifestation(false)
            .identifier("mdz-sig", "Signatur")
            .title(Locale.GERMAN, "Ein Buchtitel")
            .holders(holders)
            .build();

    repo.save(item);
    Item retrievedItem = repo.getByUuid(item.getUuid());

    retrievedItem.setHolders(List.of(holdersInDb.get(0), holdersInDb.get(2)));
    repo.update(retrievedItem);
    Item updatedItem = repo.getByUuid(retrievedItem.getUuid());

    // before asserting anything we must fix the person: item repo only gets its data from table
    // agents,
    // so special fields are not selected
    person.setGender(null);

    assertThat(updatedItem.getHolders().size()).isEqualTo(2);
    assertThat(updatedItem.getHolders()).contains(holdersInDb.get(0), holdersInDb.get(2));
  }

  @Test
  @DisplayName("return holder and part_of_item uuids in the search result")
  void testSetHolderAndPartofItemUuidInSearchResult() throws RepositoryException {
    CorporateBody holder =
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build();
    corporateBodyRepository.save(holder);

    Item parentItem = Item.builder().label("parent").build();
    repo.save(parentItem);

    Item expectedItem =
        Item.builder()
            .partOfItem(parentItem)
            .label("testSetHolderAndPartofItemUuidInSearchResult")
            .holders(List.of(holder))
            .build();
    repo.save(expectedItem);

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
  void testIsPartOfFiltering() throws RepositoryException {
    Item parentItem = Item.builder().label("parent").build();
    repo.save(parentItem);
    Item expectedItem = Item.builder().partOfItem(parentItem).label("expected").build();
    repo.save(expectedItem);
    // partOfItem is only an UUID so we must not expect more
    expectedItem.setPartOfItem(Item.builder().uuid(parentItem.getUuid()).build());

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
  void digitalObjectsForItemWithoutDigitalObjects() throws RepositoryException {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    Item item = Item.builder().label("item without digital objects").build();
    repo.save(item);
    DigitalObject digitalObject =
        DigitalObject.builder().label("digital object without item").build();
    digitalObjectRepository.save(digitalObject);

    assertThat(repo.findDigitalObjects(item.getUuid(), pageRequest)).isEmpty();
  }

  @Test
  @DisplayName("can return digital objects connected to an item")
  void digitalObjectsForItem() throws RepositoryException {
    PageRequest pageRequest = PageRequest.builder().pageSize(25).pageNumber(0).build();
    Item item1 = Item.builder().label("item1 with two digitalObject2").build();
    repo.save(item1);
    Item item2 = Item.builder().label("item2 with one digitalObject").build();
    repo.save(item2);
    DigitalObject digitalObject1 =
        DigitalObject.builder().label("digital object 1 for item1").item(item1).build();
    digitalObjectRepository.save(digitalObject1);
    DigitalObject digitalObject2 =
        DigitalObject.builder().label("digital object 2 for item1").item(item1).build();
    digitalObjectRepository.save(digitalObject2);
    DigitalObject digitalObject3 =
        DigitalObject.builder().label("digital object 1 for item2").item(item2).build();
    digitalObjectRepository.save(digitalObject3);

    PageResponse<DigitalObject> actual = repo.findDigitalObjects(item1.getUuid(), pageRequest);
    assertThat(actual.getContent()).containsExactlyInAnyOrder(digitalObject1, digitalObject2);
  }

  @Test
  @DisplayName("can use paging on retrieval of digital objects connected to an item")
  void pagedDigitalObjectsForItem() throws RepositoryException {
    PageRequest pageRequest = PageRequest.builder().pageSize(1).pageNumber(0).build();
    Item item = Item.builder().label("item1 with two digitalObject2").build();
    repo.save(item);
    DigitalObject digitalObject1 =
        DigitalObject.builder().label("digital object 1 for item1").item(item).build();
    digitalObjectRepository.save(digitalObject1);
    DigitalObject digitalObject2 =
        DigitalObject.builder().label("digital object 2 for item1").item(item).build();
    digitalObjectRepository.save(digitalObject2);

    PageResponse<DigitalObject> actual = repo.findDigitalObjects(item.getUuid(), pageRequest);
    assertThat(actual.getContent()).hasSize(1);
  }
}
