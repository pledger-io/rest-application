insert into account_type (id, label, hidden) values (9, 'reconcile', true);

insert into account (user_id, name, currency, archived, type_id)
    select
        u.id,
        'system account [reconcile]',
        'EUR',
        false,
        t.id
    from user_account u, account_type t
    where t.label = 'reconcile';
