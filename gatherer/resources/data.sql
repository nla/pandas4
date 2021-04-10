insert into GATHER_METHOD (GATHER_METHOD_ID, METHOD_DESC, METHOD_NAME)
values  (1, null, 'HTTrack'),
        (2, null, 'Upload'),
        (3, null, 'Heritrix'),
        (4, null, 'Bulk');

insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID, SCHEDULE_NAME, YEARS, MONTHS, DAYS, DAYS_OF_WEEK, HOURS_OF_DAY)
values  (1, 'None', 0, 0, 0, 0, 0),
        (2, 'Daily', 0, 0, 1, 0, 0),
        (3, 'Weekly', 0, 0, 7, 0, 0),
        (4, 'Fortnightly', 0, 0, 14, 0, 0),
        (5, 'Monthly', 0, 1, 0, 0, 0),
        (6, 'Quarterly', 0, 3, 0, 0, 0),
        (7, 'Annual', 1, 0, 0, 0, 0),
        (8, 'Half-Yearly', 0, 6, 0, 0, 0),
        (9, '9-Monthly', 0, 9, 0, 0, 0),
        (10, '18-Monthly', 0, 18, 0, 0, 0),
        (11, 'Biennial', 2, 0, 0, 0, 0),
        (12, 'Bimonthly', 0, 2, 0, 0, 0),
        (21, 'Daily (mid-morning)', 0, 0, 0, 0, 1024),
        (41, 'Daily (afternoon)', 0, 0, 0, 0, 65536);

insert into FORMAT (FORMAT_ID, NAME)
values  (1, 'Serial'),
        (2, 'Mono'),
        (3, 'Integrating');

insert into STATE (STATE_ID, STATE_NAME)
values  (1, 'archived'),
        (2, 'awaitGather'),
        (3, 'checked'),
        (4, 'checking'),
        (5, 'creation'),
        (6, 'deleted'),
        (7, 'deleting'),
        (8, 'gatherPause'),
        (9, 'gatherProcess'),
        (10, 'gathered'),
        (12, 'gathering'),
        (13, 'archiving'),
        (14, 'failed');

insert into STATUS (STATUS_ID, STATUS_NAME)
values  (1, 'nominated'),
        (2, 'rejected'),
        (3, 'selected'),
        (4, 'monitored'),
        (5, 'permission requested'),
        (6, 'permission denied'),
        (7, 'permission granted'),
        (8, 'permission impossible'),
        (11, 'ceased');

