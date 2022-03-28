CREATE TABLE account_aud (
                             id bigint NOT NULL,
                             rev integer NOT NULL,
                             revtype smallint,
                             appointment_reminder_emails_opt_in boolean,
                             birthdate date,
                             leaderboard_anonymization_opt_in boolean,
                             newsletter_opt_in boolean,
                             nickname text,
                             points integer,
                             preferred_email text,
                             profile_image_url text,
                             sex text,
                             uid text,
                             created date
);

CREATE TABLE badge_aud (
                           account_id bigint NOT NULL,
                           type character varying(255) NOT NULL,
                           rev integer NOT NULL,
                           revtype smallint,
                           last_update_on timestamp without time zone,
                           level integer
);

CREATE TABLE examination_record_aud (
                                        id bigint NOT NULL,
                                        rev integer NOT NULL,
                                        revtype smallint,
                                        first_exam boolean,
                                        planned_date timestamp without time zone,
                                        status text,
                                        type text,
                                        uuid text,
                                        account_id bigint
);

CREATE TABLE healthcare_category_aud (
                                         id bigint NOT NULL,
                                         rev integer NOT NULL,
                                         revtype smallint,
                                         value text
);

CREATE TABLE healthcare_provider_aud (
                                         institution_id bigint NOT NULL,
                                         location_id bigint NOT NULL,
                                         rev integer NOT NULL,
                                         revtype smallint,
                                         administrative_district text,
                                         care_form text,
                                         care_type text,
                                         city text,
                                         code text,
                                         district text,
                                         district_code text,
                                         email text,
                                         fax text,
                                         house_number text,
                                         hq_city text,
                                         hq_district text,
                                         hq_district_code text,
                                         hq_house_number text,
                                         hq_postal_code text,
                                         hq_region text,
                                         hq_region_code text,
                                         hq_street text,
                                         ico text,
                                         institution_type text,
                                         lat double precision,
                                         lawyer_form_code text,
                                         layer_form text,
                                         lng double precision,
                                         person_type text,
                                         person_type_code text,
                                         phone_number text,
                                         postal_code text,
                                         region text,
                                         region_code text,
                                         specialization text,
                                         street text,
                                         substitute text,
                                         title text,
                                         website text,
                                         corrected_lat           double precision,
                                         corrected_lng           double precision,
                                         corrected_phone_number  text,
                                         corrected_website       text,
                                         last_update             timestamp,
                                         last_updated            timestamp
);

CREATE TABLE healthcare_provider_category_aud (
                                                  rev integer NOT NULL,
                                                  institution_id bigint NOT NULL,
                                                  location_id bigint NOT NULL,
                                                  id bigint NOT NULL,
                                                  revtype smallint
);

CREATE TABLE selfexamination_record_aud (
                                            id bigint NOT NULL,
                                            rev integer NOT NULL,
                                            revtype smallint,
                                            due_date date,
                                            result text,
                                            status text,
                                            type text,
                                            uuid text,
                                            waiting_to date,
                                            account_id bigint
);


CREATE TABLE corrected_healthcare_provider_category_aud
(
    rev            integer not null,
    institution_id bigint  not null,
    location_id    bigint  not null,
    id             bigint  not null,
    revtype        smallint
);

ALTER TABLE ONLY corrected_healthcare_provider_category_aud
    ADD CONSTRAINT pkey_corrected_healthcare_provider_category_aud PRIMARY KEY(rev, institution_id, location_id, id);

ALTER TABLE ONLY account_aud
    ADD CONSTRAINT pkey_account_aud PRIMARY KEY (id, rev);

ALTER TABLE ONLY badge_aud
    ADD CONSTRAINT pkey_badge_aud PRIMARY KEY (account_id, type, rev);

ALTER TABLE ONLY examination_record_aud
    ADD CONSTRAINT pkey_examination_record_aud PRIMARY KEY (id, rev);

ALTER TABLE ONLY healthcare_category_aud
    ADD CONSTRAINT pkey_healthcare_category_aud PRIMARY KEY (id, rev);

ALTER TABLE ONLY healthcare_provider_aud
    ADD CONSTRAINT pkey_healthcare_provider_aud PRIMARY KEY (institution_id, location_id, rev);

ALTER TABLE ONLY healthcare_provider_category_aud
    ADD CONSTRAINT pkey_healthcare_provider_category_aud PRIMARY KEY (rev, institution_id, location_id, id);

ALTER TABLE ONLY selfexamination_record_aud
    ADD CONSTRAINT pkey_selfexamination_record_aud PRIMARY KEY (id, rev);
