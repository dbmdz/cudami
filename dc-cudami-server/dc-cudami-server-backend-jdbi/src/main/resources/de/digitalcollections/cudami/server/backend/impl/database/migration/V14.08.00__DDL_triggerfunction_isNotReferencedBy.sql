/**
 * Trigger function to check if an entry is referenced by another table.
 *
 * This function is the counterpart to `check_uuid_exists`/`check_array_uuids_exist`
 * that implement the FK function of checking that a referenced entry in a parent table
 * exists. This function on the other hand implements the "opposite" functionality of a FK.
 * It makes sure that an entry cannot be removed if another table still references it.
 * This trigger function is intended to be used in an "after delete" trigger.
 *
 * :params: variadic strings by pairs of 'referencing_table', 'referencing_column'
 */
CREATE OR REPLACE FUNCTION is_not_referenced_by()
RETURNS trigger
LANGUAGE plpgsql AS
$body$
DECLARE
	i integer := 0;
	table_name text;
	column_name text;
	has_entries boolean;
	is_uuid_array boolean := false;
	sql text;
	is_referenced boolean;
BEGIN
	if TG_ARGV is null or TG_NARGS = 0 or TG_NARGS % 2 <> 0 then
		raise EXCEPTION 'Pairs of table and corresponding column (as seperate strings each) must be passed to trigger function'
		  using ERRCODE = 'sql_statement_not_yet_complete';
	end if;
	for i in 0..TG_NARGS - 1 by 2 loop
		table_name := TG_ARGV[i];
		column_name := TG_ARGV[i+1];
		execute format('select exists (select 1 from %I)', table_name)
		  into has_entries;
		if not has_entries then
		  continue;
		end if;

		execute format($$ select pg_typeof(%I) = 'uuid[]'::regtype from %I limit 1 $$, column_name, table_name)
		  into is_uuid_array;
		if is_uuid_array then
			sql := 'select exists (select 1 from %I where $1 = any (%I))';
		else
			sql := 'select exists (select 1 from %I where %I = $1)';
		end if;
		execute format(sql, table_name, column_name)
		  into is_referenced
		  using OLD.uuid;
		if is_referenced then
			raise EXCEPTION 'UUID % is still referenced by %.%', OLD.uuid, table_name, column_name
			  using ERRCODE = 'foreign_key_violation';
		end if;
	end loop;
	return OLD;
END;
$body$;

