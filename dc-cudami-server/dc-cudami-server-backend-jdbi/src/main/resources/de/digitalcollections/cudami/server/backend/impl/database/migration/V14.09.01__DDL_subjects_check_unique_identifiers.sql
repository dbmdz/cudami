CREATE OR REPLACE FUNCTION subjects_is_unique_type_identifier(puuid UUID, ptype varchar, ids dbIdentifier[])
RETURNS boolean
STABLE LANGUAGE plpgsql
AS $body$
/**
 * Ensures uniqueness of `type` and single identifier across table `subjects`
 *
 * It is intended for check constraints and
 * returns `false` on violation.
 */
declare
	db_ident dbIdentifier;
	tuple_exists boolean;
begin
	if ids is null or array_length(ids, 1) = 0 then
		-- can be null so we ignore it
		return true;
	end if;
	foreach db_ident in array ids loop
		execute 'select exists(select 1 from subjects where $1 = type and $2 = any (identifiers) and $3 <> uuid)'
		  into tuple_exists
		  using ptype, db_ident, puuid;
		if tuple_exists then
			return false;
		end if;
	end loop;
	return true;
end;
$body$;


ALTER TABLE subjects
ADD CONSTRAINT unique_type_identifier CHECK (subjects_is_unique_type_identifier(uuid, "type", identifiers));

