create table TYPE_STATS
(
    CONTENT_TYPE         VARCHAR2(255) not null,
    STATUS               NUMBER(10)    not null,
    YEAR                 NUMBER(10)    not null,
    SNAPSHOTS            NUMBER(19)    not null,
    TOTAL_CONTENT_LENGTH NUMBER(19)    not null,
    constraint GLOBAL_STATS_PK
        primary key (CONTENT_TYPE, STATUS, YEAR)
);