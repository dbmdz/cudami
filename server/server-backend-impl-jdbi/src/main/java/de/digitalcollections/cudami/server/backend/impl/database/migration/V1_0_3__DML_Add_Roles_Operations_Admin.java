package de.digitalcollections.cudami.server.backend.impl.database.migration;

import de.digitalcollections.cudami.model.api.security.Role;
import de.digitalcollections.cudami.model.api.security.enums.Operations;
import de.digitalcollections.cudami.model.api.security.enums.Roles;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Insert predefined Role names.
 */
public class V1_0_3__DML_Add_Roles_Operations_Admin implements SpringJdbcMigration {

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    Roles[] roles = Roles.values();
    for (Roles role : roles) {
      jdbcTemplate.update("INSERT INTO roles (name) VALUES (?)", Role.PREFIX + role.name());
    }

    Operations[] operations = Operations.values();
    for (Operations operation : operations) {
      jdbcTemplate.update("INSERT INTO operations (name) VALUES (?)", operation.name());
    }

    // add operations "ALL" to role "ADMIN":
    Integer adminRoleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = (?)", new String[]{Role.PREFIX + Roles.ADMIN}, Integer.class);
    Integer allOperationsId = jdbcTemplate.queryForObject("SELECT id FROM operations WHERE name = (?)", new String[]{Operations.ALL.name()}, Integer.class);
    jdbcTemplate.update("INSERT INTO role_operation (role_id, operation_id) VALUES (?,?)", adminRoleId, allOperationsId);

  }
}
