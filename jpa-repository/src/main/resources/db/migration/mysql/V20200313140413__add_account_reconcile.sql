insert into account_type (label, hidden) values ('reconcile', true);

insert into account (user_id, name, currency, archived, type_id)
    select
        u.id,
        'system account [reconcile]',
        'EUR',
        false,
        t.id
    from user_account u, account_type t
    where t.label = 'reconcile';
