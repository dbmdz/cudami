DO $$
BEGIN
    ALTER TABLE identifiers
        ADD CONSTRAINT unique_namespace_identifier UNIQUE(namespace, identifier);
EXCEPTION
    WHEN OTHERS THEN RAISE NOTICE 'constraint unique_namespace_identifier already exists';
END $$;