package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UniqueObjectRepositoryImplTest {
  @DisplayName("can create a reduced field sql statement with replaced placeholders")
  @Test
  public void sqlSelectReducedFields() throws Exception {
    String actual = UniqueObjectRepositoryImpl.sqlSelectReducedFields("hallo", "test");
    assertThat(actual)
        .isEqualTo(
            " hallo.uuid test_uuid, hallo.created test_created, hallo.last_modified test_lastModified");
  }
}
