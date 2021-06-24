alter table GATHER_SCHEDULE add
    (YEARS NUMBER default 0 not null,
     MONTHS NUMBER default 0 not null,
     DAYS NUMBER default 0 not null,
     DAYS_OF_WEEK NUMBER default 0 not null,
     HOURS_OF_DAY NUMBER default 0 not null);

update GATHER_SCHEDULE set DAYS = 1 where SCHEDULE_NAME = 'Daily';
update GATHER_SCHEDULE set DAYS = 7 where SCHEDULE_NAME = 'Weekly';
update GATHER_SCHEDULE set DAYS = 14 where SCHEDULE_NAME = 'Fortnightly';
update GATHER_SCHEDULE set MONTHS = 1 where SCHEDULE_NAME = 'Monthly';
update GATHER_SCHEDULE set MONTHS = 3 where SCHEDULE_NAME = 'Quarterly';
update GATHER_SCHEDULE set YEARS = 1 where SCHEDULE_NAME = 'Annual';
update GATHER_SCHEDULE set MONTHS = 6 where SCHEDULE_NAME = 'Half-Yearly';
update GATHER_SCHEDULE set MONTHS = 9 where SCHEDULE_NAME = '9-Monthly';
update GATHER_SCHEDULE set MONTHS = 18 where SCHEDULE_NAME = '18-Monthly';
update GATHER_SCHEDULE set YEARS = 2 where SCHEDULE_NAME = 'Biennial';