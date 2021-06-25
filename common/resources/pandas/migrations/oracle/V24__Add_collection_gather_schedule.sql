alter table COL add GATHER_SCHEDULE_ID number default 1 not null
    constraint COL_GATHER_SCHEDULE_FK references GATHER_SCHEDULE;