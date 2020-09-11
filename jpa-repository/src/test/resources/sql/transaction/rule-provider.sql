insert into rule (id, name, user_id, group_id)
values (1, 'Smoke rule', 1, null),
       (2, 'Income rule', 1, 1);

insert into rule_condition (id, field, operation, cond_value, rule_id)
values (1, 'AMOUNT', 'LESS_THAN', '12.70', 1),
       (2, 'AMOUNT', 'MORE_THAN', '7.70', 1),
       (3, 'DESCRIPTION', 'CONTAINS', 'cigar', 1);

insert into rule_change (id, field, value, rule_id)
values (1, 'AMOUNT', '1', 1),
       (2, 'CATEGORY', '7', 1);
