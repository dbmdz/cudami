package de.digitalcollections.cms.server.backend.impl.jpa.entity;

import de.digitalcollections.cms.model.api.security.Operation;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "operations")
public class OperationImplJpa implements Operation<Long> {

  @Id
  @TableGenerator(
          name = SequenceConstants.GENERATOR_NAME, table = SequenceConstants.TABLE_NAME,
          pkColumnName = SequenceConstants.PK_COLUMN_NAME, valueColumnName = SequenceConstants.VALUE_COLUMN_NAME,
          allocationSize = SequenceConstants.ALLOCATION_SIZE,
          pkColumnValue = "OPERATION_SEQ"
  )
  @GeneratedValue(strategy = GenerationType.TABLE, generator = SequenceConstants.GENERATOR_NAME)
  @Column(name = "id")
  private Long id;

  @NotEmpty
  @Column(name = "name", nullable = false, length = 45)
  private String name;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}
