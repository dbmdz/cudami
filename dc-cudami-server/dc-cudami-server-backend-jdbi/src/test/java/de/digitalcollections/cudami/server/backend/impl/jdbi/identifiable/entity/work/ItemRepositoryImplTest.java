package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.DigitalObjectRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.work.Item;
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

  @BeforeEach
  void setup(
      @Autowired Jdbi jdbi,
      @Autowired DigitalObjectRepositoryImpl digitalObjectRepository,
      @Autowired WorkRepositoryImpl workRepository,
      @Autowired CudamiConfig config) {
    repo = new ItemRepositoryImpl(jdbi, digitalObjectRepository, workRepository, config);
  }

  @Test
  void safeAndRetrieveOneHolder() {
    List<Agent> holders = new ArrayList<>();
    holders.add(
        CorporateBody.builder()
            .label(Locale.GERMAN, "A Company")
            .identifiableObjectType(IdentifiableObjectType.CORPORATE_BODY)
            .build());

    corporateBodyRepository.save((CorporateBody) holders.get(0));

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
  }

  @Test
  void safeAndRetrieveTwoHolders() {
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

    corporateBodyRepository.save((CorporateBody) holders.get(0));
    corporateBodyRepository.save((CorporateBody) holders.get(1));

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
  }
}
