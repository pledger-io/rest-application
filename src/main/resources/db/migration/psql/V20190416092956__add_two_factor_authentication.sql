ALTER TABLE user_account
    ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE user_account
    ADD COLUMN two_factor_secret CHAR(32) NOT NULL DEFAULT '';
