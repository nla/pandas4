ALTER TABLE TITLE_GATHER ADD (ADDITIONAL_URLS_TMP CLOB);
UPDATE TITLE_GATHER SET ADDITIONAL_URLS_TMP = ADDITIONAL_URLS;
ALTER TABLE TITLE_GATHER DROP COLUMN ADDITIONAL_URLS;
ALTER TABLE TITLE_GATHER RENAME COLUMN ADDITIONAL_URLS_TMP TO ADDITIONAL_URLS;