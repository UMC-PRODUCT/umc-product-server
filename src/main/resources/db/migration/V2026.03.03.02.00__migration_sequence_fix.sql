-- V2026.02.28.06.00에서 ID를 수동으로 집어넣으면서 시퀀스가 꼬이는 문제가 발생해서 이를 해결함.

DO
$$
DECLARE
seq_record RECORD;
BEGIN
    -- 현재 스키마의 모든 시퀀스를 순회
FOR seq_record IN
SELECT s.relname AS seq_name,
       n.nspname AS schema_name,
       t.relname AS table_name,
       a.attname AS column_name
FROM pg_class s
         JOIN pg_namespace n ON n.oid = s.relnamespace
         JOIN pg_depend d ON d.objid = s.oid AND d.classid = 'pg_class'::regclass
        JOIN pg_class t
ON t.oid = d.refobjid AND t.relkind = 'r'
    JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = d.refobjsubid
WHERE s.relkind = 'S'
  AND n.nspname = 'public' -- 스키마가 public인 경우만
    LOOP
    EXECUTE format('SELECT setval(%L, COALESCE((SELECT max(%I) FROM %I.%I), 1))'
    , seq_record.schema_name || '.' || seq_record.seq_name
    , seq_record.column_name
    , seq_record.schema_name
    , seq_record.table_name);
END LOOP;
END $$;
