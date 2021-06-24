create table THUMBNAIL
(
    ID NUMBER(19) not null,
    TITLE_ID NUMBER(19),
    STATUS NUMBER(10) not null,
    PRIORITY NUMBER(10) not null,
    URL VARCHAR(4000),
    CAPTURE_DATE TIMESTAMP not null,
    SOURCE_TYPE VARCHAR(4000),
    WIDTH NUMBER(10) not null,
    HEIGHT NUMBER(10) not null,
    CROP_X NUMBER(10) not null,
    CROP_Y NUMBER(10) not null,
    CROP_WIDTH NUMBER(10) not null,
    CROP_HEIGHT NUMBER(10) not null,
    CONTENT_TYPE VARCHAR(4000) not null,
    DATA BLOB not null,
    CREATED_DATE TIMESTAMP not null,
    LAST_MODIFIED_DATE TIMESTAMP not null,

    PRIMARY KEY (ID),
    FOREIGN KEY (TITLE_ID) references TITLE (TITLE_ID) on delete CASCADE
);

create sequence THUMBNAIL_SEQ;