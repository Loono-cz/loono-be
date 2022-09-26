ALTER TABLE examination_record ADD examination_category text;
ALTER TABLE examination_record_aud ADD examination_category text;

ALTER TABLE examination_record ADD custom_interval integer;
ALTER TABLE examination_record_aud ADD custom_interval integer;

ALTER TABLE examination_record ADD periodic_exam boolean;
ALTER TABLE examination_record_aud ADD periodic_exam boolean;


