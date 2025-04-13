CREATE TABLE rule_group
(
    id       BIGSERIAL    NOT NULL,
    name     VARCHAR(255) NOT NULL,
    sort     INT          NOT NULL,
    user_id  BIGINT       NOT NULL,
    archived BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_rule_group PRIMARY KEY (id),
    CONSTRAINT fk_rule_group_user FOREIGN KEY (user_id) REFERENCES user_account (id)
);

ALTER TABLE rule
    ADD COLUMN group_id BIGINT;
ALTER TABLE rule
    ADD CONSTRAINT fk_rule_rule_group FOREIGN KEY (group_id) REFERENCES rule_group (id);
