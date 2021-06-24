--
-- remove arbitrary 10 digit precision limits on the stats in INS_GATHER
-- so we can have instances larger than 10 GB
--
alter table INS_GATHER modify GATHER_SIZE NUMBER;
alter table INS_GATHER modify GATHER_FILES NUMBER;
alter table INS_GATHER modify INSTANCE_ID NUMBER;
