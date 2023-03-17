create sequence SOCIAL_TARGET_SEQ;

create table SOCIAL_TARGET
(
    SOCIAL_TARGET_ID   NUMBER       not null primary key,
    QUERY              varchar(255) not null,
    SERVER             varchar(255) not null,
    CREATED_DATE       TIMESTAMP    null,
    LAST_MODIFIED_DATE TIMESTAMP    null,
    LAST_VISITED_DATE  TIMESTAMP    null,
    NEWEST_POST_DATE   TIMESTAMP    null,
    NEWEST_POST_ID     varchar(255) null,
    OLDEST_POST_DATE   TIMESTAMP    null,
    OLDEST_POST_ID     varchar(255) null,
    POST_COUNT         NUMBER       default 0 not null,
    CREATED_BY         NUMBER       null,
    LAST_MODIFIED_BY   NUMBER       null,
    TITLE_ID           NUMBER       null,
    constraint FK_SOCIAL_TARGET_TITLE
        foreign key (TITLE_ID) references TITLE (TITLE_ID),
    constraint FK_SOCIAL_TARGET_MODIFIED_BY
        foreign key (LAST_MODIFIED_BY) references INDIVIDUAL (INDIVIDUAL_ID),
    constraint FK_SOCIAL_TARGET_CREATED_BY
        foreign key (CREATED_BY) references INDIVIDUAL (INDIVIDUAL_ID)
);

