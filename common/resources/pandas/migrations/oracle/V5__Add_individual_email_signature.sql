alter table INDIVIDUAL add EMAIL_SIGNATURE CLOB ;
comment on column INDIVIDUAL.EMAIL_SIGNATURE is 'Text to append to emails sent by this curator';