ALTER TABLE TITLE ADD LEGAL_DEPOSIT INTEGER DEFAULT 0;

INSERT INTO REPORT_TYPE (REPORT_TYPE_ID, NAME, JAVA_CLASS, HAS_DETAILS, HAS_PERIOD, HAS_AGENCY, HAS_PUBLISHER_TYPE, HAS_RESTRICTION_TYPE) VALUES (7, 'Legal Deposit', 'au.gov.nla.webarchive.reports.LegalDepositReport', 0, 1, 1, 1, 0);

ALTER TABLE AGENCY ADD LEGAL_DEPOSIT INTEGER DEFAULT 0;