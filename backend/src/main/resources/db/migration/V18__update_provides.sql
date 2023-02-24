ALTER TABLE healthcare_provider ADD categories text;
ALTER TABLE healthcare_provider_aud ADD categories text;
DROP TABLE healthcare_provider_category_aud;
DROP TABLE healthcare_provider_category;
DROP TABLE healthcare_category_aud;
DROP TABLE healthcare_category;