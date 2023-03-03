/**
 * Show custom triggers, the associating table, the function behind and its arguments.
 *
 * This is the base select for all following selects.
 */
SELECT trig.tgname AS trigger,
	tbl.relname on_table,
	(select jsonb_agg(a) from regexp_split_to_table(encode(trig.tgargs, 'escape'), '\\000') t(a)) - '' AS arguments,
	(
		select jsonb_build_array(ist.action_timing) || jsonb_agg(ist.event_manipulation)
		from information_schema.triggers ist
		where ist.trigger_name = trig.tgname
		group by ist.action_timing
	) AS event,
	func.proname trig_function
FROM pg_trigger trig
	INNER JOIN pg_class tbl ON tbl.oid = trig.tgrelid
	INNER JOIN pg_proc func ON func.oid = trig.tgfoid
WHERE not trig.tgisinternal
ORDER BY on_table;


/**
 * Lists the tables that are looked up by custom triggers
 */
WITH triggers AS (
SELECT trig.tgname AS trigger,
	tbl.relname on_table,
	(select jsonb_agg(a) from regexp_split_to_table(encode(trig.tgargs, 'escape'), '\\000') t(a)) - '' AS arguments,
	(
		select jsonb_build_array(ist.action_timing) || jsonb_agg(ist.event_manipulation)
		from information_schema.triggers ist
		where ist.trigger_name = trig.tgname
		group by ist.action_timing
	) AS event,
	func.proname trig_function
FROM pg_trigger trig
	INNER JOIN pg_class tbl ON tbl.oid = trig.tgrelid
	INNER JOIN pg_proc func ON func.oid = trig.tgfoid
WHERE not trig.tgisinternal
)
SELECT arguments->>0 AS referenced_table, count(*) FROM triggers
-- uncomment for insert and update triggers only
--WHERE event @? '$[*] ? (@ like_regex "INSERT|UPDATE")'
GROUP BY referenced_table
ORDER BY referenced_table;


/*
 * Lists the tables that are looked up by custom triggers (referenced table)
 * and the belonging table and column the trigger is associated with
 * (referencing table and column)
 */
WITH triggers AS (
SELECT trig.tgname AS trigger,
	tbl.relname on_table,
	(select jsonb_agg(a) from regexp_split_to_table(encode(trig.tgargs, 'escape'), '\\000') t(a)) - '' AS arguments,
	(
		select jsonb_build_array(ist.action_timing) || jsonb_agg(ist.event_manipulation)
		from information_schema.triggers ist
		where ist.trigger_name = trig.tgname
		group by ist.action_timing
	) AS event,
	func.proname trig_function
FROM pg_trigger trig
	INNER JOIN pg_class tbl ON tbl.oid = trig.tgrelid
	INNER JOIN pg_proc func ON func.oid = trig.tgfoid
WHERE not trig.tgisinternal
)
SELECT arguments->>0 AS referenced_table, on_table referencing_table, arguments->>1 AS referencing_column, trig_function
FROM triggers
-- uncomment for insert and update triggers only
WHERE event @? '$[*] ? (@ like_regex "INSERT|UPDATE")'
ORDER BY referenced_table, referencing_table;

