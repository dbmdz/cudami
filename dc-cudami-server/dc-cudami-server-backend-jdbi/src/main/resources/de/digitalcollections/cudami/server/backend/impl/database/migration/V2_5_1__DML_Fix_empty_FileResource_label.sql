UPDATE fileresources SET label=(CAST(CONCAT( '{"": "', filename, '"}') AS jsonb)) WHERE label IS NULL;
