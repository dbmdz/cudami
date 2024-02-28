package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractIdentifiableRepositoryImplTest;
import de.digitalcollections.model.geo.CoordinateLocation;
import de.digitalcollections.model.identifiable.entity.geo.location.River;
import de.digitalcollections.model.validation.ValidationException;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {RiverRepositoryImpl.class})
@DisplayName("The River Repository")
class RiverRepositoryImplTest extends AbstractIdentifiableRepositoryImplTest<RiverRepositoryImpl> {
  @Autowired private IdentifierRepository identifierRepository;
  @Autowired private UrlAliasRepository urlAliasRepository;

  @BeforeEach
  public void beforeEach() {
    repo = new RiverRepositoryImpl(jdbi, cudamiConfig, identifierRepository, urlAliasRepository);
  }

  @Test
  @DisplayName("can save (create) a river")
  void testSave() throws RepositoryException, ValidationException {
    River geolocation =
        River.builder()
            .label("Test")
            .coordinateLocation(
                new CoordinateLocation(48.15093479009475, 11.52559973769878, 0d, 6d))
            .build();
    saveAndAssertTimestampsAndEqualityToSaveable(geolocation);
  }

  @Test
  @DisplayName("can update a river")
  void testUpdate() throws RepositoryException, ValidationException {
    River geolocation =
        River.builder()
            .label("Test")
            .coordinateLocation(
                new CoordinateLocation(48.15093479009475, 11.52559973769878, 0d, 6d))
            .build();
    repo.save(geolocation);

    geolocation.setLabel("changed test");
    geolocation.setCoordinateLocation(
        new CoordinateLocation(58.15093479009475, 21.52559973769878, 0d, 6d));

    River beforeUpdate = createDeepCopy(geolocation);
    updateAndAssertUpdatedLastModifiedTimestamp(geolocation);
    assertInDatabaseIsEqualToUpdateable(geolocation, beforeUpdate, Function.identity());
  }
}