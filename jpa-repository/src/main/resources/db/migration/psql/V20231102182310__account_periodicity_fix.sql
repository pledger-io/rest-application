update account
set interest_periodicity = 'MONTHS'
where interest_periodicity = '0';

update account
set interest_periodicity = 'WEEKS'
where interest_periodicity = '1';

update account
set interest_periodicity = 'YEARS'
where interest_periodicity = '2';
