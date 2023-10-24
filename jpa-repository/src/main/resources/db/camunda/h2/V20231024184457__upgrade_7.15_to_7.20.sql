insert into ACT_GE_SCHEMA_LOG
values ('500', CURRENT_TIMESTAMP, '7.16.0');

create table ACT_RE_CAMFORMDEF
(
    ID_            varchar(64)  NOT NULL,
    REV_           integer,
    KEY_           varchar(255) NOT NULL,
    VERSION_       integer      NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    TENANT_ID_     varchar(64),
    primary key (ID_)
);

insert into ACT_GE_SCHEMA_LOG
values ('600', CURRENT_TIMESTAMP, '7.17.0');

-- https://jira.camunda.com/browse/CAM-14006 --
ALTER TABLE ACT_RU_JOB
    ADD COLUMN LAST_FAILURE_LOG_ID_ varchar(64);

ALTER TABLE ACT_RU_EXT_TASK
    ADD COLUMN LAST_FAILURE_LOG_ID_ varchar(64);

create index ACT_IDX_HI_VARINST_NAME on ACT_HI_VARINST (NAME_);
create index ACT_IDX_HI_VARINST_ACT_INST_ID on ACT_HI_VARINST (ACT_INST_ID_);

-- https://jira.camunda.com/browse/CAM-14205 --
-- ensures liquibase creates the same schema as the normal create scripts --
ALTER TABLE ACT_GE_BYTEARRAY
    ALTER COLUMN BYTES_ SET DATA TYPE blob;

insert into ACT_GE_SCHEMA_LOG
values ('700', CURRENT_TIMESTAMP, '7.18.0');

-- https://jira.camunda.com/browse/CAM-14303 --
ALTER TABLE ACT_RU_TASK
    ADD COLUMN LAST_UPDATED_ timestamp;
create index ACT_IDX_TASK_LAST_UPDATED on ACT_RU_TASK (LAST_UPDATED_);

-- https://jira.camunda.com/browse/CAM-14721
ALTER TABLE ACT_RU_BATCH
    ADD COLUMN START_TIME_ timestamp;

-- https://jira.camunda.com/browse/CAM-14722
ALTER TABLE ACT_RU_BATCH
    ADD COLUMN EXEC_START_TIME_ timestamp;
ALTER TABLE ACT_HI_BATCH
    ADD COLUMN EXEC_START_TIME_ timestamp;

insert into ACT_GE_SCHEMA_LOG
values ('800', CURRENT_TIMESTAMP, '7.19.0'),
       ('900', CURRENT_TIMESTAMP, '7.20.0');