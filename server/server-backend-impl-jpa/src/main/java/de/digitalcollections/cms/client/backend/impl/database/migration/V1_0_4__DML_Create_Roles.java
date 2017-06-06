package de.digitalcollections.cms.client.backend.impl.database.migration;

import de.digitalcollections.cms.model.api.security.Role;
import de.digitalcollections.cms.model.api.security.enums.Roles;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Insert predefined Role names.
 */
public class V1_0_4__DML_Create_Roles implements SpringJdbcMigration {

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    Roles[] values = Roles.values();
    int i = 0;
    for (Roles role : values) {
      jdbcTemplate.update("INSERT into roles (id, name) values (?,?)", ++i, Role.PREFIX + role.name());
    }
  }
}
