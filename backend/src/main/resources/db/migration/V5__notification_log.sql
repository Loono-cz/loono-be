CREATE SEQUENCE notification_log_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE notification_log (
                                  id bigint PRIMARY KEY DEFAULT nextval('notification_log_seq'),
                                  name text,
                                  heading text,
                                  content text,
                                  include_external_user_ids text,
                                  schedule_time_of_day text,
                                  delayed_option text,
                                  large_image text,
                                  ios_attachments text
);
