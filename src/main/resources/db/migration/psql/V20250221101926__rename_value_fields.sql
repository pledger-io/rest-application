alter table rule_change
    rename column "value" to change_val;

alter table rule_change
alter column change_val type varchar(255);

alter table setting
    rename column "value" to setting_val;

alter table setting
alter column setting_val type varchar(255);
