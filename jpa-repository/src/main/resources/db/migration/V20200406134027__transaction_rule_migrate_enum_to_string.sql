-- Alter the change section
alter table rule_change add column tp_enum char(25);

update rule_change set tp_enum = 'SOURCE_ACCOUNT' where field = 0;
update rule_change set tp_enum = 'TO_ACCOUNT' where field = 1;
update rule_change set tp_enum = 'DESCRIPTION' where field = 2;
update rule_change set tp_enum = 'AMOUNT' where field = 3;
update rule_change set tp_enum = 'CATEGORY' where field = 4;
update rule_change set tp_enum = 'CHANGE_TRANSFER_TO' where field = 5;
update rule_change set tp_enum = 'CHANGE_TRANSFER_FROM' where field = 6;
update rule_change set tp_enum = 'BUDGET' where field = 7;
update rule_change set tp_enum = 'CONTRACT' where field = 8;

alter table rule_change drop column field;
alter table rule_change change `tp_enum` `field` char(25);

-- Alter the condition field section

alter table rule_condition add column tp_enum char(25);

update rule_condition set tp_enum = 'SOURCE_ACCOUNT' where field = 0;
update rule_condition set tp_enum = 'TO_ACCOUNT' where field = 1;
update rule_condition set tp_enum = 'DESCRIPTION' where field = 2;
update rule_condition set tp_enum = 'AMOUNT' where field = 3;
update rule_condition set tp_enum = 'CATEGORY' where field = 4;
update rule_condition set tp_enum = 'CHANGE_TRANSFER_TO' where field = 5;
update rule_condition set tp_enum = 'CHANGE_TRANSFER_FROM' where field = 6;
update rule_condition set tp_enum = 'BUDGET' where field = 7;
update rule_condition set tp_enum = 'CONTRACT' where field = 8;

alter table rule_condition drop column field;
alter table rule_condition change `tp_enum` `field` char(25);

-- Alter the condition operation

alter table rule_condition add column tp_enum char(25);

update rule_condition set tp_enum = 'EQUALS' where operation = 0;
update rule_condition set tp_enum = 'CONTAINS' where operation = 1;
update rule_condition set tp_enum = 'STARTS_WITH' where operation = 2;
update rule_condition set tp_enum = 'LESS_THAN' where operation = 3;
update rule_condition set tp_enum = 'MORE_THAN' where operation = 4;

alter table rule_condition drop column operation;
alter table rule_condition change `tp_enum` `operation` char(25);