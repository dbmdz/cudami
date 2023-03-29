CREATE OR REPLACE FUNCTION unique_subjects_type_identifier() RETURNS trigger
LANGUAGE plpgsql
AS $body$
/**
 * Ensures uniqueness of `type` and single identifier across table `subjects`
 *
 * It is intended for insert and update triggers and
 * raises unique constraint exception on violation.
 */
declare
	db_ident dbIdentifier;
	tuple_exists boolean;
begin
	if NEW.identifiers is null then
		return NEW;
	end if;
	foreach db_ident in array NEW.identifiers loop
		execute 'select exists(select 1 from subjects where $1 = type and $2 = any (identifiers) and $3 <> uuid)'
		  into tuple_exists
		  using NEW.type, db_ident, NEW.uuid;
		if tuple_exists then
			raise EXCEPTION 'Duplicate type-identifier-pair: (%, %:%)', NEW.type, db_ident.namespace, db_ident.id
			  using ERRCODE = 'unique_violation';
		end if;
	end loop;
	return NEW;
end;
$body$;


CREATE TRIGGER tr_subjects_unique_type_identifier
BEFORE INSERT OR UPDATE
ON subjects
FOR EACH ROW
EXECUTE FUNCTION unique_subjects_type_identifier();

