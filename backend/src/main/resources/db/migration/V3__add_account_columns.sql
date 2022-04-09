ALTER TABLE account
    ADD COLUMN notify boolean NOT NULL DEFAULT true;

ALTER TABLE account_aud
    ADD COLUMN notify boolean;

ALTER TABLE account
    ADD COLUMN active boolean NOT NULL DEFAULT true;

ALTER TABLE account_aud
    ADD COLUMN active boolean;
