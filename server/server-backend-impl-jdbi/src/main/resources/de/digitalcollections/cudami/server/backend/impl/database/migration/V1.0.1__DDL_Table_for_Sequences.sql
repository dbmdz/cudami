-- a database must exist before starting migration:
-- $ sudo su - postgres
-- ($ dropdb 'wms_db')
-- $ psql -c "CREATE USER wms PASSWORD 'somepassword';"
-- $ createdb wms_db -O wms

-- Sequence table for database independent sequences and to cope with non-JPA manual inserts.
-- Used in entities like this:
--
-- @Id
-- @TableGenerator(
--   name = SequenceConstants.GENERATOR_NAME, table = SequenceConstants.TABLE_NAME,
--   pkColumnName = SequenceConstants.PK_COLUMN_NAME, valueColumnName = SequenceConstants.VALUE_COLUMN_NAME,
--   allocationSize = SequenceConstants.ALLOCATION_SIZE,
--   pkColumnValue = "USER_SEQ"
-- )
-- @GeneratedValue(strategy = GenerationType.TABLE, generator = SequenceConstants.GENERATOR_NAME)
-- @Column(name = "id")
-- private Long id;
--
-- public static final int ALLOCATION_SIZE = 50;
-- public static final String GENERATOR_NAME = "TABLE_GEN";
-- public static final String PK_COLUMN_NAME = "SEQ_NAME";
-- public static final String TABLE_NAME = "SEQUENCE_TABLE";
-- public static final String VALUE_COLUMN_NAME = "SEQ_COUNT";
create table SEQUENCE_TABLE (
  SEQ_NAME varchar(255),
  SEQ_COUNT int4 
);