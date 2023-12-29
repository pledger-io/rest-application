update setting
set "value" = 'true'
where "value" = '1'
  and type = 'FLAG';

update setting
set "value" = 'false'
where "value" = '0'
  and type = 'FLAG';
