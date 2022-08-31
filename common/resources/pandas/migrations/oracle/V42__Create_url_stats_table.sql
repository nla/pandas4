create table URL_STATS
(
    SITE                 VARCHAR2(256) not null,
    CONTENT_TYPE         VARCHAR2(256) not null,
    YEAR                 NUMBER(10)    not null,
    SNAPSHOTS            NUMBER(19)    not null,
    TOTAL_CONTENT_LENGTH NUMBER(19)    not null,
    constraint URL_STATS_PK
        primary key (SITE, CONTENT_TYPE, YEAR)
);