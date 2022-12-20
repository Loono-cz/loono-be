ALTER TABLE notification_log ADD created_at text;

CREATE SEQUENCE cron_control_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE cron_control (
                                  id bigint PRIMARY KEY DEFAULT nextval('cron_control_seq'),
                                  message text,
                                  created_at text
);