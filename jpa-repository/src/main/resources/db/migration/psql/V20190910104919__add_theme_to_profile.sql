ALTER TABLE user_account ADD COLUMN theme VARCHAR(255);

UPDATE user_account SET theme = 'dark';
