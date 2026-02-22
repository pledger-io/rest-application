-- add the type column and default all existing to CSVImportProvider

ALTER TABLE import_config
    ADD COLUMN type VARCHAR(255) DEFAULT 'CSVImportProvider' NOT NULL;

