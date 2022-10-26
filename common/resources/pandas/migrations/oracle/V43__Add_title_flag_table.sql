create table TITLE_FLAG
(
    INDIVIDUAL_ID number not null
        constraint TITLE_FLAG_INDIVIDUAL_FK
            references INDIVIDUAL (INDIVIDUAL_ID)
                on delete cascade,
    TITLE_ID      number not null
        constraint TITLE_FLAG_TITLE_FK
            references TITLE (TITLE_ID)
                on delete cascade,
    constraint TITLE_FLAG_PK
        primary key (INDIVIDUAL_ID, TITLE_ID)
);