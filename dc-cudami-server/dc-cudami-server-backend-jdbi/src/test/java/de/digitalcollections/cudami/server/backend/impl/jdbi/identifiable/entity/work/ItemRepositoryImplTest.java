package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.PersonRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Gender;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {ItemRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The Item Repository Test")
public class ItemRepositoryImplTest {

  private ItemRepositoryImpl repo;

  @Autowired CorporateBodyRepository corporateBodyRepository;
  @Autowired AgentRepository agentRepository;
  @Autowired PersonRepository personRepository;

  @BeforeEach
  void setup(
      @Autowired Jdbi jdbi,
      @Autowired DigitalObjectRepositoryImpl digitalObjectRepository,
      @Autowired WorkRepositoryImpl workRepository,
      @Autowired CudamiConfig config) {
    repo = new ItemRepositoryImpl(jdbi, digitalObjectRepository, workRepository, config);
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
}
