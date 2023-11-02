CREATE OR REPLACE FUNCTION dbidentifiers2jsonb(dbids dbidentifier[]) RETURNS jsonb
RETURNS NULL ON NULL INPUT
IMMUTABLE
PARALLEL SAFE
LANGUAGE plpgsql
AS $function$
declare
	id dbidentifier;
	idjson jsonb;
	result_json jsonb[];
begin
	foreach id in array dbids loop
		idjson := jsonb_build_object('objectType', 'IDENTIFIER',
			'namespace', id.namespace,
			'id', id.id);
		result_json := array_append(result_json, idjson);
	end loop;
	return to_jsonb(result_json);
end;
$function$;

