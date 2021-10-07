create table LINKED_ACCOUNT
(
    LINKED_ACCOUNT_ID  number       not null
        constraint LINKED_ACCOUNT_PK
            primary key,
    INDIVIDUAL_ID      number       not null
        constraint LINKED_ACCOUNT_INDIVIDUAL_FK
            references INDIVIDUAL,
    PROVIDER           varchar(255) not null,
    EXTERNAL_ID        varchar(255) not null,
    CREATED_DATE       timestamp    not null,
    LAST_MODIFIED_DATE timestamp,
    LAST_LOGIN_DATE    timestamp
);

create index LINKED_ACCOUNT_EXTID_INDEX ON LINKED_ACCOUNT (PROVIDER, EXTERNAL_ID);