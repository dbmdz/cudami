/**
 * Recursive select to list all children and "grandchildren"
 * of `identifiables`.
 *
 * Can be used as is. If you want a different startpoint then
 * just change `parent.relname` (first select in with-clause)
 * to whatever table name (as string) you need.
 */

WITH RECURSIVE inheritances (lvl, parent, child, child_id) AS (
SELECT 1, parent.relname, child.relname, i.inhrelid
	FROM pg_inherits i, pg_class parent, pg_class child
	WHERE i.inhparent = parent.oid and i.inhrelid = child.oid and parent.relname = 'identifiables'
UNION
SELECT inheritances.lvl + 1, parent.relname, child.relname, i.inhrelid
	FROM inheritances, pg_inherits i, pg_class parent, pg_class child
	WHERE inheritances.child_id = i.inhparent and i.inhparent = parent.oid and i.inhrelid = child.oid
)
SELECT lvl, parent, child FROM inheritances ORDER BY lvl, parent, child;

