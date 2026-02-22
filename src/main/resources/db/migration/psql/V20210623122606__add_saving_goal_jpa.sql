CREATE TABLE saving_goal
(
    id           BIGSERIAL      NOT NULL,

    goal         DECIMAL(22, 2) NOT NULL,
    allocated    DECIMAL(22, 2) NOT NULL DEFAULT 0,
    target_date  DATE           NOT NULL,

    name         VARCHAR(250)   NOT NULL,
    description  VARCHAR(1024),

    periodicity  CHAR(10),
    reoccurrence DECIMAL(10, 0),

    account_id   BIGINT         NOT NULL,
    archived     BOOLEAN        NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_saving_goal PRIMARY KEY (id),
    CONSTRAINT fk_saving_goal_account FOREIGN KEY (account_id) REFERENCES account (id)
);
