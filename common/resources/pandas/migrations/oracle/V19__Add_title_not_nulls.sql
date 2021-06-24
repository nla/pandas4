update TITLE set AWAITING_CONFIRMATION = 0 where AWAITING_CONFIRMATION is NULL;
alter table TITLE modify AWAITING_CONFIRMATION default 0 not null;

update TITLE set DISAPPEARED = 0 where DISAPPEARED is NULL;
alter table TITLE modify DISAPPEARED default 0 not null;

update TITLE set LEGAL_DEPOSIT = 0 where LEGAL_DEPOSIT is null;
alter table TITLE modify LEGAL_DEPOSIT not null;

update TITLE set IS_CATALOGUING_NOT_REQ = 0 where IS_CATALOGUING_NOT_REQ is NULL;
alter table TITLE modify IS_CATALOGUING_NOT_REQ default 0 not null;

update TITLE set IS_SUBSCRIPTION = 0 where IS_SUBSCRIPTION is NULL;
alter table TITLE modify IS_SUBSCRIPTION default 0 not null;

update TITLE set UNABLE_TO_ARCHIVE = 0 where UNABLE_TO_ARCHIVE is NULL;
alter table TITLE modify UNABLE_TO_ARCHIVE default 0 not null;