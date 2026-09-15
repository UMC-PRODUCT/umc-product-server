DO
$$
    DECLARE
        r RECORD;
    BEGIN
        FOR r IN
            SELECT c.relname                                                  AS table_name,
                   a.attname                                                  AS column_name,
                   pg_get_serial_sequence(format('%I', c.relname), a.attname) AS seq_name
            FROM pg_class c
                     JOIN pg_attribute a ON a.attrelid = c.oid
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relkind = 'r'
              AND n.nspname = 'public'
              AND a.attnum > 0
              AND pg_get_serial_sequence(format('%I', c.relname), a.attname) IS NOT NULL
            LOOP
                EXECUTE format(
                    'SELECT setval(%L, COALESCE((SELECT MAX(%I) FROM %I), 1), true)',
                    r.seq_name, r.column_name, r.table_name
                        );
            END LOOP;
    END
$$;

