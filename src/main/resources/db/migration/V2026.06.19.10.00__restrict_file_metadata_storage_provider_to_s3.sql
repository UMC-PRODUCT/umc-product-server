ALTER TABLE file_metadata
    DROP CONSTRAINT IF EXISTS file_metadata_storage_provider_check;

ALTER TABLE file_metadata
    ADD CONSTRAINT file_metadata_storage_provider_check
        CHECK (storage_provider = 'AWS_S3');
