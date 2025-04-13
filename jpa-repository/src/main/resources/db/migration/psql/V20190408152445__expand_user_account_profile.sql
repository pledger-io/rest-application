ALTER TABLE user_account
    ADD COLUMN currency CHAR(3);
ALTER TABLE user_account
    ADD COLUMN gravatar BYTEA;

