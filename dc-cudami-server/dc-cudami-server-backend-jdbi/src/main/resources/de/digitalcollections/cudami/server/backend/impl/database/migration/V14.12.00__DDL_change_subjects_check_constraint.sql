ALTER TABLE subjects DROP CONSTRAINT unique_type_identifier;
DROP FUNCTION subjects_is_unique_type_identifier;

CREATE FUNCTION subjects_is_unique_type_identifier(puuid UUID, ptype varchar, ids dbIdentifier[], plabel jsonb)
RETURNS boolean
STABLE LANGUAGE plpgsql
AS $body$
/**
 * Ensures uniqueness of `type` and single identifier across table `subjects`.
 *
 * If there are not any identifiers then `type` and `label` must be unique.
 * It is intended for check constraints and
 * returns `false` on violation.
 */
declare
	db_ident dbIdentifier;
	tuple_exists boolean;
begin
	if ids is null or ids = '{}'::dbIdentifier[] then
		execute 'select exists(select 1 from subjects where $1 = type and $2 = label and $3 <> uuid)'
		  into tuple_exists
		  using ptype, plabel, puuid;
		if tuple_exists then
			return false;
		end if;
	else
		foreach db_ident in array ids loop
			execute 'select exists(select 1 from subjects where $1 = type and $2 = any (identifiers) and $3 <> uuid)'
			  into tuple_exists
			  using ptype, db_ident, puuid;
			if tuple_exists then
				return false;
			end if;
		end loop;
	end if;
	return true;
end;
$body$;

ALTER TABLE subjects
ADD CONSTRAINT unique_type_identifier CHECK (subjects_is_unique_type_identifier(uuid, "type", identifiers, label));

