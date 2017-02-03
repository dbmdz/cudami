package de.digitalcollections.cms.client.backend.impl.jpa.entity;

/**
 * Using a sequence-table (instead of hibernate_sequence) to prevent id conflicts when inserting rows without hibernate
 * technology (we use Flyway on server start, where we use SQL-files or Spring-JDBC to insert data, before hibernate is
 * started). When hibernate starts, it looks up new starting id before allocating IDs (based on allocation_size).
 *
 * Example with Strings instead constants:
 *
 * @TableGenerator(name="TABLE_GEN", table="SEQUENCE_TABLE", pkColumnName="SEQ_NAME", valueColumnName="SEQ_COUNT",
 * pkColumnValue="USER_SEQ")
 */
public class SequenceConstants {

  public static final int ALLOCATION_SIZE = 50;
  public static final String GENERATOR_NAME = "TABLE_GEN";
  public static final String PK_COLUMN_NAME = "SEQ_NAME";
  public static final String TABLE_NAME = "SEQUENCE_TABLE";
  public static final String VALUE_COLUMN_NAME = "SEQ_COUNT";
}
