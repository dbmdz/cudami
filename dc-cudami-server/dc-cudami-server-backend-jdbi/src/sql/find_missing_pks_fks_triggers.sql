/**
 * This big select finds all columns that are of type `uuid` or `uuid[]`
 * that have neither a constraint (PK, FK) nor a trigger checking for correct referencing.
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
),

trigger_refs AS (
SELECT arguments->>0 AS referenced_table, on_table referencing_table, arguments->>1 AS referencing_column, trig_function
FROM triggers
WHERE event @? '$[*] ? (@ like_regex "INSERT|UPDATE")'
ORDER BY referenced_table, referencing_table
)

SELECT * FROM (
	SELECT cols.table_name, cols.column_name, cols.udt_name,
		kcu.constraint_name, rc.unique_constraint_name referenced_constr,
		CASE WHEN tr.trig_function IS NOT NULL THEN format('%s(%s, %s)', tr.trig_function, tr.referenced_table, tr.referencing_column) ELSE NULL END AS tr_func_cmd
	FROM information_schema.columns cols
	LEFT JOIN information_schema.key_column_usage kcu ON kcu.table_name = cols.table_name AND kcu.column_name = cols.column_name
	LEFT JOIN information_schema.referential_constraints rc ON rc.constraint_name = kcu.constraint_name
	LEFT JOIN trigger_refs tr ON tr.referencing_table = cols.table_name AND tr.referencing_column = cols.column_name
) f
WHERE udt_name ~* '_?uuid' AND COALESCE(constraint_name, tr_func_cmd) IS NULL;

