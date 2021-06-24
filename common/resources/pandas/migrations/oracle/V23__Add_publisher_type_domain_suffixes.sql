alter table PUBLISHER_TYPE add DOMAIN_SUFFIXES VARCHAR(512);
update PUBLISHER_TYPE set DOMAIN_SUFFIXES = '.gov.au' where PUBLISHER_TYPE_ID = 1;
update PUBLISHER_TYPE set DOMAIN_SUFFIXES = '.asn.au .org .org.au' where PUBLISHER_TYPE_ID = 2;
update PUBLISHER_TYPE set DOMAIN_SUFFIXES = '.csiro.au .edu .edu.au' where PUBLISHER_TYPE_ID = 3;
update PUBLISHER_TYPE set DOMAIN_SUFFIXES = '.com .com.au .net .net.au' where PUBLISHER_TYPE_ID = 4;
update PUBLISHER_TYPE set DOMAIN_SUFFIXES = '.id.au' where PUBLISHER_TYPE_ID = 5;
