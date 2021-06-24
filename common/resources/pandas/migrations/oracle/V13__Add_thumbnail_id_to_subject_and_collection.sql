alter table SUBJECT add THUMBNAIL_ID NUMBER(19);
alter table SUBJECT add constraint SUBJECT_THUMBNAIL_ID_FK foreign key (THUMBNAIL_ID) references THUMBNAIL;

alter table COL add THUMBNAIL_ID NUMBER(19);
alter table COL add constraint COL_THUMBNAIL_ID_FK foreign key (THUMBNAIL_ID) references THUMBNAIL;