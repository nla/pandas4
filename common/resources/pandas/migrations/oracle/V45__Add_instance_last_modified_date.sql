alter table INSTANCE add LAST_MODIFIED_DATE TIMESTAMP default SYSDATE not null;
CREATE INDEX INSTANCE_LAST_MODIFIED_DATE ON INSTANCE (LAST_MODIFIED_DATE);