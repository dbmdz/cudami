# Helpful SQL Scripts

This directory is a loose collection of SQL scripts with selects and functions
that are not part of cudami itself but are helpful for development of and working
with cudami's database.

## Contribution

*This is not part of cudami software itself so do not place any migrations here that
are necessary for cudami!*

Please start your scripts with a describing comment that also contains hints for use.

If the script creates a function or other database object then it should contain the appropriate
drop statement as well.

Statements that modify existing tables or data **must** be commented to prevent them from
being executed accidentially, e.g.

```sql
/*
UPDATE table
	SET col = 'something'
	WHERE othercol = 5;
*/

--DELETE FROM othertable;
```

