package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.relation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.relation.EntityRelationRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityRelationService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.relation.EntityToEntityRelation;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The EntityRelation service")
class EntityRelationServiceImplTest extends AbstractServiceImplTest {

  private EntityRelationService entityRelationService;
  private EntityRelationRepository entityRelationRepository;

  @BeforeEach
  public void beforeEach() {
    entityRelationRepository = mock(EntityRelationRepository.class);
    entityRelationService = new EntityRelationServiceImpl(entityRelationRepository);
  }

  @DisplayName(
      "persisting entity relations will fill the objects of the relations with the UUID of the manifestations only")
  @Test
  public void relationObjectsOnlyWithUuid() throws ServiceException {
    UUID uuid = UUID.randomUUID();
    Manifestation manifestation = Manifestation.builder().uuid(uuid).build();
    EntityToEntityRelation relation =
        EntityToEntityRelation.builder()
            .subject(Person.builder().label("Karl Ranseier").randomUuid().build())
            .predicate("is_least_successful_author_of")
            .object(manifestation)
            .build();
    manifestation.setRelations(List.of(relation));

    List<EntityToEntityRelation> relations = manifestation.getRelations();
    entityRelationService.persistEntityRelations(manifestation, relations, true);
    manifestation.setRelations(relations);

    Manifestation manifestionWithUUIDOnly = Manifestation.builder().uuid(uuid).build();
    assertThat(
            manifestation.getRelations().stream()
                .map(EntityToEntityRelation::getObject)
                .collect(Collectors.toList()))
        .containsExactly(manifestionWithUUIDOnly);
  }
}
