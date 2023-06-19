CREATE OR REPLACE FUNCTION public.subjects_is_unique_type_identifier(puuid uuid, ptype character varying, ids dbidentifier[], plabel jsonb)
RETURNS boolean
STABLE LANGUAGE plpgsql
AS $function$
/**
 * Ensures uniqueness of `type` and single identifier across table `subjects`.
 *
 * If there are not any identifiers then `type` and `label` must be unique.
 * It is intended for check constraints and
 * returns `false` on violation.
 */
declare
	db_ident record; --dbidentifier actually but makes trouble in backups
	tuple_exists boolean;
begin
	if ids is null or cardinality(ids) = 0 then
		execute 'select exists(select 1 from public.subjects where $1 = type and $2 = label and $3 <> uuid)'
		  into tuple_exists
		  using ptype, plabel, puuid;
		if tuple_exists then
			return false;
		end if;
	else
		foreach db_ident in array ids loop
			execute 'select exists(select 1 from public.subjects where $1 = type and $2 = any (identifiers) and $3 <> uuid)'
			  into tuple_exists
			  using ptype, db_ident, puuid;
			if tuple_exists then
				return false;
			end if;
		end loop;
	end if;
	return true;
end;
$function$;

