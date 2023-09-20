do $$
declare
	entry record;
begin
	for entry in
	  select ua.uuid, i.identifiable_type, i.identifiable_objecttype
	  from url_aliases ua
		  inner join identifiables i on i.uuid = ua.target_uuid
	  where ua.target_identifiable_type <> i.identifiable_type
		  or ua.target_identifiable_objecttype <> i.identifiable_objecttype
	  loop

		update url_aliases set target_identifiable_type = entry.identifiable_type,
			target_identifiable_objecttype = entry.identifiable_objecttype
		where uuid = entry.uuid;

	end loop;
end;
$$;

