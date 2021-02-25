insert into FORMAT (FORMAT_ID, NAME)
values (1, 'Serial'),
       (2, 'Mono'),
       (3, 'Integrating');
alter sequence FORMAT_SEQ restart with 4;

insert into GATHER_METHOD (GATHER_METHOD_ID, METHOD_DESC, METHOD_NAME)
values (1, null, 'HTTrack'),
       (2, null, 'Upload'),
       (3, null, 'Heritrix'),
       (4, null, 'Bulk');
alter sequence GATHER_METHOD_SEQ restart with 5;

insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID, SCHEDULE_NAME, YEARS, MONTHS, DAYS, DAYS_OF_WEEK, HOURS_OF_DAY)
values  (1, 'None', 0, 0, 0, 0, 0),
        (2, 'Daily', 0, 0, 1, 0, 0),
        (3, 'Weekly', 0, 0, 7, 0, 0),
        (4, 'Fortnightly', 0, 0, 14, 0, 0),
        (5, 'Monthly', 0, 1, 0, 0, 0),
        (6, 'Quarterly', 0, 3, 0, 0, 0),
        (7, 'Annual', 1, 0, 0, 0, 0),
        (8, 'Half-Yearly', 0, 6, 0, 0, 0),
        (9, '9-Monthly', 0, 9, 0, 0, 0),
        (10, '18-Monthly', 0, 18, 0, 0, 0),
        (11, 'Biennial', 2, 0, 0, 0, 0),
        (12, 'Bimonthly', 0, 2, 0, 0, 0);
alter sequence gather_schedule_seq restart with 13;

insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION, PUBLISHER_TYPE, PUBLISHER_TYPE_ID)
values  ('Government bodies and agencies', 'Government', 1),
        ('Organisations can be public or private bodies that provide non-commercial material.', 'Organisation', 2),
        ('Educational Institutions', 'Education', 3),
        ('Commercial bodies provide material on a cost basis.', 'Commercial', 4),
        ('Individual', 'Personal', 5),
        ('Use when unknown', 'Other', 6);
alter sequence publisher_type_seq restart with 7;

insert into STATE (STATE_ID, STATE_NAME)
values (1, 'archived'),
       (2, 'awaitGather'),
       (3, 'checked'),
       (4, 'checking'),
       (5, 'creation'),
       (6, 'deleted'),
       (7, 'deleting'),
       (8, 'gatherPause'),
       (9, 'gatherProcess'),
       (10, 'gathered'),
       (12, 'gathering'),
       (13, 'archiving'),
       (14, 'failed');
alter sequence STATE_SEQ restart with 15;

insert into STATUS (STATUS_ID, STATUS_NAME)
values (1, 'nominated'),
       (2, 'rejected'),
       (3, 'selected'),
       (4, 'monitored'),
       (5, 'permission requested'),
       (6, 'permission denied'),
       (7, 'permission granted'),
       (8, 'permission impossible'),
       (11,'ceased');
alter sequence STATUS_SEQ restart with 12;

insert into PUBLIC.SUBJECT (SUBJECT_ID, SUBJECT_NAME, SUBJECT_PARENT_ID, THUMBNAIL_URL, DESCRIPTION, THUMBNAIL_ID)
values  (1, 'Agriculture', 13, null, null, null),
        (2, 'Arts', null, null, null, null),
        (3, 'Business & Economy', null, null, null, null),
        (4, 'Computers & Internet', 13, null, null, null),
        (5, 'Education', null, null, null, null),
        (6, 'Election Campaigns', 21, null, null, null),
        (7, 'Environment', null, null, null, null),
        (8, 'Festivals & Events (Arts)', 2, null, null, null),
        (9, 'Festivals & Events (Cultural)', 23, null, null, null),
        (10, 'Health', null, null, null, null),
        (11, 'History', null, null, null, null),
        (12, 'Indigenous Australians', null, null, null, null),
        (13, 'Industry & Technology', null, 'https://webarchive.nla.gov.au/thumbnail/collection/12909/snapshot/0', null, null),
        (14, 'Children', 23, null, null, null),
        (15, 'Government & Law', null, null, null, null),
        (16, 'Literature', 2, null, null, null),
        (17, 'Music', 2, null, null, null),
        (18, 'Media', null, null, null, null),
        (19, 'Olympic & Paralympic Games', 25, null, null, null),
        (20, 'Political Humour & Satire', 21, null, null, null),
        (21, 'Politics', null, null, null, null),
        (22, 'Sciences', null, null, null, null),
        (23, 'People & Culture', null, null, null, null),
        (24, 'Sporting Personalities', 25, null, null, null),
        (25, 'Sports & Recreation', null, null, null, null),
        (26, 'Centenary of Federation', 11, null, null, null),
        (27, 'Australian Republic Debate', 11, null, null, null),
        (28, 'Political Parties and Politicians', 21, null, null, null),
        (29, 'Blogs', 18, null, null, null),
        (41, 'Architecture', 2, null, null, null),
        (42, 'Dance', 2, null, null, null),
        (43, 'Decorative Arts', 2, null, null, null),
        (44, 'Design & Fashion', 2, null, null, null),
        (45, 'Fine Arts', 2, null, null, null),
        (46, 'Multi-Media and Digital Arts', 2, null, null, null),
        (47, 'Photography', 2, null, null, null),
        (48, 'Banking & Finance', 3, null, null, null),
        (49, 'Commerce', 3, null, null, null),
        (50, 'Economics', 3, null, null, null),
        (51, 'Management', 3, null, null, null),
        (52, 'Taxation', 3, null, null, null),
        (53, 'Defence', null, null, null, null),
        (54, 'Air Force', 53, null, null, null),
        (55, 'Army', 53, null, null, null),
        (56, 'Navy', 53, null, null, null),
        (57, 'Unit Associations', 53, null, null, null),
        (58, 'Veterans', 53, null, null, null),
        (59, 'Schooling', 5, null, null, null),
        (60, 'Tertiary Education', 5, null, null, null),
        (61, 'Vocational Education', 5, null, null, null),
        (62, 'Climate Change', 7, null, null, null),
        (63, 'Environmental Protection', 7, null, null, null),
        (64, 'Forestry', 7, null, null, null),
        (65, 'Water', 7, null, null, null),
        (66, 'Men', 23, null, null, null),
        (67, 'Commonwealth Government', 15, null, null, null),
        (70, 'Local Government', 15, null, null, null),
        (72, 'Alternative & Complementary Health Care', 10, null, null, null),
        (73, 'Health Research', 10, null, null, null),
        (74, 'Medical & Hospital Care', 10, null, null, null),
        (75, 'Medical Conditions & Diseases', 10, null, null, null),
        (76, 'Mental Health', 10, null, null, null),
        (77, 'Pharmaceuticals', 10, null, null, null),
        (78, 'Public Health', 10, null, null, null),
        (79, 'Constitution & Referenda', 11, null, null, null),
        (80, 'Family History & Genealogy', 11, null, null, null),
        (81, 'Local History', 11, null, null, null),
        (82, 'Military History', 11, null, null, null),
        (83, 'Humanities', null, null, null, null),
        (84, 'Anthropology', 83, null, null, null),
        (85, 'Archaeology', 83, null, null, null),
        (86, 'Philosophy', 83, null, null, null),
        (87, 'Government Indigenous Policy', 12, null, null, null),
        (88, 'Indigenous Art', 12, null, null, null),
        (89, 'Indigenous Business & Commerce', 12, null, null, null),
        (90, 'Indigenous Culture', 12, null, null, null),
        (91, 'Indigenous Education', 12, null, null, null),
        (92, 'Indigenous Employment', 12, null, null, null),
        (93, 'Indigenous Health', 12, null, null, null),
        (94, 'Indigenous History', 12, null, null, null),
        (95, 'Indigenous Land Rights', 12, null, null, null),
        (96, 'Indigenous Languages', 12, null, null, null),
        (97, 'Indigenous Native Title', 12, null, null, null),
        (98, 'Aquaculture & Fisheries', 13, null, null, null),
        (99, 'Construction', 13, null, null, null),
        (100, 'Energy', 13, null, null, null),
        (101, 'Industrial & Manufacturing', 13, null, null, null),
        (102, 'Mining', 13, null, null, null),
        (103, 'Telecommunications', 13, null, null, null),
        (104, 'Transportation', 13, null, null, null),
        (105, 'Radio', 18, null, null, null),
        (106, 'Television', 18, null, null, null),
        (107, 'Aged People', 23, null, null, null),
        (108, 'Cultural Heritage Management', 23, null, null, null),
        (109, 'Entertainment', 23, null, null, null),
        (110, 'Ethnic Communities & Heritage', 23, null, null, null),
        (111, 'Families', 23, null, null, null),
        (112, 'Food & Drink', 23, null, null, null),
        (113, 'Lesbian, Gay, Bisexual, Trans and Intersex', 23, null, null, null),
        (114, 'Libraries & Cultural Institutions', 23, null, null, null),
        (116, 'People with Disabilities', 23, null, null, null),
        (117, 'Religion', 23, null, null, null),
        (118, 'Women', 23, null, null, null),
        (119, 'Youth', 23, null, null, null),
        (120, 'Political Action', 21, null, null, null),
        (121, 'Astronomy', 22, null, null, null),
        (122, 'Biology', 22, null, null, null),
        (123, 'Biotechnology', 22, null, null, null),
        (124, 'Chemistry', 22, null, null, null),
        (125, 'Geography and Mapping', 22, null, null, null),
        (126, 'Geology', 22, null, null, null),
        (127, 'Mathematics', 22, null, null, null),
        (128, 'Physics', 22, null, null, null),
        (129, 'Sociology', 22, null, null, null),
        (130, 'Society & Social Issues', null, null, null, null),
        (131, 'Community Issues & Volunteering', 130, null, null, null),
        (132, 'Crime & Justice', 130, null, null, null),
        (133, 'Drug & Alcohol Issues', 130, null, null, null),
        (134, 'Employment & Industrial Relations', 130, null, null, null),
        (135, 'Housing', 130, null, null, null),
        (137, 'Social Institutions', 130, null, null, null),
        (138, 'Social Problems and Action', 130, null, null, null),
        (139, 'Social Welfare', 130, null, null, null),
        (140, 'Tourism & Travel', null, null, null, null),
        (141, 'Indigenous Tourism', 140, null, null, null),
        (142, 'Games & Hobbies', 25, null, null, null),
        (143, 'Sites for Children', 25, null, null, null),
        (144, 'Sporting Events', 25, null, null, null),
        (145, 'Sporting Organisations', 25, null, null, null),
        (161, 'Foreign Affairs & Trade', 15, null, null, null),
        (162, 'Law & Regulation', 15, null, null, null),
        (163, 'State & Territory Government', 15, null, null, null),
        (181, 'Film & Cinema', 2, null, null, null),
        (201, 'Performing Arts', 2, null, null, null),
        (221, 'Newspapers', 18, null, null, null),
        (241, 'Poetry', 2, null, null, null),
        (261, 'Immigration & Emigration', 130, null, null, null),
        (281, 'Comics & Zines', 18, null, null, null),
        (301, 'Animals', 22, null, null, null),
        (302, 'Plants', 22, null, null, null),
        (321, 'Natural Disasters', 7, null, null, null),
        (322, 'Charities and not-for-profits', 130, null, null, null),
        (323, 'Social Media', 18, null, null, null),
        (341, 'Family Violence', 130, null, null, null),
        (361, 'Linguistics', 22, null, null, null),
        (381, 'Commonwealth Games', 25, null, null, null);
alter sequence subject_seq restart with 382;

--
-- Create a default agency
--

insert into ORGANISATION(ALIAS,AGENCY_ID,AUDIT_DATE,AUDIT_USERID,COMMENTS,LONGCOUNTRY,EMAIL,FAX,INDEXER_ID,LINE1,LINE2,LOCALITY,MOBILE_PHONE,NAME,ORGANISATION_ID,PHONE,POSTCODE,PUBLISHER_ID,SERVICE_ID,LONGSTATE,URL) values ('EG',null,null,null,null,null,null,null,null,'Example Place','Example Place','Example',null,'Default Agency',1,null,'1234',null,null,'EXPL','http://www.example.org/');
insert into AGENCY (AGENCY_ID,EXTERNAL_EMAIL,FORM_LETTER_URL,LOCAL_DATABASE_PREFIX,LOCAL_REFERENCE_PREFIX,ORGANISATION_ID) values (1,'agency@example.org','http://example.org/manual/general_procedures.html',null,'',1);
insert into PUBLISHER (LOCAL_REFERENCE,NOTES,ORGANISATION_ID,PUBLISHER_ID,PUBLISHER_TYPE_ID) values (null,null,1,1,1);
update ORGANISATION set AGENCY_ID = 1, PUBLISHER_ID = 1 where ORGANISATION_ID = 1;

alter sequence ORGANISATION_SEQ restart with 2;
alter sequence AGENCY_SEQ restart with 2;
alter sequence PUBLISHER_SEQ restart with 2;

-- Create a default admin user
--

Insert into INDIVIDUAL (AUDIT_CREATE_DATE,AUDIT_CREATE_USERID,AUDIT_DATE,AUDIT_USERID,COMMENTS,EMAIL,FAX,FUNCTION,INDIVIDUAL_ID,IS_ACTIVE,MOBILE_PHONE,NAME_FAMILY,NAME_GIVEN,NAME_TITLE,PASSWORD,PHONE,URL,USERID) values (null,null,null,null,null,null,null,null,2,true,null,'Administrator','System',null,'admin',null,null,'admin');
Insert into INDIVIDUAL (AUDIT_CREATE_DATE,AUDIT_CREATE_USERID,AUDIT_DATE,AUDIT_USERID,COMMENTS,EMAIL,FAX,FUNCTION,INDIVIDUAL_ID,IS_ACTIVE,MOBILE_PHONE,NAME_FAMILY,NAME_GIVEN,NAME_TITLE,PASSWORD,PHONE,URL,USERID) values (null,null,null,null,null,null,null,null,1,true,null,'User','System',null,'ckent',null,null,'ckent');
alter sequence INDIVIDUAL_SEQ restart with 3;

Insert into ROLE (AUDIT_CREATE_DATE,AUDIT_DATE,AUDIT_USERID,COMMENTS,INDIVIDUAL_ID,ORGANISATION_ID,ROLE_ID,ROLE_TITLE,ROLE_TYPE) values (null,null,null,'These are the people that work directly with the PANDAS software as well as addressing any issues that may occur',1,1,1,'Pandas Administrator','PanAdmin');
alter sequence ROLE_SEQ restart with 2;
