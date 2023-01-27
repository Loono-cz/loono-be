CREATE SEQUENCE consultancy_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE consultancy_log (
                                  id bigint PRIMARY KEY DEFAULT nextval('consultancy_log_seq'),
                                  account_uid text,
                                  tag text,
                                  message text,
                                  passed bool,
                                  caught_exception text,
                                  created_at text
);