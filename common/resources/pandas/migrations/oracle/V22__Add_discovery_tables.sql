create table DISCOVERY_SOURCE
(
    DISCOVERY_SOURCE_ID    NUMBER NOT NULL PRIMARY KEY,
    CREATED_DATE           timestamp,
    ITEM_DESCRIPTION_QUERY varchar(255),
    ITEM_LINK_QUERY        varchar(255),
    ITEM_NAME_QUERY        varchar(255),
    ITEM_QUERY             varchar(255),
    LAST_MODIFIED_DATE     timestamp,
    LINK_QUERY             varchar(255),
    NAME                   varchar(255),
    URL                    varchar(255),
    CREATED_BY             NUMBER
        constraint FK_DISCOVERY_SOURCE_CREATED_BY
            references INDIVIDUAL,
    LAST_MODIFIED_BY       NUMBER
        constraint FK_DISCOVERY_SOURCE_MODIFIED
            references INDIVIDUAL
);

create table DISCOVERY
(
    DISCOVERY_ID        number not null
        constraint DISCOVERY_PKEY
            primary key,
    CREATED_DATE        timestamp,
    DESCRIPTION         varchar(1024),
    LAST_MODIFIED_DATE  timestamp,
    LOCALITY            varchar(255),
    NAME                varchar(1024),
    POSTCODE            varchar(255),
    SOURCE_URL          varchar(255),
    STATE               varchar(255),
    URL                 varchar(1024),
    CREATED_BY          number
        constraint FK_DISCOVERY_CREATED_BY
            references INDIVIDUAL,
    LAST_MODIFIED_BY    number
        constraint FK_DISCOVERY_LAST_MODIFIED_BY
            references INDIVIDUAL,
    DISCOVERY_SOURCE_ID number
        constraint FK_DISCOVERY_SOURCE
            references DISCOVERY_SOURCE,
    TITLE_ID            number
        constraint FK_DISCOVERY_TITLE
            references TITLE
);