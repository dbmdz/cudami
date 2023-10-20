/**
 * Collects all identifiers belonging to an identifiable and
 * returns them as full identifier objects in a JSONB array.
 *
 * :param: identifiable_uuid the uuid of the identifiable whose identifiers are searched for
 * :returns: JSONB array with identifier objects
 */
CREATE OR REPLACE FUNCTION public.get_identifiers(identifiable_uuid UUID) RETURNS jsonb
RETURNS NULL ON NULL INPUT
PARALLEL SAFE
STABLE
LANGUAGE plpgsql
AS $function$
declare
	idrow record;
	idjson jsonb;
	result_jsons jsonb[];
begin
	for idrow in select * from public.identifiers i where i.identifiable = identifiable_uuid loop
		idjson := jsonb_build_object('objectType', 'IDENTIFIER',
			'uuid', idrow.uuid,
			'identifiable', idrow.identifiable,
			'namespace', idrow.namespace,
			'id', idrow.identifier,
			'created', idrow.created,
			'lastModified', idrow.last_modified);
		result_jsons := array_append(result_jsons, idjson);
	end loop;
	return to_jsonb(result_jsons);
end;
$function$;

