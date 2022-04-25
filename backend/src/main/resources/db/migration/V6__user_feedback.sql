CREATE SEQUENCE user_feedback_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE user_feedback (
                                  id bigint PRIMARY KEY DEFAULT nextval('user_feedback_seq'),
                                  evaluation integer NOT NULL DEFAULT 0,
                                  message text,
                                  account_id bigint NOT NULL
);

ALTER TABLE ONLY user_feedback
    ADD CONSTRAINT fk_user_feedback FOREIGN KEY (account_id) REFERENCES account(id);
