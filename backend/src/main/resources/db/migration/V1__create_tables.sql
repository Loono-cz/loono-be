CREATE SEQUENCE account_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE account (
                                id bigint PRIMARY KEY DEFAULT nextval('account_seq'),
                                appointment_reminder_emails_opt_in boolean NOT NULL,
                                birthdate date NOT NULL,
                                leaderboard_anonymization_opt_in boolean NOT NULL,
                                newsletter_opt_in boolean NOT NULL,
                                nickname text NOT NULL,
                                points integer NOT NULL,
                                preferred_email text NOT NULL,
                                profile_image_url text,
                                sex text NOT NULL,
                                uid text NOT NULL,
                                created date NOT NULL
);

CREATE TABLE badge (
                              account_id bigint NOT NULL,
                              type character varying(255) NOT NULL,
                              last_update_on timestamp without time zone NOT NULL,
                              level integer NOT NULL
);

CREATE SEQUENCE examination_record_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE examination_record (
                                           id bigint PRIMARY KEY DEFAULT nextval('examination_record_seq'),
                                           first_exam boolean NOT NULL,
                                           planned_date timestamp without time zone,
                                           status text NOT NULL,
                                           type text NOT NULL,
                                           uuid text NOT NULL,
                                           account_id bigint NOT NULL
);

CREATE SEQUENCE healthcare_category_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE healthcare_category (
                                            id bigint PRIMARY KEY DEFAULT nextval('healthcare_category_seq'),
                                            value text NOT NULL
);

CREATE TABLE healthcare_provider (
                                            institution_id bigint NOT NULL,
                                            location_id bigint NOT NULL,
                                            administrative_district text,
                                            care_form text,
                                            care_type text,
                                            city text NOT NULL,
                                            code text NOT NULL,
                                            district text NOT NULL,
                                            district_code text NOT NULL,
                                            email text,
                                            fax text,
                                            house_number text NOT NULL,
                                            hq_city text,
                                            hq_district text,
                                            hq_district_code text,
                                            hq_house_number text,
                                            hq_postal_code text,
                                            hq_region text,
                                            hq_region_code text,
                                            hq_street text,
                                            ico text NOT NULL,
                                            institution_type text NOT NULL,
                                            lat double precision,
                                            lawyer_form_code text,
                                            layer_form text,
                                            lng double precision,
                                            person_type text NOT NULL,
                                            person_type_code text NOT NULL,
                                            phone_number text,
                                            postal_code text NOT NULL,
                                            region text NOT NULL,
                                            region_code text NOT NULL,
                                            specialization text NOT NULL,
                                            street text,
                                            substitute text,
                                            title text NOT NULL,
                                            website text,
                                            corrected_lat           double precision,
                                            corrected_lng           double precision,
                                            corrected_phone_number  text,
                                            corrected_website       text,
                                            last_update             timestamp,
                                            last_updated            timestamp
);

CREATE TABLE healthcare_provider_category (
                                                     institution_id bigint NOT NULL,
                                                     location_id bigint NOT NULL,
                                                     id bigint NOT NULL
);

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE revinfo (
                                rev integer PRIMARY KEY,
                                revtstmp bigint
);

CREATE SEQUENCE selfexamination_record_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
;

CREATE TABLE selfexamination_record (
                                               id bigint PRIMARY KEY DEFAULT nextval('healthcare_category_seq'),
                                               due_date date,
                                               result text,
                                               status text NOT NULL,
                                               type text NOT NULL,
                                               uuid text NOT NULL,
                                               waiting_to date,
                                               account_id bigint NOT NULL
);

CREATE TABLE server_properties (
                                          id bigint PRIMARY KEY,
                                          last_update date NOT NULL,
                                          super_user_name text NOT NULL,
                                          super_user_password text NOT NULL
);

CREATE TABLE corrected_healthcare_provider_category
(
    institution_id bigint not null,
    location_id    bigint not null,
    id             bigint not null
);

ALTER TABLE ONLY badge
    ADD CONSTRAINT pkey_badge PRIMARY KEY (account_id, type);

ALTER TABLE ONLY healthcare_provider_category
    ADD CONSTRAINT pkey_healthcare_provider_category PRIMARY KEY (institution_id, location_id, id);

ALTER TABLE ONLY healthcare_provider
    ADD CONSTRAINT pkey_healthcare_provider PRIMARY KEY (institution_id, location_id);

ALTER TABLE ONLY account
    ADD CONSTRAINT uk_account UNIQUE (uid);

ALTER TABLE ONLY selfexamination_record
    ADD CONSTRAINT uk_selfexamination_record UNIQUE (uuid);

ALTER TABLE ONLY examination_record
    ADD CONSTRAINT uk_examination_record UNIQUE (uuid);

ALTER TABLE ONLY examination_record
    ADD CONSTRAINT fk_examination_record FOREIGN KEY (account_id) REFERENCES account(id);

ALTER TABLE ONLY selfexamination_record
    ADD CONSTRAINT fk_selfexamination_record FOREIGN KEY (account_id) REFERENCES account(id);

ALTER TABLE ONLY badge
    ADD CONSTRAINT fk_badge FOREIGN KEY (account_id) REFERENCES account(id);

ALTER TABLE ONLY healthcare_provider_category
    ADD CONSTRAINT fk_healthcare_provider_category_hc_cat FOREIGN KEY (id) REFERENCES healthcare_category(id);

ALTER TABLE ONLY healthcare_provider_category
    ADD CONSTRAINT fk_healthcare_provider_category_hc_prov FOREIGN KEY (institution_id, location_id) REFERENCES healthcare_provider(institution_id, location_id);

ALTER TABLE ONLY corrected_healthcare_provider_category
    ADD CONSTRAINT pkey_corrected_healthcare_provider_category PRIMARY KEY(institution_id, location_id);

ALTER TABLE ONLY corrected_healthcare_provider_category
    ADD CONSTRAINT fkey_corrected_healthcare_provider_category FOREIGN KEY (institution_id, location_id) REFERENCES healthcare_provider(institution_id, location_id);
