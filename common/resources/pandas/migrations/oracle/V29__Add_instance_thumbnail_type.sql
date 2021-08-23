alter table INSTANCE_THUMBNAIL add TYPE number(3) default 0 not null;
alter table INSTANCE_THUMBNAIL drop constraint INSTANCE_THUMBNAIL_PK;
alter table INSTANCE_THUMBNAIL add constraint INSTANCE_THUMBNAIL_PK primary key (INSTANCE_ID, TYPE);