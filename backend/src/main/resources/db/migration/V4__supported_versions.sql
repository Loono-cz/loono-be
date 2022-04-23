ALTER TABLE server_properties
    ADD COLUMN supported_app_version character varying(255) NOT NULL DEFAULT '0.3.8';
