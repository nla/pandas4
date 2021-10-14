create table SCOPE
(
    SCOPE_ID number       not null
        constraint SCOPE_PK
            primary key,
    DEPTH    number,
    NAME     varchar(255) not null
);

create sequence SCOPE_SEQ;

insert into SCOPE (SCOPE_ID, DEPTH, NAME) values (SCOPE_SEQ.nextval, null, 'All pages on this website');
insert into SCOPE (SCOPE_ID, DEPTH, NAME) values (SCOPE_SEQ.nextval, 0, 'Just this page');
insert into SCOPE (SCOPE_ID, DEPTH, NAME) values (SCOPE_SEQ.nextval, 1, 'This page and the pages it links to');

alter table TITLE_GATHER
    add SCOPE_ID number;
alter table TITLE_GATHER
    add constraint SCOPE_FK foreign key (SCOPE_ID) references SCOPE (SCOPE_ID);

