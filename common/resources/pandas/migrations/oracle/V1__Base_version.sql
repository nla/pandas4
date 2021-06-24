CREATE SEQUENCE "ACTION_SEQ";
CREATE SEQUENCE "ACTOR_SEQ";
CREATE SEQUENCE "AGENCY_AREA_IP_SEQ";
CREATE SEQUENCE "AGENCY_AREA_SEQ";
CREATE SEQUENCE "AGENCY_SEQ";
CREATE SEQUENCE "APPLICATION_SEQ";
CREATE SEQUENCE "ARCH_ISSUE_SEQ";
CREATE SEQUENCE "AUTH_GROUP_SEQ";
CREATE SEQUENCE "AUTH_RESTR_SEQ";
CREATE SEQUENCE "AUTH_USER_SEQ";
CREATE SEQUENCE "COL_SEQ";
CREATE SEQUENCE "COMMAND_LINE_OPT_SEQ";
CREATE SEQUENCE "CONDITION_SEQ";
CREATE SEQUENCE "CONTACT_METHOD_SEQ";
CREATE SEQUENCE "CONTACT_SEQ";
CREATE SEQUENCE "CONTACT_TYPE_SEQ";
CREATE SEQUENCE "COPYRIGHT_TYPE_SEQ";
CREATE SEQUENCE "DATE_RESTR_SEQ";
CREATE SEQUENCE "FORMAT_SEQ";
CREATE SEQUENCE "GATHER_DATE_SEQ";
CREATE SEQUENCE "GATHER_FILTER_PRESET_SEQ";
CREATE SEQUENCE "GATHER_METHOD_SEQ";
CREATE SEQUENCE "GATHER_SCHEDULE_SEQ";
CREATE SEQUENCE "INDEXER_SEQ";
CREATE SEQUENCE "INDIVIDUAL_SEQ";
CREATE SEQUENCE "INSTANCE_SEQ";
CREATE SEQUENCE "INS_GATHER_SEQ";
CREATE SEQUENCE "INS_RESOURCE_SEQ";
CREATE SEQUENCE "ISSUE_GROUP_SEQ";
CREATE SEQUENCE "MIME_EXTENSION_SEQ";
CREATE SEQUENCE "MIME_TYPE_SEQ";
CREATE SEQUENCE "NOTIFICATION_SEQ";
CREATE SEQUENCE "NOTIFICATION_STATUS_SEQ";
CREATE SEQUENCE "OLD_INSTANCE_STATE_SEQ";
CREATE SEQUENCE "OLD_INSTANCE_STATUS_SEQ";
CREATE SEQUENCE "OLD_TITLE_STANDING_SEQ";
CREATE SEQUENCE "OLD_TITLE_STATUS_SEQ";
CREATE SEQUENCE "OPTION_ARGUMENT_SEQ";
CREATE SEQUENCE "OPTION_GROUP_SEQ";
CREATE SEQUENCE "ORGANISATION_SEQ";
CREATE SEQUENCE "OWNER_HISTORY_SEQ";
CREATE SEQUENCE "PANDAS_EXCEPTION_LOG_SEQ";
CREATE SEQUENCE "PERIOD_RESTR_SEQ";
CREATE SEQUENCE "PERIOD_TYPE_SEQ";
CREATE SEQUENCE "PERMISSION_SEQ";
CREATE SEQUENCE "PERMISSION_STATE_SEQ";
CREATE SEQUENCE "PERMISSION_TYPE_SEQ";
CREATE SEQUENCE "PROFILE_SEQ";
CREATE SEQUENCE "PUBLISHER_SEQ";
CREATE SEQUENCE "PUBLISHER_TYPE_SEQ";
CREATE SEQUENCE "QA_PROBLEM_SEQ";
CREATE SEQUENCE "REASON_SEQ";
CREATE SEQUENCE "REPORT_SCHEDULE_SEQ";
CREATE SEQUENCE "REPORT_SEQ";
CREATE SEQUENCE "REPORT_TYPE_SEQ";
CREATE SEQUENCE "ROLE_SEQ";
CREATE SEQUENCE "STATE_HISTORY_SEQ";
CREATE SEQUENCE "STATE_SEQ";
CREATE SEQUENCE "STATUS_HISTORY_SEQ";
CREATE SEQUENCE "STATUS_SEQ";
CREATE SEQUENCE "SUBJECT_SEQ";
CREATE SEQUENCE "TEP_SEQ";
CREATE SEQUENCE "TITLE_GATHER_SEQ";
CREATE SEQUENCE "TITLE_HISTORY_SEQ";
CREATE SEQUENCE "TITLE_PAR_CHILD_SEQ";
CREATE SEQUENCE "TITLE_PREVIOUS_NAME_SEQ";
CREATE SEQUENCE "TITLE_SEQ";
CREATE SEQUENCE "UNSENT_EMAIL_SEQ";

CREATE TABLE "ACTION"
(	"ACTION_DESCRIPTION" VARCHAR2(1024),
"ACTION_ID" NUMBER NOT NULL,
"INDIVIDUAL_ID" NUMBER,
"LABEL" VARCHAR2(1024)
) ;

COMMENT ON COLUMN "ACTION"."ACTION_DESCRIPTION" IS 'A description of the action';
COMMENT ON COLUMN "ACTION"."ACTION_ID" IS 'Sequence generated unique primary key for the action';
COMMENT ON COLUMN "ACTION"."INDIVIDUAL_ID" IS 'Foreign key reference to the individual who can perform the action';
COMMENT ON COLUMN "ACTION"."LABEL" IS 'A label to identify the action';
COMMENT ON TABLE "ACTION"  IS 'An action which can be performed within the system. (not used so far as of April 2006)';

CREATE TABLE "ACTOR"
(	"ACTOR_DESCRIPTION" VARCHAR2(1024),
"ACTOR_ID" NUMBER NOT NULL,
"INDIVIDUAL_ID" NUMBER NOT NULL,
"LABEL" VARCHAR2(1024)
) ;

COMMENT ON COLUMN "ACTOR"."ACTOR_DESCRIPTION" IS 'A description of the actor';
COMMENT ON COLUMN "ACTOR"."ACTOR_ID" IS 'Sequence generated Actor primary key';
COMMENT ON COLUMN "ACTOR"."INDIVIDUAL_ID" IS 'Foreign key to the individual who corresponds to this actor in the system';
COMMENT ON COLUMN "ACTOR"."LABEL" IS 'A label to identify the actor';
COMMENT ON TABLE "ACTOR"  IS 'A system user, which has permission to permform a list of actions (not used so far as of April 2006)';

CREATE TABLE "ACTOR_ACTION"
(	"ACTION_ID" NUMBER NOT NULL,
"ACTOR_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "ACTOR_ACTION"."ACTION_ID" IS 'The action related to this actor';
COMMENT ON COLUMN "ACTOR_ACTION"."ACTOR_ID" IS 'The actor related to this action';
COMMENT ON TABLE "ACTOR_ACTION"  IS 'The relationship between an action and a system user';

CREATE TABLE "AGENCY"
(	"AGENCY_ID" NUMBER NOT NULL,
"EXTERNAL_EMAIL" VARCHAR2(64),
"FORM_LETTER_URL" VARCHAR2(4000),
"LOCAL_DATABASE_PREFIX" VARCHAR2(64),
"LOCAL_REFERENCE_PREFIX" VARCHAR2(64),
"LOGO" BLOB,
"ORGANISATION_ID" NUMBER
) ;

COMMENT ON COLUMN "AGENCY"."AGENCY_ID" IS 'Sequence generated Agency primary key';
COMMENT ON COLUMN "AGENCY"."EXTERNAL_EMAIL" IS 'Email address for this agency, to be displayed to the public';
COMMENT ON COLUMN "AGENCY"."FORM_LETTER_URL" IS 'The URL this agency uses to access any form letters they wish to use to communicate with publisher and title contacts.';
COMMENT ON COLUMN "AGENCY"."LOGO" IS 'This agency''s logo image file';
COMMENT ON COLUMN "AGENCY"."ORGANISATION_ID" IS 'The organisation table which corresponds to this agency';
COMMENT ON TABLE "AGENCY"  IS 'A partner agency who is involved in selecting and archiving titles';

CREATE TABLE "AGENCY_AREA"
(	"AGENCY_AREA_ID" NUMBER NOT NULL,
"AGENCY_ID" NUMBER,
"AREA_NAME" VARCHAR2(256),
"AREA_WORDING" VARCHAR2(2048)
) ;

COMMENT ON COLUMN "AGENCY_AREA"."AGENCY_AREA_ID" IS 'Sequence generated agency area id';
COMMENT ON COLUMN "AGENCY_AREA"."AGENCY_ID" IS 'Foreign key to the agency which maintains this area';
COMMENT ON COLUMN "AGENCY_AREA"."AREA_NAME" IS 'Name or label for the physical location being referred to';
COMMENT ON TABLE "AGENCY_AREA"  IS 'A location associated with an agency. It can be used when restricting titles so that they can only be viewed from particular areas. Each area is a label given to a set of IP addresses. Eg."National Library Reading Room"';

CREATE TABLE "AGENCY_AREA_IP"
(	"ADDRESS" VARCHAR2(256),
"AGENCY_AREA_ID" NUMBER,
"AGENCY_AREA_IP_ID" NUMBER NOT NULL,
"MASK" VARCHAR2(256)
) ;

COMMENT ON COLUMN "AGENCY_AREA_IP"."ADDRESS" IS 'An IP address located within a particular agency area';
COMMENT ON COLUMN "AGENCY_AREA_IP"."AGENCY_AREA_ID" IS 'Foreign key to the agency area this IP address is located in';
COMMENT ON COLUMN "AGENCY_AREA_IP"."AGENCY_AREA_IP_ID" IS 'sequency generated id for this agency area ip';
COMMENT ON TABLE "AGENCY_AREA_IP"  IS 'An IP address that is located in a listed agency area. This is used to restrict access to a title.';

CREATE TABLE "APPLICATION"
(	"APPLICATION_ID" NUMBER NOT NULL,
"NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "APPLICATION"."APPLICATION_ID" IS 'Sequency generated ID for an application';
COMMENT ON COLUMN "APPLICATION"."NAME" IS 'The name or title of a computer application';
COMMENT ON TABLE "APPLICATION"  IS 'A piece of software associated with a particular type of file.';

CREATE TABLE "APP_MIME_TYPE"
(	"APPLICATION_ID" NUMBER NOT NULL,
"MIME_TYPE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "APP_MIME_TYPE"."APPLICATION_ID" IS 'Foreign key to an application associated with this mime type';
COMMENT ON COLUMN "APP_MIME_TYPE"."MIME_TYPE_ID" IS 'Sequence generated ID for this mime type';
COMMENT ON TABLE "APP_MIME_TYPE"  IS 'A particular type of file.';

CREATE TABLE "ARCH_ISSUE"
(	"INSTANCE_ID" NUMBER,
"IS_DISPLAYED" NUMBER,
"ISSUE_GROUP_ID" NUMBER,
"ISSUE_ID" NUMBER NOT NULL,
"ISSUE_ORDER" NUMBER,
"TITLE" VARCHAR2(1024),
"URL" VARCHAR2(1024)
) ;

COMMENT ON COLUMN "ARCH_ISSUE"."INSTANCE_ID" IS 'Sequence generated ID for an issue';
COMMENT ON COLUMN "ARCH_ISSUE"."ISSUE_GROUP_ID" IS 'Foreign key to the group which contains this issue';
COMMENT ON COLUMN "ARCH_ISSUE"."ISSUE_ID" IS 'Sequence generated ID for an issue';
COMMENT ON COLUMN "ARCH_ISSUE"."ISSUE_ORDER" IS 'The display order for this issue within its group';
COMMENT ON COLUMN "ARCH_ISSUE"."TITLE" IS 'The label for this issue';
COMMENT ON COLUMN "ARCH_ISSUE"."URL" IS 'The URL for this issue within the display system. This will be a point somwehere within its related instance';
COMMENT ON TABLE "ARCH_ISSUE"  IS 'An alternate entry point into a archived instance. This is published in the display system in addition to the instance''s main entry URL.';

CREATE TABLE "AUTH_GROUP"
(	"AUTH_GROUP_ID" NUMBER NOT NULL,
"GROUP_NAME" VARCHAR2(256)
) ;

COMMENT ON TABLE "AUTH_GROUP"  IS 'A group of usernames which are used to restrict who can view an archived title.';

CREATE TABLE "AUTH_RESTR"
(	"CONDITION_DATE" TIMESTAMP (6),
"CONDITION_ID" NUMBER,
"TITLE_AUTH_RESTR_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER
) ;

COMMENT ON COLUMN "AUTH_RESTR"."CONDITION_DATE" IS 'The date the current condition (status) for this restriction was applied';
COMMENT ON COLUMN "AUTH_RESTR"."CONDITION_ID" IS 'Foreign key to the status associated with this restriction (enabled, disabled, etc)';
COMMENT ON COLUMN "AUTH_RESTR"."TITLE_AUTH_RESTR_ID" IS 'Sequence generated id for an authorisation restriction';
COMMENT ON COLUMN "AUTH_RESTR"."TITLE_ID" IS 'Foreign key to the title this authorisation restriction refers to';
COMMENT ON TABLE "AUTH_RESTR"  IS 'An authorisation restriction which can be applied to a title to restrict who can view the archived file/s.';

CREATE TABLE "AUTH_RES_GROUP"
(	"AUTH_GROUP_ID" NUMBER NOT NULL,
"AUTH_RESTRICT_ID" NUMBER NOT NULL
) ;

COMMENT ON TABLE "AUTH_RES_GROUP"  IS 'The relationship between an authorisation restriction and the group of usernames associated with it.';

CREATE TABLE "AUTH_USER"
(	"AUTH_GROUP_ID" NUMBER,
"AUTH_USER_ID" NUMBER NOT NULL,
"INDIVIDUAL_ID" NUMBER,
"PASSWORD" VARCHAR2(128),
"USERNAME" VARCHAR2(128)
) ;

COMMENT ON TABLE "AUTH_USER"  IS 'A username and password associated with an authorisation group. These groups can be associated with an authorisation restriction for a title.';

CREATE TABLE "COL"
(	"COL_ID" NUMBER NOT NULL,
"DISPLAY_COMMENT" VARCHAR2(4000),
"DISPLAY_ORDER" NUMBER,
"IS_DISPLAYED" NUMBER,
"NAME" VARCHAR2(256) NOT NULL,
"COL_PARENT_ID" NUMBER
) ;

COMMENT ON COLUMN "COL"."COL_ID" IS 'Sequence generated id for a collection';
COMMENT ON COLUMN "COL"."DISPLAY_COMMENT" IS 'Description for a collection';
COMMENT ON COLUMN "COL"."DISPLAY_ORDER" IS 'Order in which the collection will be displayed (Not used??)';
COMMENT ON COLUMN "COL"."IS_DISPLAYED" IS 'Can prevent a collection from appearing in the display system';
COMMENT ON COLUMN "COL"."NAME" IS 'The name of a collection';
COMMENT ON COLUMN "COL"."COL_PARENT_ID" IS 'Optional foreign key to the parernt collection';
COMMENT ON TABLE "COL"  IS 'A named set (collection) to which online resources can be assigned. These can be associated wioth a particular subject and are more specific than a subject. Eg. 2006 Referendum for new toothbrushes.';

CREATE TABLE "COL_SUBS"
(	"COL_ID" NUMBER NOT NULL,
"SUBJECT_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "COL_SUBS"."COL_ID" IS 'Foreign key to the collection related to a particular subject';
COMMENT ON COLUMN "COL_SUBS"."SUBJECT_ID" IS 'Foreign key to the subject related to a particular collection';
COMMENT ON TABLE "COL_SUBS"  IS 'The relationship between a collection and related subjects.';

CREATE TABLE "COMMAND_LINE_OPT"
(	"ACCESS_LEVEL" NUMBER,
"COMMAND_LINE_OPTION_ID" NUMBER NOT NULL,
"DEFAULT_VALUE" VARCHAR2(128),
"DISPLAY_NAME" VARCHAR2(256),
"EXPLANATION" VARCHAR2(2000),
"HIDE_ARGUMENT" NUMBER,
"HIDE_OPTION" NUMBER,
"IS_ACTIVE" NUMBER,
"IS_ARGUMENT_QUOTED" NUMBER,
"IS_MANDATORY" NUMBER,
"LONG_OPTION" VARCHAR2(64),
"OPTION_DESCRIPTION" VARCHAR2(256),
"OPTION_GROUP_ID" NUMBER,
"OPTION_PREFIX" VARCHAR2(16),
"OPTION_SEPARATOR" VARCHAR2(16),
"SHORT_OPTION" VARCHAR2(16),
"UI_ELEMENT" VARCHAR2(64)
) ;

COMMENT ON COLUMN "COMMAND_LINE_OPT"."ACCESS_LEVEL" IS 'Defines the level at which this option can be changed. 0 = system level, 1 = title or profile level)';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."COMMAND_LINE_OPTION_ID" IS 'Sequence generated ID for this command line option';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."DEFAULT_VALUE" IS 'The default value for this option';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."DISPLAY_NAME" IS 'The name to be displayed on the GUI for this option';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."EXPLANATION" IS 'A descriptive string to explain in detail what this option does and what format it should be entered in. ';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."HIDE_ARGUMENT" IS 'Indicates whether or not to hide the argument from the gatherer application';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."HIDE_OPTION" IS 'Indicates whether or not to hide the option from the gatherer application';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."IS_ACTIVE" IS 'Indicates whether or not the option can be set within the GUI (either at a system or title level)';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."IS_ARGUMENT_QUOTED" IS 'Whether or not this option''s argument should be quoted by the system when being passed to the gathering application';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."IS_MANDATORY" IS 'Whether this option *must* be provided to the gathering application';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."LONG_OPTION" IS 'The longer, more descriptive command line version of this option. eg. --help as opposed to -h';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."OPTION_DESCRIPTION" IS 'A short description of this option';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."OPTION_GROUP_ID" IS 'Foreign key to the logical group this option belongs to';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."OPTION_PREFIX" IS 'A string to be added to the beginning of the option, when passed to the gathering application';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."OPTION_SEPARATOR" IS 'String to be used after this option, to separate it from other options (when constructing the command line string for the gathering application).';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."SHORT_OPTION" IS 'The (short) command line string for this option. This is the value passed to the gathering application, along with the OPTION_PREFIX value. eg -h';
COMMENT ON COLUMN "COMMAND_LINE_OPT"."UI_ELEMENT" IS 'The type of user interface component that should be used to edit this option';
COMMENT ON TABLE "COMMAND_LINE_OPT"  IS 'Command line options available to be passed to the application being used to gather titles. eg. -P';

CREATE TABLE "CONDITION"
(	"CONDITION_DESCRIPTION" VARCHAR2(4000),
"CONDITION_ID" NUMBER NOT NULL,
"NAME" VARCHAR2(1024) NOT NULL
) ;

COMMENT ON COLUMN "CONDITION"."CONDITION_DESCRIPTION" IS 'An explanation of what each condition value means';
COMMENT ON COLUMN "CONDITION"."CONDITION_ID" IS 'A sequence generated id for a condition';
COMMENT ON COLUMN "CONDITION"."NAME" IS 'The name or label for a particular condition/status';
COMMENT ON TABLE "CONDITION"  IS 'The status lookup table for title display restrictions. Eg. enabled, disabled, expired';

CREATE TABLE "CONTACT"
(	"CONTACT_DATE" TIMESTAMP (6),
"CONTACT_ID" NUMBER NOT NULL,
"CONTACT_METHOD_ID" NUMBER,
"CONTACT_TYPE_ID" NUMBER,
"INDEXER_ID" NUMBER,
"INDIVIDUAL_ID" NUMBER NOT NULL,
"NOTE" VARCHAR2(4000),
"PUBLISHER_ID" NUMBER,
"TITLE_ID" NUMBER,
"USER_ID" NUMBER
) ;

COMMENT ON COLUMN "CONTACT"."CONTACT_DATE" IS 'The date and time on which a communication occurred';
COMMENT ON COLUMN "CONTACT"."CONTACT_ID" IS 'A sequence generated id for a contact event (communication)';
COMMENT ON COLUMN "CONTACT"."CONTACT_METHOD_ID" IS 'Foreign key to a lookup of methods of communication';
COMMENT ON COLUMN "CONTACT"."CONTACT_TYPE_ID" IS 'Foreign key to a lookup of types of communications';
COMMENT ON COLUMN "CONTACT"."INDEXER_ID" IS 'An optional foreign key to the indexing agency which this communication was with';
COMMENT ON COLUMN "CONTACT"."INDIVIDUAL_ID" IS 'The contact person this communication was with. This person should be associated with a publisher or an indexing agency.';
COMMENT ON COLUMN "CONTACT"."NOTE" IS 'A note on what this communication was about';
COMMENT ON COLUMN "CONTACT"."PUBLISHER_ID" IS 'An optional foreign key to the publisher which this communication was with';
COMMENT ON COLUMN "CONTACT"."TITLE_ID" IS 'A foreign key to the title this communication was in reference to';
COMMENT ON TABLE "CONTACT"  IS 'A communication (contact event) between a pandas user and a contact person for a publishing agency or title.';

CREATE TABLE "CONTACT_METHOD"
(	"CONTACT_METHOD" VARCHAR2(64) NOT NULL,
"CONTACT_METHOD_ID" NUMBER NOT NULL
) ;

CREATE TABLE "CONTACT_TYPE"
(	"CONTACT_TYPE" VARCHAR2(32) NOT NULL,
"CONTACT_TYPE_ID" NUMBER NOT NULL,
"EMAIL_SUBJECT" VARCHAR2(1024) NOT NULL
) ;

CREATE TABLE "COPYRIGHT_TYPE"
(	"COPYRIGHT_TYPE" VARCHAR2(64) NOT NULL,
"COPYRIGHT_TYPE_ID" NUMBER NOT NULL
) ;

COMMENT ON TABLE "COPYRIGHT_TYPE"  IS 'Lookup table for the type of copyright/disclaimer URL and note displayed on a title''s TEP';

CREATE TABLE "DATE_RESTR"
(	"AGENCY_AREA_ID" NUMBER,
"CONDITION_DATE" TIMESTAMP (6),
"CONDITION_ID" NUMBER,
"RESTRICTION_END_DATE" TIMESTAMP (6),
"RESTICTION_START_DATE" TIMESTAMP (6),
"TITLE_DATE_RESTRICTION_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER
) ;

COMMENT ON COLUMN "DATE_RESTR"."AGENCY_AREA_ID" IS 'Foreign key to the location which this restriction limits viewing of the title to';
COMMENT ON COLUMN "DATE_RESTR"."CONDITION_DATE" IS 'The date the current condition (status) for this restriction was applied';
COMMENT ON COLUMN "DATE_RESTR"."CONDITION_ID" IS 'Foreign key to the condition (status) of this title display restriction';
COMMENT ON COLUMN "DATE_RESTR"."RESTRICTION_END_DATE" IS 'The date on which this display restriction expires';
COMMENT ON COLUMN "DATE_RESTR"."RESTICTION_START_DATE" IS 'The date on which this display restriction first becomes effective (if enabled)';
COMMENT ON COLUMN "DATE_RESTR"."TITLE_DATE_RESTRICTION_ID" IS 'Sequence generated ID for this restriction';
COMMENT ON COLUMN "DATE_RESTR"."TITLE_ID" IS 'Foreign key to the title this restriction applies to';
COMMENT ON TABLE "DATE_RESTR"  IS 'A restriction on where and when a title''s TEP can be displayed.';

CREATE TABLE "FORMAT"
(	"FORMAT_ID" NUMBER NOT NULL,
"NAME" VARCHAR2(64) NOT NULL
) ;

COMMENT ON COLUMN "FORMAT"."FORMAT_ID" IS 'Sequence generated ID for a format';
COMMENT ON COLUMN "FORMAT"."NAME" IS 'A label for a type of title format';
COMMENT ON TABLE "FORMAT"  IS 'Lookup table for the format of a title. Eg. mono, integrating, serial';

CREATE TABLE "GATHER_DATE"
(	"GATHER_DATE" TIMESTAMP (6),
"GATHER_DATE_ID" NUMBER(10,0) NOT NULL,
"TITLE_GATHER_ID" NUMBER(10,0) NOT NULL
) ;

COMMENT ON COLUMN "GATHER_DATE"."GATHER_DATE" IS 'A date on which a non-recurring gather should take place for a title';
COMMENT ON COLUMN "GATHER_DATE"."GATHER_DATE_ID" IS 'Sequence generated ID for a non-recurring gather date';
COMMENT ON COLUMN "GATHER_DATE"."TITLE_GATHER_ID" IS 'Foreign key to the gather details for a particular title';
COMMENT ON TABLE "GATHER_DATE"  IS 'A one off date on which a title is to be gathered (non-recurring)';

CREATE TABLE "GATHER_FILTER_PRESET"
(	"FILTER_NAME" VARCHAR2(256),
"FILTER_PRESET" VARCHAR2(256),
"GATHER_FILTER_PRESET_ID" NUMBER NOT NULL
) ;

CREATE TABLE "GATHER_METHOD"
(	"GATHER_METHOD_ID" NUMBER(10,0) NOT NULL,
"METHOD_DESC" VARCHAR2(256),
"METHOD_NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "GATHER_METHOD"."GATHER_METHOD_ID" IS 'Sequence generated ID for a gather method';
COMMENT ON COLUMN "GATHER_METHOD"."METHOD_DESC" IS 'A description of a type of gather method';
COMMENT ON COLUMN "GATHER_METHOD"."METHOD_NAME" IS 'A label for a type of gather method';
COMMENT ON TABLE "GATHER_METHOD"  IS 'Lookup table for available methods of gathering a title';

CREATE TABLE "GATHER_SCHEDULE"
(	"GATHER_SCHEDULE_ID" NUMBER(10,0) NOT NULL,
"SCHEDULE_NAME" VARCHAR2(256) NOT NULL
) ;

COMMENT ON COLUMN "GATHER_SCHEDULE"."GATHER_SCHEDULE_ID" IS 'Sequence generated ID for a recurring gather frequency';
COMMENT ON COLUMN "GATHER_SCHEDULE"."SCHEDULE_NAME" IS 'An frequency available for recurring gather schedules';
COMMENT ON TABLE "GATHER_SCHEDULE"  IS 'Lookup table of frequencies available for scheduling a recurring gather.';

CREATE TABLE "INDEXER"
(	"DO_NOTIFY" NUMBER,
"INDEXER_ID" NUMBER NOT NULL,
"NOTE" VARCHAR2(4000),
"ORGANISATION_ID" NUMBER
) ;

COMMENT ON COLUMN "INDEXER"."DO_NOTIFY" IS 'Whether to notify this indexing agency when significant events occur, or not';
COMMENT ON COLUMN "INDEXER"."INDEXER_ID" IS 'Sequence generated ID for an indexing agency';
COMMENT ON COLUMN "INDEXER"."NOTE" IS 'Freeform notes about an indexing agency';
COMMENT ON TABLE "INDEXER"  IS 'An indexing agency who nominates titles, either by external methods of communicating with system users, or directly using informational user accounts linked to indexers rather than partner agencies';

CREATE TABLE "INDIVIDUAL"
(	"AUDIT_CREATE_DATE" TIMESTAMP (6),
"AUDIT_CREATE_USERID" NUMBER,
"AUDIT_DATE" TIMESTAMP (6),
"AUDIT_USERID" NUMBER,
"COMMENTS" VARCHAR2(100),
"EMAIL" VARCHAR2(120),
"FAX" VARCHAR2(25),
"FUNCTION" VARCHAR2(120),
"INDIVIDUAL_ID" NUMBER NOT NULL,
"IS_ACTIVE" NUMBER,
"MOBILE_PHONE" VARCHAR2(25),
"NAME_FAMILY" VARCHAR2(30),
"NAME_GIVEN" VARCHAR2(130),
"NAME_TITLE" VARCHAR2(12),
"PASSWORD" VARCHAR2(100),
"PWDIGEST" VARCHAR2(100),
"PHONE" VARCHAR2(25),
"URL" VARCHAR2(1024),
"USERID" VARCHAR2(20)
) ;

COMMENT ON COLUMN "INDIVIDUAL"."AUDIT_CREATE_DATE" IS 'The date and time on which this individual was created';
COMMENT ON COLUMN "INDIVIDUAL"."AUDIT_CREATE_USERID" IS 'The system user who created this individual';
COMMENT ON COLUMN "INDIVIDUAL"."AUDIT_DATE" IS 'The date and time on which this individual was last updated';
COMMENT ON COLUMN "INDIVIDUAL"."AUDIT_USERID" IS 'The system user who last updated this individual';
COMMENT ON COLUMN "INDIVIDUAL"."COMMENTS" IS 'Notes about this individual';
COMMENT ON COLUMN "INDIVIDUAL"."EMAIL" IS 'The email address for this individual';
COMMENT ON COLUMN "INDIVIDUAL"."FAX" IS 'The fax number for this individual';
COMMENT ON COLUMN "INDIVIDUAL"."INDIVIDUAL_ID" IS 'Sequence generated ID for this individual';
COMMENT ON COLUMN "INDIVIDUAL"."MOBILE_PHONE" IS 'The mobile phone number for this individual';
COMMENT ON COLUMN "INDIVIDUAL"."NAME_FAMILY" IS 'The family (last) name of this individual';
COMMENT ON COLUMN "INDIVIDUAL"."NAME_GIVEN" IS 'The given (first) name of this individual';
COMMENT ON COLUMN "INDIVIDUAL"."NAME_TITLE" IS 'The title of this individual (eg. Mr, Mrs)';
COMMENT ON COLUMN "INDIVIDUAL"."PASSWORD" IS 'The password for this individual''s user account';
COMMENT ON COLUMN "INDIVIDUAL"."PHONE" IS 'The phone number for this individual';
COMMENT ON COLUMN "INDIVIDUAL"."URL" IS 'A URL for this individual';
COMMENT ON COLUMN "INDIVIDUAL"."USERID" IS 'The username for this individual''s user account';
COMMENT ON TABLE "INDIVIDUAL"  IS 'A person who is connected to the archive. These could be system users, or contact people linked to indexers, publishers or titles';

CREATE TABLE "INDIV_NOTIF"
(	"INDIVIDUAL_ID" NUMBER NOT NULL,
"NOTIFICATION_ID" NUMBER NOT NULL,
"NOTIFICATION_STATUS_ID" NUMBER
) ;

COMMENT ON COLUMN "INDIV_NOTIF"."INDIVIDUAL_ID" IS 'forign key to the individual a notification is for';
COMMENT ON COLUMN "INDIV_NOTIF"."NOTIFICATION_ID" IS 'Foreign key to a notification for a particular user';
COMMENT ON COLUMN "INDIV_NOTIF"."NOTIFICATION_STATUS_ID" IS 'Foreign key to the status of a notification message';
COMMENT ON TABLE "INDIV_NOTIF"  IS 'The relationship between an Individual and a Notification';

CREATE TABLE "INSTANCE"
(	"CURRENT_STATE_ID" NUMBER,
"DISPLAY_NOTE" VARCHAR2(4000),
"GATHER_COMMAND" VARCHAR2(4000),
"GATHER_METHOD_NAME" VARCHAR2(256),
"GATHERED_URL" VARCHAR2(1024),
"INSTANCE_DATE" TIMESTAMP (6),
"INSTANCE_ID" NUMBER NOT NULL,
"INSTANCE_STATE_ID" NUMBER,
"INSTANCE_STATUS_ID" NUMBER,
"IS_DISPLAYED" NUMBER,
"PREFIX" VARCHAR2(256),
"PROCESSABLE" NUMBER,
"REMOVEABLE" NUMBER,
"RESOURCE_ID" NUMBER,
"RESTRICTABLE" NUMBER,
"RESTRICTION_ENABLED_T" NUMBER,
"TEP_URL" VARCHAR2(1024),
"TITLE_ID" NUMBER,
"TRANSPORTABLE" NUMBER,
"TYPE_NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "INSTANCE"."CURRENT_STATE_ID" IS 'Foreign key to the state of this instance';
COMMENT ON COLUMN "INSTANCE"."DISPLAY_NOTE" IS 'A note to be displayed with this instance on the Title TEP';
COMMENT ON COLUMN "INSTANCE"."GATHER_METHOD_NAME" IS 'The method used to gather this instance';
COMMENT ON COLUMN "INSTANCE"."GATHERED_URL" IS 'The Seed URL used for this instance';
COMMENT ON COLUMN "INSTANCE"."INSTANCE_DATE" IS 'The date and time at which this instance was gathered';
COMMENT ON COLUMN "INSTANCE"."INSTANCE_ID" IS 'Sequence generated ID for the title instance';
COMMENT ON COLUMN "INSTANCE"."INSTANCE_STATE_ID" IS 'Foreign key to the old Pv2 state for this instance';
COMMENT ON COLUMN "INSTANCE"."INSTANCE_STATUS_ID" IS 'Foreign key to the old Pv2 status for this instance';
COMMENT ON COLUMN "INSTANCE"."IS_DISPLAYED" IS 'Indicated whether this instance is to be displayed on the Title''s TEP or not';
COMMENT ON COLUMN "INSTANCE"."PREFIX" IS 'The folder prefix used when accessing the display version of this instance';
COMMENT ON COLUMN "INSTANCE"."PROCESSABLE" IS 'Whether this instance can be processed (legacy instances may not be processable)';
COMMENT ON COLUMN "INSTANCE"."REMOVEABLE" IS 'Whether this instance can be removed (legacy instances may not be supported for removal by the current framework)';
COMMENT ON COLUMN "INSTANCE"."RESTRICTABLE" IS 'Whether this instance can have restrictions placed on it using the current restricter engine. (Due to different filesystem structures used with pre-pandas archiving, some instances can not be restricted)';
COMMENT ON COLUMN "INSTANCE"."TEP_URL" IS 'The URL to be used on the TEP for this instance';
COMMENT ON COLUMN "INSTANCE"."TITLE_ID" IS 'Foreign key to the title this instance belongs to';
COMMENT ON COLUMN "INSTANCE"."TRANSPORTABLE" IS 'Whether transporting this instance is supported or not. (legacy instances may not be supported for removal by the current framework)';
COMMENT ON COLUMN "INSTANCE"."TYPE_NAME" IS 'Which pandas system created this instance (pre-pandas, pandas 1, etc)';
COMMENT ON TABLE "INSTANCE"  IS 'A snapshot of the online resource, including status, date and summary statistics.';

CREATE TABLE "INSTANCE_MIME"
(	"INSTANCE_ID" NUMBER NOT NULL,
"MIME_TYPE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "INSTANCE_MIME"."INSTANCE_ID" IS 'The instance associated with a particular mime type';
COMMENT ON COLUMN "INSTANCE_MIME"."MIME_TYPE_ID" IS 'The mime type associated with a particular instance';
COMMENT ON TABLE "INSTANCE_MIME"  IS 'The relationship between a title instance and a file mime type';

CREATE TABLE "INSTANCE_OPT_ARG"
(	"INSTANCE_ID" NUMBER NOT NULL,
"INS_OPT_ARG_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "INSTANCE_OPT_ARG"."INSTANCE_ID" IS 'The instance a particular argument was used to gather';
COMMENT ON COLUMN "INSTANCE_OPT_ARG"."INS_OPT_ARG_ID" IS 'The argument used to gather a particular instance';
COMMENT ON TABLE "INSTANCE_OPT_ARG"  IS 'the relationship between an instance and the command line options specified when it was gathered.';

CREATE TABLE "INS_GATHER"
(	"GATHER_FILES" NUMBER(10,0),
"GATHER_FINISH" TIMESTAMP (6),
"GATHER_RATE" NUMBER,
"GATHER_SIZE" NUMBER(10,0),
"GATHER_START" TIMESTAMP (6),
"GATHER_TIME" NUMBER,
"INSTANCE_ID" NUMBER(10,0) NOT NULL
) ;

COMMENT ON COLUMN "INS_GATHER"."GATHER_FILES" IS 'The number of files gathered for this instance';
COMMENT ON COLUMN "INS_GATHER"."GATHER_FINISH" IS 'The date and time this instance finished gathering';
COMMENT ON COLUMN "INS_GATHER"."GATHER_RATE" IS 'The average download rate for this instance''s gather (bytes/s)';
COMMENT ON COLUMN "INS_GATHER"."GATHER_SIZE" IS 'How large the gather for this instance is (bytes)';
COMMENT ON COLUMN "INS_GATHER"."GATHER_START" IS 'The date and time this instance started gathering';
COMMENT ON COLUMN "INS_GATHER"."GATHER_TIME" IS 'How long the gather for this instance took';
COMMENT ON TABLE "INS_GATHER"  IS 'Gather statistics for a particular title instance.';

CREATE TABLE "INS_RESOURCE"
(	"DISPLAY_URL" VARCHAR2(1024),
"GATHERED_URL" VARCHAR2(1024),
"INSTANCE_ID" NUMBER(10,0) NOT NULL,
"LOCAL_URL" VARCHAR2(1024)
) ;

CREATE TABLE "ISSUE_GROUP"
(	"ISSUE_GROUP_ID" NUMBER NOT NULL,
"NAME" VARCHAR2(256),
"NOTES" VARCHAR2(4000),
"ISSUE_GROUP_ORDER" NUMBER,
"TEP_ID" NUMBER
) ;

COMMENT ON COLUMN "ISSUE_GROUP"."ISSUE_GROUP_ID" IS 'Sequence generated ID for an issue group';
COMMENT ON COLUMN "ISSUE_GROUP"."NAME" IS 'The name or label of an issue group';
COMMENT ON COLUMN "ISSUE_GROUP"."NOTES" IS 'Any display notes or description for this group of issues';
COMMENT ON COLUMN "ISSUE_GROUP"."ISSUE_GROUP_ORDER" IS 'The display order for this issue group, compared to other issue groups for a particular title';
COMMENT ON TABLE "ISSUE_GROUP"  IS 'A label under which to group issues (ARCH_ISSUE) on the title''s TEP';

CREATE TABLE "MIME_EXTENSION"
(	"MIME_EXTENSION" VARCHAR2(32),
"MIME_EXTENSION_ID" NUMBER NOT NULL,
"MIME_TYPE_ID" NUMBER
) ;

COMMENT ON COLUMN "MIME_EXTENSION"."MIME_EXTENSION" IS 'A file extension associated with a mime type';
COMMENT ON COLUMN "MIME_EXTENSION"."MIME_EXTENSION_ID" IS 'Sequence generated id for a mime extension';
COMMENT ON COLUMN "MIME_EXTENSION"."MIME_TYPE_ID" IS 'Foreign key to the mime type for this extension';
COMMENT ON TABLE "MIME_EXTENSION"  IS 'The file extension for a particular mime type.';

CREATE TABLE "MIME_TYPE"
(	"DATE_ADDED" TIMESTAMP (6),
"MIME_TYPE_ID" NUMBER NOT NULL,
"NOTE" VARCHAR2(256),
"PARAMETER" VARCHAR2(256),
"SUBTYPE" VARCHAR2(64),
"TYPE" VARCHAR2(64)
) ;

COMMENT ON COLUMN "MIME_TYPE"."DATE_ADDED" IS 'The date this mime type was added';
COMMENT ON COLUMN "MIME_TYPE"."MIME_TYPE_ID" IS 'Sequence generated id for this mime type';
COMMENT ON COLUMN "MIME_TYPE"."NOTE" IS 'Notes about this mime type';
COMMENT ON COLUMN "MIME_TYPE"."PARAMETER" IS 'Extra parameters specified with a mime type. Eg. charset';
COMMENT ON COLUMN "MIME_TYPE"."SUBTYPE" IS 'Mime subtype code (eg xml), listed after the mime type, and a "/", eg. text/xml';
COMMENT ON COLUMN "MIME_TYPE"."TYPE" IS 'Mime type code, eg. text';
COMMENT ON TABLE "MIME_TYPE"  IS 'The code for a particular type of file format';

CREATE TABLE "NOTIFICATION"
(	"CONTENT" CLOB,
"END_DATE" TIMESTAMP (6),
"HEADING" VARCHAR2(256),
"NOTIFICATION_ID" NUMBER NOT NULL,
"START_DATE" TIMESTAMP (6)
) ;

COMMENT ON COLUMN "NOTIFICATION"."CONTENT" IS 'Notification message';
COMMENT ON COLUMN "NOTIFICATION"."END_DATE" IS 'Expiry date for this notification message';
COMMENT ON COLUMN "NOTIFICATION"."HEADING" IS 'The subject of a notification';
COMMENT ON COLUMN "NOTIFICATION"."NOTIFICATION_ID" IS 'Sequence generated id for a notification';
COMMENT ON COLUMN "NOTIFICATION"."START_DATE" IS 'The date on which this notification was sent';
COMMENT ON TABLE "NOTIFICATION"  IS 'A message for a particular system user';

CREATE TABLE "NOTIFICATION_STATUS"
(	"NOTIFICATION_STATUS_ID" NUMBER NOT NULL,
"STATUS" VARCHAR2(128)
) ;

COMMENT ON COLUMN "NOTIFICATION_STATUS"."NOTIFICATION_STATUS_ID" IS 'Sequence generated ID for a notification status';
COMMENT ON COLUMN "NOTIFICATION_STATUS"."STATUS" IS 'A possible status value for notification';
COMMENT ON TABLE "NOTIFICATION_STATUS"  IS 'Lookup table for the status of a notification';

CREATE TABLE "OLD_INSTANCE_STATE"
(	"INSTANCE_STATE_ID" NUMBER NOT NULL,
"INSTANCE_STATE_NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "OLD_INSTANCE_STATE"."INSTANCE_STATE_ID" IS 'Sequence generated ID for an instance state from Pv2';
COMMENT ON COLUMN "OLD_INSTANCE_STATE"."INSTANCE_STATE_NAME" IS 'A possible instance state from Pv2';
COMMENT ON TABLE "OLD_INSTANCE_STATE"  IS 'Lookup table for instance states used in the previous version of PANDAS (version 2).';

CREATE TABLE "OLD_INSTANCE_STATUS"
(	"INSTANCE_STATUS_ID" NUMBER NOT NULL,
"INSTANCE_STATUS_NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "OLD_INSTANCE_STATUS"."INSTANCE_STATUS_ID" IS 'Sequence generated ID for an instance status from Pv2';
COMMENT ON COLUMN "OLD_INSTANCE_STATUS"."INSTANCE_STATUS_NAME" IS 'A possible instance status from Pv2';
COMMENT ON TABLE "OLD_INSTANCE_STATUS"  IS 'Lookup table for instance statuses used in the previous version of PANDAS (version 2).';

CREATE TABLE "OLD_TITLE_STANDING"
(	"TITLE_STANDING_ID" NUMBER NOT NULL,
"TITLE_STANDING_NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "OLD_TITLE_STANDING"."TITLE_STANDING_ID" IS 'Sequence generated ID for a title standing from Pv2';
COMMENT ON COLUMN "OLD_TITLE_STANDING"."TITLE_STANDING_NAME" IS 'A possible title standing from Pv2';
COMMENT ON TABLE "OLD_TITLE_STANDING"  IS 'Lookup table for title standings used in the previous version of PANDAS (version 2).';

CREATE TABLE "OLD_TITLE_STATUS"
(	"TITLE_STATUS_ID" NUMBER NOT NULL,
"TITLE_STATUS_NAME" VARCHAR2(256),
"TITLE_STATUS_NOTES" VARCHAR2(4000)
) ;

COMMENT ON COLUMN "OLD_TITLE_STATUS"."TITLE_STATUS_ID" IS 'Sequence generated ID for a title status from Pv2';
COMMENT ON COLUMN "OLD_TITLE_STATUS"."TITLE_STATUS_NAME" IS 'A possible title status from Pv2';
COMMENT ON COLUMN "OLD_TITLE_STATUS"."TITLE_STATUS_NOTES" IS 'Description of a possible title status from Pv2';
COMMENT ON TABLE "OLD_TITLE_STATUS"  IS 'Lookup table for title statuses used in the previous version of PANDAS (version 2).';

CREATE TABLE "OPTION_ARGUMENT"
(	"ARGUMENT" VARCHAR2(4000),
"ARGUMENT_DESCRIPTION" VARCHAR2(512),
"COMMAND_LINE_OPTION_ID" NUMBER,
"OPTION_ARGUMENT_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "OPTION_ARGUMENT"."ARGUMENT" IS 'The possible argument value for a gather option, or the argument value used or to be used for a particular gather';
COMMENT ON COLUMN "OPTION_ARGUMENT"."ARGUMENT_DESCRIPTION" IS 'A description of a possible argument value for a gather option';
COMMENT ON COLUMN "OPTION_ARGUMENT"."COMMAND_LINE_OPTION_ID" IS 'Foreign key to the command line option this argument can be or was used for';
COMMENT ON COLUMN "OPTION_ARGUMENT"."OPTION_ARGUMENT_ID" IS 'Sequence generated id for a command line argument';
COMMENT ON TABLE "OPTION_ARGUMENT"  IS 'Possible values for particular command line arguments which can be passed to the application doing the gathering, as well as the values actually set for a particular profile/title or values used for a particular instance.';

CREATE TABLE "OPTION_GROUP"
(	"ACCESS_LEVEL" NUMBER,
"OPTION_GROUP_ID" NUMBER NOT NULL,
"DISPLAY_ORDER" NUMBER,
"GROUP_NAME" VARCHAR2(256)
) ;

COMMENT ON COLUMN "OPTION_GROUP"."OPTION_GROUP_ID" IS 'Sequence generated ID for a logical group of command line gather options';
COMMENT ON COLUMN "OPTION_GROUP"."GROUP_NAME" IS 'Label for a logical grouping on command line gather options';
COMMENT ON TABLE "OPTION_GROUP"  IS 'A named set of logically related command line options for gathering';

CREATE TABLE "OPT_ARG_PROFILE"
(	"OPTION_ARGUMENT_ID" NUMBER NOT NULL,
"PROFILE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "OPT_ARG_PROFILE"."OPTION_ARGUMENT_ID" IS 'Foreign key to the argument used for a particular gather profile';
COMMENT ON COLUMN "OPT_ARG_PROFILE"."PROFILE_ID" IS 'Foreign key to the profile associated with a particular argument';
COMMENT ON TABLE "OPT_ARG_PROFILE"  IS 'The relationship between a profile of gather command line options and a profile they belong to.';

CREATE TABLE "ORGANISATION"
(	"ALIAS" VARCHAR2(500),
"AGENCY_ID" NUMBER,
"AUDIT_DATE" TIMESTAMP (6),
"AUDIT_USERID" NUMBER,
"COMMENTS" VARCHAR2(100),
"LONGCOUNTRY" VARCHAR2(100),
"EMAIL" VARCHAR2(100),
"FAX" VARCHAR2(20),
"INDEXER_ID" NUMBER,
"LINE1" VARCHAR2(200),
"LINE2" VARCHAR2(200),
"LOCALITY" VARCHAR2(46),
"MOBILE_PHONE" VARCHAR2(20),
"NAME" VARCHAR2(256),
"ORGANISATION_ID" NUMBER NOT NULL,
"PHONE" VARCHAR2(20),
"POSTCODE" VARCHAR2(10),
"PUBLISHER_ID" NUMBER,
"SERVICE_ID" NUMBER,
"LONGSTATE" VARCHAR2(100),
"URL" VARCHAR2(1024)
) ;

COMMENT ON COLUMN "ORGANISATION"."ALIAS" IS 'Abbreviation or alternate name for an organisation';
COMMENT ON COLUMN "ORGANISATION"."AGENCY_ID" IS 'Foreign key to the partner agency which this set of organisation details belongs to. Mutually exclusive with indexer_id and publisher_id';
COMMENT ON COLUMN "ORGANISATION"."AUDIT_DATE" IS 'The date and time on which this table was last updated';
COMMENT ON COLUMN "ORGANISATION"."AUDIT_USERID" IS 'The user who last updated this table';
COMMENT ON COLUMN "ORGANISATION"."COMMENTS" IS 'Any notes on this organisation';
COMMENT ON COLUMN "ORGANISATION"."LONGCOUNTRY" IS 'The full country name used in this organisation''s primary address';
COMMENT ON COLUMN "ORGANISATION"."EMAIL" IS 'The email address for this organisation';
COMMENT ON COLUMN "ORGANISATION"."FAX" IS 'The fax number for this organisation';
COMMENT ON COLUMN "ORGANISATION"."INDEXER_ID" IS 'Foreign key to the indexing agency which this set of organisation details belongs to. Mutually exclusive with agency_id and publisher_id';
COMMENT ON COLUMN "ORGANISATION"."LINE1" IS 'First line of this organisation''s primary address.';
COMMENT ON COLUMN "ORGANISATION"."LINE2" IS 'Second line of this organisation''s primary address.';
COMMENT ON COLUMN "ORGANISATION"."LOCALITY" IS 'Suburb or town of this organisation''s primary address';
COMMENT ON COLUMN "ORGANISATION"."MOBILE_PHONE" IS 'Mobile phone contact number for this organisation';
COMMENT ON COLUMN "ORGANISATION"."NAME" IS 'The name of this organisation';
COMMENT ON COLUMN "ORGANISATION"."ORGANISATION_ID" IS 'Sequence generated ID for an organisation';
COMMENT ON COLUMN "ORGANISATION"."PHONE" IS 'Phone number for an organisation';
COMMENT ON COLUMN "ORGANISATION"."POSTCODE" IS 'postcode for this organisation''s primary address';
COMMENT ON COLUMN "ORGANISATION"."PUBLISHER_ID" IS 'Foreign key to the publishing agency which this set of organisation details belongs to. Mutually exclusive with agency_id and indexer_id';
COMMENT ON COLUMN "ORGANISATION"."LONGSTATE" IS 'The state for this organisation''s primary address';
COMMENT ON COLUMN "ORGANISATION"."URL" IS 'This organisation''s internet address or webpage';
COMMENT ON TABLE "ORGANISATION"  IS 'An organisation connected to the archive. These could be archive partners, indexing agencies, or publishing organisations responsible for web content.';

CREATE TABLE "OWNER_HISTORY"
(	"AGENCY_ID" NUMBER,
"INDIVIDUAL_ID" NUMBER NOT NULL,
"NOTE" VARCHAR2(4000),
"OWNER_ID" NUMBER NOT NULL,
"OWNERSHIP_DATE" TIMESTAMP (6) NOT NULL,
"TITLE_ID" NUMBER NOT NULL,
"TRANSFERRER_ID" NUMBER
) ;

COMMENT ON COLUMN "OWNER_HISTORY"."AGENCY_ID" IS 'Foreign key to the partner agency the listed user belongs to';
COMMENT ON COLUMN "OWNER_HISTORY"."INDIVIDUAL_ID" IS 'Foreign key to the pandas user who owns or previously owned a particular title';
COMMENT ON COLUMN "OWNER_HISTORY"."NOTE" IS 'A note written by a user who was transferring a title to another user';
COMMENT ON COLUMN "OWNER_HISTORY"."OWNERSHIP_DATE" IS 'The date on which ownership of the title began for this user';
COMMENT ON COLUMN "OWNER_HISTORY"."TITLE_ID" IS 'Foreign key to the title this ownership record refers to';
COMMENT ON COLUMN "OWNER_HISTORY"."TRANSFERRER_ID" IS 'Foreign key to the individual who transferred ownership of this title';
COMMENT ON TABLE "OWNER_HISTORY"  IS 'A log of any change in the system user responsible for the title.';

CREATE TABLE "PANDAS_EXCEPTION_LOG"
(	"EXCEPTION_DATE" TIMESTAMP (6),
"EXCEPTION_DETAIL" VARCHAR2(4000),
"INSTANCE_ID" NUMBER,
"EXCEPTION_ORIGINATOR" VARCHAR2(100),
"EXCEPTION_LOG_ID" NUMBER NOT NULL,
"PI" NUMBER,
"EXCEPTION_SUMMARY" VARCHAR2(4000),
"TITLE_ID" NUMBER,
"EXCEPTION_VIEWED" NUMBER
) ;

CREATE TABLE "PERIOD_RESTR"
(	"AGENCY_AREA_ID" NUMBER,
"CONDITION_DATE" TIMESTAMP (6),
"CONDITION_ID" NUMBER,
"PERIOD_MULTIPLIER" NUMBER,
"PERIOD_RESTRICTION_ID" NUMBER NOT NULL,
"PERIOD_TYPE_ID" NUMBER,
"TITLE_ID" NUMBER
) ;

COMMENT ON COLUMN "PERIOD_RESTR"."AGENCY_AREA_ID" IS 'Foreign key to the location which access to a title is limited to';
COMMENT ON COLUMN "PERIOD_RESTR"."CONDITION_DATE" IS 'The date the current condition (status) for this restriction was applied';
COMMENT ON COLUMN "PERIOD_RESTR"."CONDITION_ID" IS 'Foreign key to the status fo this restriction';
COMMENT ON COLUMN "PERIOD_RESTR"."PERIOD_MULTIPLIER" IS 'How many times the period type must pass after the start date, before this restriction expires';
COMMENT ON COLUMN "PERIOD_RESTR"."PERIOD_RESTRICTION_ID" IS 'Sequence generated id for this period restriction';
COMMENT ON COLUMN "PERIOD_RESTR"."PERIOD_TYPE_ID" IS 'Foreign key to the time period for this restriction';
COMMENT ON COLUMN "PERIOD_RESTR"."TITLE_ID" IS 'Foreign key to the title for this retriction';
COMMENT ON TABLE "PERIOD_RESTR"  IS 'A title display restriction which prevents a title instance from being publically available for a certain period of time after it is gathered.';

CREATE TABLE "PERIOD_TYPE"
(	"PERIOD_TYPE" VARCHAR2(20),
"PERIOD_TYPE_DESCRIPTION" VARCHAR2(1024),
"PERIOD_TYPE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "PERIOD_TYPE"."PERIOD_TYPE" IS 'Time periods available for period restrictions';
COMMENT ON COLUMN "PERIOD_TYPE"."PERIOD_TYPE_DESCRIPTION" IS 'A description of what a period type means or when it should be used';
COMMENT ON COLUMN "PERIOD_TYPE"."PERIOD_TYPE_ID" IS 'Sequence generated id for a period type';
COMMENT ON TABLE "PERIOD_TYPE"  IS 'A lookup table for the intervals of time available when defining a period display restriction. eg. day/s, week/s, ...';

CREATE TABLE "PERMISSION"
(	"DOMAIN" VARCHAR2(4000),
"INDIVIDUAL_ID" NUMBER,
"IS_BLANKET" NUMBER,
"LOCAL_REFERENCE" VARCHAR2(16),
"NOTE" VARCHAR2(4000),
"PERMISSION_DESCRIPTION" VARCHAR2(256),
"PERMISSION_ID" NUMBER NOT NULL,
"PERMISSION_STATE" VARCHAR2(64),
"PERMISSION_STATE_ID" NUMBER,
"PERMISSION_TYPE" VARCHAR2(50),
"PERMISSION_TYPE_ID" NUMBER,
"PUBLISHER_ID" NUMBER,
"STATUS_SET_DATE" TIMESTAMP (6),
"TITLE_ID" NUMBER
) ;

COMMENT ON COLUMN "PERMISSION"."DOMAIN" IS 'The web domain a publisher blanket permission applies to. eg. www.act.com.au';
COMMENT ON COLUMN "PERMISSION"."INDIVIDUAL_ID" IS 'The contact person who granted or denied this permission';
COMMENT ON COLUMN "PERMISSION"."LOCAL_REFERENCE" IS 'The local reference number for files or record pertaining to this permission(within the NLA, this will be a trim file number)';
COMMENT ON COLUMN "PERMISSION"."NOTE" IS 'Any notes or extra conditions for this permission';
COMMENT ON COLUMN "PERMISSION"."PERMISSION_ID" IS 'Sequence generated ID for a permission';
COMMENT ON COLUMN "PERMISSION"."PERMISSION_STATE_ID" IS 'Foreign key to the status of this permission. eg Granted, Denied';
COMMENT ON COLUMN "PERMISSION"."PERMISSION_TYPE_ID" IS 'Foreign key to the type of permission, ie. publisher (blanket) level or title level';
COMMENT ON COLUMN "PERMISSION"."PUBLISHER_ID" IS 'Foreign key to the publisher who has the authority to grant this permission, if this is a blanket permission.';
COMMENT ON COLUMN "PERMISSION"."STATUS_SET_DATE" IS 'The date on which this permission''s status was determined';
COMMENT ON COLUMN "PERMISSION"."TITLE_ID" IS 'Foreign key to the title this permission refers to (if it is a title level permission)';
COMMENT ON TABLE "PERMISSION"  IS 'Information about whether the publisher of a title has granted or denied access to archived versions of that title, or to a group of related titles which they have the rights to.';

CREATE TABLE "PERMISSION_STATE"
(	"PERMISSION_STATE" VARCHAR2(256) NOT NULL,
"PERMISSION_STATE_ID" NUMBER NOT NULL
) ;

COMMENT ON TABLE "PERMISSION_STATE"  IS 'Lookup table for possible permission states. Eg. granted, unknown';

CREATE TABLE "PERMISSION_TYPE"
(	"PERMISSION_TYPE" VARCHAR2(256) NOT NULL,
"PERMISSION_TYPE_ID" NUMBER NOT NULL
) ;

COMMENT ON TABLE "PERMISSION_TYPE"  IS 'Lookup table for the types of permissions available - title level or blanket';

CREATE TABLE "PROFILE"
(	"NAME" VARCHAR2(512) NOT NULL,
"PROFILE_DESCRIPTION" VARCHAR2(2048),
"PROFILE_ID" NUMBER NOT NULL,
"IS_DEFAULT" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "PROFILE"."NAME" IS 'The label for this predefined set of gather options';
COMMENT ON COLUMN "PROFILE"."PROFILE_DESCRIPTION" IS 'A description of the settings stored in this predefined set of gather options';
COMMENT ON COLUMN "PROFILE"."PROFILE_ID" IS 'Sequence generated ID for this gather profile';
COMMENT ON TABLE "PROFILE"  IS 'A named set/template of settings to control crawl behaviour.';

CREATE TABLE "PUBLISHER"
(	"LOCAL_REFERENCE" VARCHAR2(256),
"NOTES" VARCHAR2(4000),
"ORGANISATION_ID" NUMBER NOT NULL,
"PUBLISHER_ID" NUMBER NOT NULL,
"PUBLISHER_TYPE_ID" NUMBER
) ;

COMMENT ON COLUMN "PUBLISHER"."LOCAL_REFERENCE" IS 'The local reference number for this publisher. For the NLA, this will be a TRIM file number.';
COMMENT ON COLUMN "PUBLISHER"."NOTES" IS 'Notes about this publisher';
COMMENT ON COLUMN "PUBLISHER"."ORGANISATION_ID" IS 'Foreign key to the organisation details for this publisher';
COMMENT ON COLUMN "PUBLISHER"."PUBLISHER_ID" IS 'Sequence generated ID for this publisher';
COMMENT ON COLUMN "PUBLISHER"."PUBLISHER_TYPE_ID" IS 'Foreign key to the type of organisation this publisher is';
COMMENT ON TABLE "PUBLISHER"  IS 'An organisation (which may consist of a single person) that holds the copyright to one or more titles.';

CREATE TABLE "PUBLISHER_TYPE"
(	"PUBLISHER_DESCRIPTION" VARCHAR2(4000),
"PUBLISHER_TYPE" VARCHAR2(256) NOT NULL,
"PUBLISHER_TYPE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "PUBLISHER_TYPE"."PUBLISHER_DESCRIPTION" IS 'Description of available publisher types';
COMMENT ON COLUMN "PUBLISHER_TYPE"."PUBLISHER_TYPE" IS 'Name of available publisher types';
COMMENT ON COLUMN "PUBLISHER_TYPE"."PUBLISHER_TYPE_ID" IS 'Sequence generated ID for this publisher type';
COMMENT ON TABLE "PUBLISHER_TYPE"  IS 'Lookup table for the kind of publishing organisations available. eg. government, commercial, ...';

CREATE TABLE "QA_PROBLEM"
(	"AFFECTS_WHOLE_SITE" NUMBER,
"CREATION_DATE" TIMESTAMP (6),
"EXAMPLE_LINK" VARCHAR2(4000),
"EXTERNAL_ID" VARCHAR2(16),
"INDIVIDUAL_ID" NUMBER,
"INSTANCE_ID" NUMBER,
"IS_RECURRING" NUMBER,
"MODIFICATION_DATE" TIMESTAMP (6),
"PROBLEM_DESCRIPTION" VARCHAR2(4000),
"PROBLEM_ID" NUMBER NOT NULL,
"PROBLEM_NAME" VARCHAR2(1024),
"REQUIRES_EXTERNAL_SAVE" NUMBER,
"STATE" VARCHAR2(100)
) ;

COMMENT ON COLUMN "QA_PROBLEM"."AFFECTS_WHOLE_SITE" IS 'Whether or not this problem affects the whole site or just one or two pages';
COMMENT ON COLUMN "QA_PROBLEM"."CREATION_DATE" IS 'The date and time at which the QA problem was first created';
COMMENT ON COLUMN "QA_PROBLEM"."EXAMPLE_LINK" IS 'Links to any examples of the problem';
COMMENT ON COLUMN "QA_PROBLEM"."EXTERNAL_ID" IS 'The ID of the problem in the external QA system';
COMMENT ON COLUMN "QA_PROBLEM"."INDIVIDUAL_ID" IS 'The user who reported the problem';
COMMENT ON COLUMN "QA_PROBLEM"."INSTANCE_ID" IS 'The instance this problem was found in';
COMMENT ON COLUMN "QA_PROBLEM"."IS_RECURRING" IS 'Whether this problem is always/often found in instances of this title';
COMMENT ON COLUMN "QA_PROBLEM"."MODIFICATION_DATE" IS 'The date and time on which this QA problem was last updated';
COMMENT ON COLUMN "QA_PROBLEM"."PROBLEM_DESCRIPTION" IS 'A description of this QA problem';
COMMENT ON COLUMN "QA_PROBLEM"."PROBLEM_ID" IS 'A sequence generated ID for this QA problem';
COMMENT ON COLUMN "QA_PROBLEM"."PROBLEM_NAME" IS 'A short label for this QA problem';
COMMENT ON COLUMN "QA_PROBLEM"."REQUIRES_EXTERNAL_SAVE" IS 'Indicates whether a synchronization is needed between the database values and the version of this problem stored in the external QA system';
COMMENT ON COLUMN "QA_PROBLEM"."STATE" IS 'The current state of this QA problem. eg. closed, open';
COMMENT ON TABLE "QA_PROBLEM"  IS 'A QA problem associated with a title instance which has been identified by a pandas user.';

CREATE TABLE "REASON"
(	"REASON" VARCHAR2(512),
"REASON_ID" NUMBER NOT NULL,
"STATUS_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "REASON"."REASON" IS 'Possible reasons for a title status change';
COMMENT ON COLUMN "REASON"."REASON_ID" IS 'Sequence generated ID for status reasons';
COMMENT ON COLUMN "REASON"."STATUS_ID" IS 'Foreign key to the status this reason may be applied to';
COMMENT ON TABLE "REASON"  IS 'Lookup table for reasons that explain why a title has been set to a particular status.';

CREATE TABLE "REPORT"
(	"REPORT_ID" NUMBER NOT NULL,
"INDIVIDUAL_ID" NUMBER NOT NULL,
"REPORT_TYPE_ID" NUMBER NOT NULL,
"AGENCY_ID" NUMBER,
"PERIOD_START" DATE,
"PERIOD_END" DATE,
"SHOW_DETAILS" NUMBER NOT NULL,
"PUBLISHER_TYPE_ID" NUMBER,
"RESTRICTION_TYPE" NUMBER,
"LAST_GENERATION_DATE" DATE,
"NEXT_GENERATION_DATE" DATE,
"REPORT_SCHEDULE_ID" NUMBER,
"SCHEDULED_DAY" NUMBER,
"IS_VISIBLE" NUMBER NOT NULL,
"ERROR_MSG" VARCHAR2(512)
) ;

COMMENT ON COLUMN "REPORT"."REPORT_ID" IS 'Unique identifier for this report (either a scheduled or a particular ad-hoc report).';
COMMENT ON COLUMN "REPORT"."INDIVIDUAL_ID" IS 'Id of the individual who requested this report.';
COMMENT ON COLUMN "REPORT"."REPORT_TYPE_ID" IS 'Key of the type of report that was selected, see report_type table.';
COMMENT ON COLUMN "REPORT"."AGENCY_ID" IS 'Key of the agency that this report covers.';
COMMENT ON COLUMN "REPORT"."PERIOD_START" IS 'Start date for the period this report covers. Should be null for scheduled reports.';
COMMENT ON COLUMN "REPORT"."PERIOD_END" IS 'End date for the period this report covers. Should be null for scheduled reports.';
COMMENT ON COLUMN "REPORT"."SHOW_DETAILS" IS 'Detail level to be included in the report. 0 = "Numbers only" summary. 1 = full details.';
COMMENT ON COLUMN "REPORT"."PUBLISHER_TYPE_ID" IS 'Publisher type this report should cover. Null if not applicable for this report type.';
COMMENT ON COLUMN "REPORT"."RESTRICTION_TYPE" IS 'Restriction type this report should cover (if applicable). null=N/A, 0=period, 1=date, 2=auth';
COMMENT ON COLUMN "REPORT"."LAST_GENERATION_DATE" IS 'Date this report was last generated.';
COMMENT ON COLUMN "REPORT"."NEXT_GENERATION_DATE" IS 'Date this report should next be generated. Null if the report is not scheduled (ie ad-hoc report).';
COMMENT ON COLUMN "REPORT"."REPORT_SCHEDULE_ID" IS 'Key of the schedule this report is on. Null if the report is not scheduled.';
COMMENT ON COLUMN "REPORT"."SCHEDULED_DAY" IS 'Day of the week or month (depending on schedule_id) that this report should be delivered upon.';
COMMENT ON COLUMN "REPORT"."IS_VISIBLE" IS 'Should this report appear in the user''s report tray? 0=hidden, 1=visible';
COMMENT ON COLUMN "REPORT"."ERROR_MSG" IS 'Error message if the report generation failed';

CREATE TABLE "REPORT_SCHEDULE"
(	"REPORT_SCHEDULE_ID" NUMBER NOT NULL,
"NAME" VARCHAR2(256) NOT NULL
) ;

COMMENT ON COLUMN "REPORT_SCHEDULE"."REPORT_SCHEDULE_ID" IS 'Primary key for report_schedule.';
COMMENT ON COLUMN "REPORT_SCHEDULE"."NAME" IS 'Name of this schedule (''Weekly'', ''Monthly'' etc.)';

CREATE TABLE "REPORT_TYPE"
(	"REPORT_TYPE_ID" NUMBER NOT NULL,
"NAME" VARCHAR2(512) NOT NULL,
"JAVA_CLASS" VARCHAR2(256) NOT NULL,
"HAS_DETAILS" NUMBER DEFAULT 0 NOT NULL,
"HAS_PERIOD" NUMBER DEFAULT 0 NOT NULL,
"HAS_AGENCY" NUMBER DEFAULT 0 NOT NULL,
"HAS_PUBLISHER_TYPE" NUMBER DEFAULT 0 NOT NULL,
"HAS_RESTRICTION_TYPE" NUMBER DEFAULT 0 NOT NULL
) ;

COMMENT ON COLUMN "REPORT_TYPE"."REPORT_TYPE_ID" IS 'Primary key for report_type';
COMMENT ON COLUMN "REPORT_TYPE"."NAME" IS 'A friendly name for this report type.';
COMMENT ON COLUMN "REPORT_TYPE"."JAVA_CLASS" IS 'The java class that implements this report type. eg ''au.gov.nla.webarchive.reports.ArchiveStatisticsReport''';
COMMENT ON COLUMN "REPORT_TYPE"."HAS_DETAILS" IS '1 if this report has both "show details" and "numbers only" modes.';
COMMENT ON COLUMN "REPORT_TYPE"."HAS_PERIOD" IS '1 if this report requires a period to be specified, else 0.';
COMMENT ON COLUMN "REPORT_TYPE"."HAS_AGENCY" IS '1 if this report requires an agency (or all agencies) to be specified, else 0.';
COMMENT ON COLUMN "REPORT_TYPE"."HAS_PUBLISHER_TYPE" IS '1 if this report requires a publisher type, else 0.';
COMMENT ON COLUMN "REPORT_TYPE"."HAS_RESTRICTION_TYPE" IS '1 if this report requires a restriction type, else 0.';

CREATE TABLE "ROLE"
(	"AUDIT_CREATE_DATE" TIMESTAMP (6),
"AUDIT_DATE" TIMESTAMP (6),
"AUDIT_USERID" NUMBER,
"COMMENTS" VARCHAR2(200),
"INDIVIDUAL_ID" NUMBER NOT NULL,
"ORGANISATION_ID" NUMBER NOT NULL,
"ROLE_ID" NUMBER NOT NULL,
"ROLE_TITLE" VARCHAR2(100) NOT NULL,
"ROLE_TYPE" VARCHAR2(20) NOT NULL
) ;

COMMENT ON COLUMN "ROLE"."AUDIT_CREATE_DATE" IS 'The date and time at which this role was created';
COMMENT ON COLUMN "ROLE"."AUDIT_DATE" IS 'The date and time at which this role was last modified';
COMMENT ON COLUMN "ROLE"."AUDIT_USERID" IS 'The user who last modified this role';
COMMENT ON COLUMN "ROLE"."COMMENTS" IS 'Any notes about this role';
COMMENT ON COLUMN "ROLE"."INDIVIDUAL_ID" IS 'Foreign key to the individual who performs this role';
COMMENT ON COLUMN "ROLE"."ORGANISATION_ID" IS 'Foreign key to the organisation the user performs this role in';
COMMENT ON COLUMN "ROLE"."ROLE_ID" IS 'Sequence generated ID for this role';
COMMENT ON COLUMN "ROLE"."ROLE_TITLE" IS 'The name of this role';
COMMENT ON TABLE "ROLE"  IS 'The role a particular individual plays in an organisation.';

CREATE TABLE "STATE"
(	"STATE_ID" NUMBER NOT NULL,
"STATE_NAME" VARCHAR2(64)
) ;

COMMENT ON COLUMN "STATE"."STATE_ID" IS 'Sequence generated ID for an instance state';
COMMENT ON COLUMN "STATE"."STATE_NAME" IS 'Possible instance states';
COMMENT ON TABLE "STATE"  IS 'A lookup table for the values available for instance state. Eg. gathered, deleted, ...';

CREATE TABLE "STATE_HISTORY"
(	"END_DATE" TIMESTAMP (6),
"INDIVIDUAL_ID" NUMBER,
"INSTANCE_ID" NUMBER,
"START_DATE" TIMESTAMP (6),
"STATE_HISTORY_ID" NUMBER NOT NULL,
"STATE_ID" NUMBER
) ;

COMMENT ON COLUMN "STATE_HISTORY"."END_DATE" IS 'The date and time this state stopped being the current one for an instance';
COMMENT ON COLUMN "STATE_HISTORY"."INDIVIDUAL_ID" IS 'Foreign key to the individual who caused this state to be applied';
COMMENT ON COLUMN "STATE_HISTORY"."INSTANCE_ID" IS 'Foreign key to the instance this state applies or once applied to';
COMMENT ON COLUMN "STATE_HISTORY"."STATE_HISTORY_ID" IS 'Sequence generated id for this status history entry';
COMMENT ON COLUMN "STATE_HISTORY"."STATE_ID" IS 'Foreign key to the state for this status history entry ';
COMMENT ON TABLE "STATE_HISTORY"  IS 'The state a particular instance is in, or was previously in.';

CREATE TABLE "STATUS"
(	"STATUS_ID" NUMBER NOT NULL,
"STATUS_NAME" VARCHAR2(128)
) ;

COMMENT ON COLUMN "STATUS"."STATUS_ID" IS 'Sequence generated ID for this title status';
COMMENT ON COLUMN "STATUS"."STATUS_NAME" IS 'The name or title of this title status';
COMMENT ON TABLE "STATUS"  IS 'A lookup table for the values available for title status. Eg. selected, nominated, ...';

CREATE TABLE "STATUS_HISTORY"
(	"END_DATE" TIMESTAMP (6),
"INDIVIDUAL_ID" NUMBER,
"REASON_ID" NUMBER,
"START_DATE" TIMESTAMP (6),
"STATUS_HISTORY_ID" NUMBER NOT NULL,
"STATUS_ID" NUMBER,
"TITLE_ID" NUMBER
) ;

COMMENT ON COLUMN "STATUS_HISTORY"."END_DATE" IS 'The date and time this status stopped being the current one for a title';
COMMENT ON COLUMN "STATUS_HISTORY"."REASON_ID" IS 'Foreign key to a reason why this status was applied';
COMMENT ON COLUMN "STATUS_HISTORY"."STATUS_HISTORY_ID" IS 'Sequence generated ID for this status history entry';
COMMENT ON COLUMN "STATUS_HISTORY"."STATUS_ID" IS 'Foreign key to the status for this entry';
COMMENT ON COLUMN "STATUS_HISTORY"."TITLE_ID" IS 'Foreign key to the title this status applies or applied to';
COMMENT ON TABLE "STATUS_HISTORY"  IS 'A log of any change in the status of a title, as well as the title''s current status value.';

CREATE TABLE "SUBJECT"
(	"SUBJECT_ID" NUMBER NOT NULL,
"SUBJECT_NAME" VARCHAR2(256) NOT NULL,
"SUBJECT_PARENT_ID" NUMBER
) ;

COMMENT ON COLUMN "SUBJECT"."SUBJECT_ID" IS 'Sequence generated ID for this subject';
COMMENT ON COLUMN "SUBJECT"."SUBJECT_NAME" IS 'The name of or label for this subject';
COMMENT ON COLUMN "SUBJECT"."SUBJECT_PARENT_ID" IS 'Foreign key to the subject which this subject is a child of';
COMMENT ON TABLE "SUBJECT"  IS 'A lookup table of topics that a title or a collection of titles may belong to.';

CREATE TABLE "SUBJECT_TITLES"
(	"SUBJECT_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "SUBJECT_TITLES"."SUBJECT_ID" IS 'Foreign key to a subject this title belongs to';
COMMENT ON COLUMN "SUBJECT_TITLES"."TITLE_ID" IS 'Foreign key to a title this subject contains';
COMMENT ON TABLE "SUBJECT_TITLES"  IS 'The relationship between a title and a subject (many titles can belong to a subject, and many subjects can belong to a title).';

CREATE TABLE "TEP"
(	"COPYRIGHT_NOTE" VARCHAR2(4000),
"COPYRIGHT_TYPE_ID" NUMBER,
"COPYRIGHT_URL" VARCHAR2(1024),
"DISPLAY_DATE" TIMESTAMP (6),
"DISPLAY_TITLE" VARCHAR2(4000),
"DO_COLLECTION" NUMBER,
"DO_SEARCH" NUMBER,
"DO_SUBJECT" NUMBER,
"GENERAL_NOTE" VARCHAR2(4000),
"HAS_COPYRIGHT" NUMBER,
"HAS_DISCLAIMER" NUMBER,
"HIT_COUNT" NUMBER,
"IS_PUBLISHED" NUMBER,
"METADATA_OLD" VARCHAR2(4000),
"TEP_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER NOT NULL,
"METADATA" CLOB
) ;

COMMENT ON COLUMN "TEP"."COPYRIGHT_NOTE" IS 'Copyright and/or disclaimer for the publisher wishes to appear for this title';
COMMENT ON COLUMN "TEP"."COPYRIGHT_URL" IS 'URL to a copyright statement or disclaimer for this title';
COMMENT ON COLUMN "TEP"."DISPLAY_TITLE" IS 'The title/heading to be displayed on the TEP';
COMMENT ON COLUMN "TEP"."DO_COLLECTION" IS 'Indicates whether the display system should list this title in any collections it belongs to';
COMMENT ON COLUMN "TEP"."DO_SEARCH" IS 'Indicates whether the display system should list this title in search results';
COMMENT ON COLUMN "TEP"."DO_SUBJECT" IS 'Indicates whether the display system should list this title under any subjects it belongs to';
COMMENT ON COLUMN "TEP"."GENERAL_NOTE" IS 'A note to be displayed on this TEP';
COMMENT ON COLUMN "TEP"."HAS_COPYRIGHT" IS 'Indicates whether the statement for this TEP is a copyright statement. (can also be a disclaimer)';
COMMENT ON COLUMN "TEP"."HAS_DISCLAIMER" IS 'Indicates whether the statement for this TEP is a disclaimer (can also be a copyright statement)';
COMMENT ON COLUMN "TEP"."HIT_COUNT" IS 'The number of times this TEP has been shown in the display system';
COMMENT ON COLUMN "TEP"."IS_PUBLISHED" IS 'Indicated whether the TEP is to appear in the display system at all';
COMMENT ON COLUMN "TEP"."METADATA_OLD" IS 'Metadata to be placed in the header of the TEP';
COMMENT ON COLUMN "TEP"."TEP_ID" IS 'Sequence generated ID for this TEP';
COMMENT ON COLUMN "TEP"."TITLE_ID" IS 'Foreign key to the title which this TEP displays';
COMMENT ON TABLE "TEP"  IS 'Information about, or to be displayed on, a Title Entry Page (TEP)';

CREATE TABLE "TITLE"
(	"AGENCY_ID" NUMBER,
"ANBD_NUMBER" VARCHAR2(22),
"AWAITING_CONFIRMATION" NUMBER,
"CONTENT_WARNING" VARCHAR2(256),
"CURRENT_OWNER_ID" NUMBER,
"CURRENT_STATUS_ID" NUMBER,
"DEFAULT_PERMISSION_ID" NUMBER,
"DISAPPEARED" NUMBER,
"FORMAT_ID" NUMBER,
"INDEXER_ID" NUMBER,
"IS_CATALOGUING_NOT_REQ" NUMBER,
"IS_SUBSCRIPTION" NUMBER,
"LEGACY_PURL" VARCHAR2(1024),
"LOCAL_DATABASE_NO" VARCHAR2(25),
"LOCAL_REFERENCE" VARCHAR2(25),
"NAME" VARCHAR2(256),
"NOTES" VARCHAR2(4000),
"PERMISSION_ID" NUMBER,
"PI" NUMBER,
"PUBLISHER_ID" NUMBER,
"REG_DATE" TIMESTAMP (6),
"SEED_URL" VARCHAR2(1024),
"SHORT_DISPLAY_NAME" VARCHAR2(256),
"TEP_ID" NUMBER,
"TITLE_ID" NUMBER NOT NULL,
"TITLE_RESOURCE_ID" NUMBER,
"STANDING_ID" NUMBER,
"STATUS_ID" NUMBER,
"TITLE_URL" VARCHAR2(1024),
"UNABLE_TO_ARCHIVE" NUMBER
) ;

COMMENT ON COLUMN "TITLE"."ANBD_NUMBER" IS 'Australian National Bibliographic Database catalogue record identifier for this archived resource';
COMMENT ON COLUMN "TITLE"."AWAITING_CONFIRMATION" IS 'Flags whether this title is waiting to be acknowledged after a transfer of ownership';
COMMENT ON COLUMN "TITLE"."CONTENT_WARNING" IS 'Notes about any offensive content within this title';
COMMENT ON COLUMN "TITLE"."CURRENT_OWNER_ID" IS 'Foreign key to the record storing who is currently assigned as this title''s owner';
COMMENT ON COLUMN "TITLE"."CURRENT_STATUS_ID" IS 'Foreign key to the current status history entry for this title';
COMMENT ON COLUMN "TITLE"."DEFAULT_PERMISSION_ID" IS 'Foreign key to the title level permission for this title';
COMMENT ON COLUMN "TITLE"."FORMAT_ID" IS 'Foreign key to the lookup table indicating which format this title is in eg. integrating, serial';
COMMENT ON COLUMN "TITLE"."INDEXER_ID" IS 'Foreign key to the indexing agency which asked for this title to be nominated';
COMMENT ON COLUMN "TITLE"."IS_CATALOGUING_NOT_REQ" IS 'Flags whether cataloguing is required for this title or not';
COMMENT ON COLUMN "TITLE"."IS_SUBSCRIPTION" IS 'Indicates whether this title must be subscribed to before it can be accessesd';
COMMENT ON COLUMN "TITLE"."LEGACY_PURL" IS 'PURLs which were stored for titles in a previous version of the system, no longer added or edited but need to be stored to maintain their persistence';
COMMENT ON COLUMN "TITLE"."LOCAL_DATABASE_NO" IS 'An agency specific database number for this title. At the NLA, this is a Voyager database number.';
COMMENT ON COLUMN "TITLE"."LOCAL_REFERENCE" IS 'An agency specific reference number for this title. At the NLA, this is a TRIM number.';
COMMENT ON COLUMN "TITLE"."NAME" IS 'The name or heading of this title';
COMMENT ON COLUMN "TITLE"."NOTES" IS 'Any notes about this title';
COMMENT ON COLUMN "TITLE"."PERMISSION_ID" IS 'Foreign key to the active permission for this title. The active permission may be a title level permission or a publisher blanket permission';
COMMENT ON COLUMN "TITLE"."PI" IS 'Persistant Identifier used for referencing archived copies of this online resource';
COMMENT ON COLUMN "TITLE"."PUBLISHER_ID" IS 'Foreign key to the publishing agency who hold the copyright for this title';
COMMENT ON COLUMN "TITLE"."REG_DATE" IS 'The date this title was created';
COMMENT ON COLUMN "TITLE"."SEED_URL" IS 'The URL which will be used to gather this title';
COMMENT ON COLUMN "TITLE"."SHORT_DISPLAY_NAME" IS 'Shortened name used for display in worktrays';
COMMENT ON COLUMN "TITLE"."TEP_ID" IS 'Foreign key to the Title Entry Page (TEP) for this title, which will be used in the display system';
COMMENT ON COLUMN "TITLE"."TITLE_ID" IS 'Sequence generated ID for this title';
COMMENT ON COLUMN "TITLE"."STANDING_ID" IS 'Foreign key to the old Pv2 standing for this title';
COMMENT ON COLUMN "TITLE"."STATUS_ID" IS 'Foreign key to the old Pv2 status of this title';
COMMENT ON COLUMN "TITLE"."TITLE_URL" IS 'URL for this resource on the live web';
COMMENT ON TABLE "TITLE"  IS 'An online resource selected for archiving';

CREATE TABLE "TITLE_COL"
(	"COLLECTION_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "TITLE_COL"."COLLECTION_ID" IS 'Foreign key to a title related to this collection';
COMMENT ON COLUMN "TITLE_COL"."TITLE_ID" IS 'Foreign key to a collection related to this title';
COMMENT ON TABLE "TITLE_COL"  IS 'The relationship between a title and a collection';

CREATE TABLE "TITLE_GATHER"
(	"ACTIVE_PROFILE_ID" NUMBER,
"ADDITIONAL_URLS" VARCHAR2(4000),
"AUTHENTICATE_IP" NUMBER,
"AUTHENTICATE_USER" NUMBER,
"CAL_START_DATE" TIMESTAMP (6),
"FIRST_GATHER_DATE" TIMESTAMP (6),
"GATHER_METHOD_ID" NUMBER(10,0),
"GATHER_SCHEDULE_ID" NUMBER(10,0),
"GATHER_URL" VARCHAR2(4000),
"LAST_GATHER_DATE" TIMESTAMP (6),
"NEXT_GATHER_DATE" TIMESTAMP (6),
"NOTES" VARCHAR2(4000),
"PASSWORD" VARCHAR2(128),
"QUEUED" NUMBER,
"IS_SCHEDULED" NUMBER,
"SCHEDULED_DATE" TIMESTAMP (6),
"TITLE_ID" NUMBER NOT NULL,
"USERNAME" VARCHAR2(128),
"GATHER_COMMAND" VARCHAR2(4000)
) ;

COMMENT ON COLUMN "TITLE_GATHER"."ACTIVE_PROFILE_ID" IS 'Foreign key to the gather profile currently applied to this title (if any)';
COMMENT ON COLUMN "TITLE_GATHER"."FIRST_GATHER_DATE" IS 'The date on which this title was first gathered';
COMMENT ON COLUMN "TITLE_GATHER"."GATHER_METHOD_ID" IS 'Foreign key to the method used to gather this title';
COMMENT ON COLUMN "TITLE_GATHER"."GATHER_SCHEDULE_ID" IS 'Foreign key to the frequency of recurring gathers for this title';
COMMENT ON COLUMN "TITLE_GATHER"."LAST_GATHER_DATE" IS 'The most recent date on which this title was gathered';
COMMENT ON COLUMN "TITLE_GATHER"."NEXT_GATHER_DATE" IS 'The next date on which this title will be gathered (could be a recurring or non-recurring gather)';
COMMENT ON COLUMN "TITLE_GATHER"."NOTES" IS 'Notes about gathering this title';
COMMENT ON COLUMN "TITLE_GATHER"."IS_SCHEDULED" IS 'Controls whether the title gather will take place or not';
COMMENT ON COLUMN "TITLE_GATHER"."SCHEDULED_DATE" IS 'The next recurring gather date for this title';
COMMENT ON COLUMN "TITLE_GATHER"."TITLE_ID" IS 'Foreign key to the title these gather details are for';
COMMENT ON TABLE "TITLE_GATHER"  IS 'Information about the gather settings and options for a title';

CREATE TABLE "TITLE_HISTORY"
(	"CEASED_ID" NUMBER,
"CONTINUES_ID" NUMBER,
"DATE_CHANGED" TIMESTAMP (6),
"TITLE_HISTORY_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "TITLE_HISTORY"."CEASED_ID" IS 'Foreign key to the title which has been replaced by a new (continuing) title';
COMMENT ON COLUMN "TITLE_HISTORY"."CONTINUES_ID" IS 'Foreign key to the title which took over from a ceased title';
COMMENT ON COLUMN "TITLE_HISTORY"."DATE_CHANGED" IS 'The date the old title was replaced by the new title';
COMMENT ON COLUMN "TITLE_HISTORY"."TITLE_HISTORY_ID" IS 'Sequence generated ID for a record of historicly used titles';
COMMENT ON TABLE "TITLE_HISTORY"  IS 'A relationship between two "serial" format titles where one has replaced the other.';

CREATE TABLE "TITLE_INDIVIDUAL"
(	"INDIVIDUAL_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "TITLE_INDIVIDUAL"."INDIVIDUAL_ID" IS 'Foreign key to a contact person for this title';
COMMENT ON COLUMN "TITLE_INDIVIDUAL"."TITLE_ID" IS 'Foreign key to a title related to this contact person';
COMMENT ON TABLE "TITLE_INDIVIDUAL"  IS 'A many to many relationship between a title and its associated contact people.';

CREATE TABLE "TITLE_PAR_CHILD"
(	"CHILD_ID" NUMBER NOT NULL,
"PARENT_ID" NUMBER NOT NULL,
"RELATED_DATE" TIMESTAMP (6),
"TITLE_PAR_CHILD_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "TITLE_PAR_CHILD"."CHILD_ID" IS 'Foreign key to a child title of this parent title';
COMMENT ON COLUMN "TITLE_PAR_CHILD"."PARENT_ID" IS 'Foreign key to the parent title for this child title';
COMMENT ON COLUMN "TITLE_PAR_CHILD"."RELATED_DATE" IS 'The date the parent child relationship was created';
COMMENT ON COLUMN "TITLE_PAR_CHILD"."TITLE_PAR_CHILD_ID" IS 'Sequence generated ID for the child parent title relationship';
COMMENT ON TABLE "TITLE_PAR_CHILD"  IS 'The relationship between two titles where one is the parent of the other.';

CREATE TABLE "TITLE_PREVIOUS_NAME"
(	"DATE_CHANGED" TIMESTAMP (6),
"PREVIOUS_NAME" VARCHAR2(256),
"TITLE_HISTORY_ID" NUMBER NOT NULL,
"TITLE_ID" NUMBER
) ;

COMMENT ON COLUMN "TITLE_PREVIOUS_NAME"."DATE_CHANGED" IS 'The date the previous name stopped being current';
COMMENT ON COLUMN "TITLE_PREVIOUS_NAME"."PREVIOUS_NAME" IS 'A name which used to be used for a title, but has now been changed';
COMMENT ON COLUMN "TITLE_PREVIOUS_NAME"."TITLE_HISTORY_ID" IS 'Sequence generated ID for the previous title name';
COMMENT ON COLUMN "TITLE_PREVIOUS_NAME"."TITLE_ID" IS 'Foreign key to the title this previous name is for';
COMMENT ON TABLE "TITLE_PREVIOUS_NAME"  IS 'The name an "integrating" format title was previously known as';

CREATE TABLE "T_GATHER_ARG"
(	"OPTION_ARGUMENT_ID" NUMBER NOT NULL,
"TITLE_GATHER_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "T_GATHER_ARG"."OPTION_ARGUMENT_ID" IS 'An option argument related to this title gather';
COMMENT ON COLUMN "T_GATHER_ARG"."TITLE_GATHER_ID" IS 'A title gather related to this option argument';
COMMENT ON TABLE "T_GATHER_ARG"  IS 'The gather options used for gathering a particular title';

CREATE TABLE "UNSENT_EMAIL"
(	"BCC" VARCHAR2(4000),
"CC" VARCHAR2(4000),
"MESSAGE" CLOB,
"RECIPIENT" VARCHAR2(4000),
"REPLY_TO" VARCHAR2(4000),
"SENDER" VARCHAR2(256),
"SUBJECT" VARCHAR2(256),
"UNSENT_EMAIL_ID" NUMBER NOT NULL
) ;

COMMENT ON COLUMN "UNSENT_EMAIL"."BCC" IS 'BCC (blind carbon copy) email addresses for an unsent email';
COMMENT ON COLUMN "UNSENT_EMAIL"."CC" IS 'CC (carbon copy) email addresses for an unsent email';
COMMENT ON COLUMN "UNSENT_EMAIL"."MESSAGE" IS 'The body of an unsent email';
COMMENT ON COLUMN "UNSENT_EMAIL"."RECIPIENT" IS 'The recipient email address of an unsent email';
COMMENT ON COLUMN "UNSENT_EMAIL"."REPLY_TO" IS 'The email address replies to this email should be addressed to';
COMMENT ON COLUMN "UNSENT_EMAIL"."SENDER" IS 'The sender of this email';
COMMENT ON COLUMN "UNSENT_EMAIL"."SUBJECT" IS 'The subject of this email';
COMMENT ON COLUMN "UNSENT_EMAIL"."UNSENT_EMAIL_ID" IS 'Sequence generated ID for this email record';
COMMENT ON TABLE "UNSENT_EMAIL"  IS 'Messages/Notifications which were not able to be sent';

CREATE OR REPLACE FORCE VIEW "AUTH_USER_GROUP_VIEW" ("GROUP_NAME", "USERNAME", "PASSWORD") AS
SELECT g.group_name, u.username, u.password FROM auth_group g, auth_user u WHERE g.auth_group_id = u.auth_group_id;

CREATE INDEX "FK_TITLEPARCHILD_CHILD" ON "TITLE_PAR_CHILD" ("CHILD_ID")
;

CREATE INDEX "FK_PERMISSION_TITLE" ON "PERMISSION" ("TITLE_ID")
;

CREATE INDEX "FK_INSTANCE_TITLE" ON "INSTANCE" ("TITLE_ID")
;

CREATE INDEX "FK_AGENCY_ORGANISATION" ON "AGENCY" ("ORGANISATION_ID")
;

CREATE INDEX "FK_PERIODRESTR_CONDITION" ON "PERIOD_RESTR" ("CONDITION_ID")
;

CREATE INDEX "FK_COLSUBS_SUBJECT" ON "COL_SUBS" ("SUBJECT_ID")
;

CREATE INDEX "FK_DATERESTR_AGENCYAREA" ON "DATE_RESTR" ("AGENCY_AREA_ID")
;

CREATE INDEX "FK_PERMISSION_PERMISSIONTYPE" ON "PERMISSION" ("PERMISSION_TYPE_ID")
;

CREATE INDEX "FK_CONTACT_INDIVIDUAL" ON "CONTACT" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_INDIVNOTIF_NOTIFICATION" ON "INDIV_NOTIF" ("NOTIFICATION_ID")
;

CREATE UNIQUE INDEX "STATE_NAME_IDX" ON "STATE" ("STATE_NAME")
;

CREATE INDEX "FK_SUBJECTTITLES_TITLE" ON "SUBJECT_TITLES" ("TITLE_ID")
;

CREATE INDEX "FK_ORGANISATION_PUBLISHER" ON "ORGANISATION" ("PUBLISHER_ID")
;

CREATE INDEX "FK_TITLE_AGENCY" ON "TITLE" ("AGENCY_ID")
;

CREATE INDEX "FK_ORGANISATION_AGENCY" ON "ORGANISATION" ("AGENCY_ID")
;

CREATE INDEX "FK_TITLEGATHER_ACTIVEPROFILE" ON "TITLE_GATHER" ("ACTIVE_PROFILE_ID")
;

CREATE INDEX "FK_TEP_COPYRIGHTTYPE" ON "TEP" ("COPYRIGHT_TYPE_ID")
;

CREATE INDEX "FK_ARCHISSUE_INSTANCE" ON "ARCH_ISSUE" ("INSTANCE_ID")
;

CREATE INDEX "FK_OPTARGPROFILE_PROFILE" ON "OPT_ARG_PROFILE" ("PROFILE_ID")
;

CREATE INDEX "FK_INDIVNOTIF_NOTIFSTAT" ON "INDIV_NOTIF" ("NOTIFICATION_STATUS_ID")
;

CREATE INDEX "FK_INSTANCE_INSTANCESTATE" ON "INSTANCE" ("INSTANCE_STATE_ID")
;

CREATE INDEX "FK_TGATHERARG_TITLEGATHER" ON "T_GATHER_ARG" ("TITLE_GATHER_ID")
;

CREATE INDEX "FK_OWNERHISTORY_TRANSFERRER" ON "OWNER_HISTORY" ("TRANSFERRER_ID")
;

CREATE INDEX "FK_TITLEINDIVIDUAL_TITLE" ON "TITLE_INDIVIDUAL" ("TITLE_ID")
;

CREATE INDEX "FK_INDEXER_ORGANISATION" ON "INDEXER" ("ORGANISATION_ID")
;

CREATE UNIQUE INDEX "REPORT_TYPE_PK" ON "REPORT_TYPE" ("REPORT_TYPE_ID")
;

CREATE INDEX "FK_AUTHRESTR_CONDITION" ON "AUTH_RESTR" ("CONDITION_ID")
;

CREATE INDEX "FK_PERIODRESTR_PERIODTYPE" ON "PERIOD_RESTR" ("PERIOD_TYPE_ID")
;

CREATE INDEX "FK_TITLECOL_TITLE" ON "TITLE_COL" ("TITLE_ID")
;

CREATE INDEX "FK_PERIODRESTR_AGENCYAREA" ON "PERIOD_RESTR" ("AGENCY_AREA_ID")
;

CREATE INDEX "FK_STATEHISTORY_INSTANCE" ON "STATE_HISTORY" ("INSTANCE_ID")
;

CREATE INDEX "FK_STATUSHISTORY_TITLE" ON "STATUS_HISTORY" ("TITLE_ID")
;

CREATE INDEX "FK_STATEHISTORY_INDIVIDUAL" ON "STATE_HISTORY" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_ARCHISSUE_ISSUEGROUP" ON "ARCH_ISSUE" ("ISSUE_GROUP_ID")
;

CREATE INDEX "FK_INSTANCE_CURRENTSTATE" ON "INSTANCE" ("CURRENT_STATE_ID")
;

CREATE INDEX "FK_TITLEHISTORY_CEASED" ON "TITLE_HISTORY" ("CEASED_ID")
;

CREATE INDEX "FK_CONTACT_CONTACTMETHOD" ON "CONTACT" ("CONTACT_METHOD_ID")
;

CREATE INDEX "FK_ACTOR_INDIVIDUAL" ON "ACTOR" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_INSTANCEMIME_MIMETYPE" ON "INSTANCE_MIME" ("MIME_TYPE_ID")
;

CREATE INDEX "FK_AGENCYAREA_AGENCY" ON "AGENCY_AREA" ("AGENCY_ID")
;

CREATE INDEX "FK_INSTANCEOPTARG_INSOPTARG" ON "INSTANCE_OPT_ARG" ("INS_OPT_ARG_ID")
;

CREATE INDEX "FK_AUTHRESGROUP_AUTHRESTRICT" ON "AUTH_RES_GROUP" ("AUTH_RESTRICT_ID")
;

CREATE INDEX "FK_CONTACT_INDEXER" ON "CONTACT" ("INDEXER_ID")
;

CREATE INDEX "FK_OWNERHISTORY_INDIVIDUAL" ON "OWNER_HISTORY" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_ROLE_ORGANISATION" ON "ROLE" ("ORGANISATION_ID")
;

CREATE INDEX "FK_QAPROBLEM_INDIVIDUAL" ON "QA_PROBLEM" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_STATUSHISTORY_STATUS" ON "STATUS_HISTORY" ("STATUS_ID")
;

CREATE INDEX "FK_TITLEPARCHILD_PARENT" ON "TITLE_PAR_CHILD" ("PARENT_ID")
;

CREATE INDEX "FK_PUBLISHER_ORGANISATION" ON "PUBLISHER" ("ORGANISATION_ID")
;

CREATE INDEX "FK_OWNERHISTORY_TITLE" ON "OWNER_HISTORY" ("TITLE_ID")
;

CREATE INDEX "FK_TITLE_PERMISSION" ON "TITLE" ("PERMISSION_ID")
;

CREATE INDEX "FK_TEP_TITLE" ON "TEP" ("TITLE_ID")
;

CREATE INDEX "FK_COL_COLPARENT" ON "COL" ("COL_PARENT_ID")
;

CREATE INDEX "FK_TITLE_PUBLISHER" ON "TITLE" ("PUBLISHER_ID")
;

CREATE INDEX "FK_STATUSHISTORY_REASON" ON "STATUS_HISTORY" ("REASON_ID")
;

CREATE INDEX "FK_TITLE_INDEXER" ON "TITLE" ("INDEXER_ID")
;

CREATE INDEX "FK_AGENCYAREAIP_AGENCYAREA" ON "AGENCY_AREA_IP" ("AGENCY_AREA_ID")
;

CREATE INDEX "FK_CONTACT_TITLE" ON "CONTACT" ("TITLE_ID")
;

CREATE INDEX "FK_QAPROBLEM_INSTANCE" ON "QA_PROBLEM" ("INSTANCE_ID")
;

CREATE INDEX "FK_DATERESTR_CONDITION" ON "DATE_RESTR" ("CONDITION_ID")
;

CREATE INDEX "FK_ORGANISATION_INDEXER" ON "ORGANISATION" ("INDEXER_ID")
;

CREATE INDEX "FK_PERMISSION_INDIVIDUAL" ON "PERMISSION" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_CONTACT_USER" ON "CONTACT" ("USER_ID")
;

CREATE INDEX "FK_STATEHISTORY_STATE" ON "STATE_HISTORY" ("STATE_ID")
;

CREATE UNIQUE INDEX "REPORT_SCHEDULE_PK" ON "REPORT_SCHEDULE" ("REPORT_SCHEDULE_ID")
;

CREATE INDEX "FK_TITLEHISTORY_CONTINUES" ON "TITLE_HISTORY" ("CONTINUES_ID")
;

CREATE INDEX "FK_REASON_STATUS" ON "REASON" ("STATUS_ID")
;

CREATE INDEX "FK_ISSUEGROUP_TEP" ON "ISSUE_GROUP" ("TEP_ID")
;

CREATE INDEX "FK_APPMIMETYPE_MIMETYPE" ON "APP_MIME_TYPE" ("MIME_TYPE_ID")
;

CREATE INDEX "FK_STATUSHISTORY_INDIVIDUAL" ON "STATUS_HISTORY" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_CONTACT_PUBLISHER" ON "CONTACT" ("PUBLISHER_ID")
;

CREATE INDEX "FK_INSTANCE_INSTANCESTATUS" ON "INSTANCE" ("INSTANCE_STATUS_ID")
;

CREATE INDEX "FK_CONTACT_CONTACTTYPE" ON "CONTACT" ("CONTACT_TYPE_ID")
;

CREATE INDEX "FK_PUBLISHER_PUBLISHERTYPE" ON "PUBLISHER" ("PUBLISHER_TYPE_ID")
;

CREATE INDEX "FK_PERMISSION_PERMISSIONSTATE" ON "PERMISSION" ("PERMISSION_STATE_ID")
;

CREATE INDEX "FK_TITLEGATHER_GATHERSCHEDULE" ON "TITLE_GATHER" ("GATHER_SCHEDULE_ID")
;

CREATE INDEX "FK_ACTORACTION_ACTOR" ON "ACTOR_ACTION" ("ACTOR_ID")
;

CREATE INDEX "FK_GATHERDATE_TITLEGATHER" ON "GATHER_DATE" ("TITLE_GATHER_ID")
;

CREATE INDEX "FK_TITLEGATHER_GATHERMETHOD" ON "TITLE_GATHER" ("GATHER_METHOD_ID")
;

CREATE INDEX "FK_AUTHUSER_INDIVIDUAL" ON "AUTH_USER" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_OWNERHISTORY_AGENCY" ON "OWNER_HISTORY" ("AGENCY_ID")
;

CREATE INDEX "FK_ROLE_INDIVIDUAL" ON "ROLE" ("INDIVIDUAL_ID")
;

CREATE INDEX "FK_AUTHUSER_AUTHGROUP" ON "AUTH_USER" ("AUTH_GROUP_ID")
;

CREATE INDEX "FK_SUBJECT_SUBJECTPARENT" ON "SUBJECT" ("SUBJECT_PARENT_ID")
;

CREATE INDEX "FK_TITLE_FORMAT" ON "TITLE" ("FORMAT_ID")
;

CREATE INDEX "FK_COMMANDLINEOPT_OPTIONGROUP" ON "COMMAND_LINE_OPT" ("OPTION_GROUP_ID")
;

CREATE INDEX "FK_PERMISSION_PUBLISHER" ON "PERMISSION" ("PUBLISHER_ID")
;

ALTER TABLE "AGENCY_AREA_IP" ADD PRIMARY KEY ("AGENCY_AREA_IP_ID");

ALTER TABLE "T_GATHER_ARG" ADD PRIMARY KEY ("OPTION_ARGUMENT_ID", "TITLE_GATHER_ID");

ALTER TABLE "REPORT_TYPE" ADD CONSTRAINT "REPORT_TYPE_PK" PRIMARY KEY ("REPORT_TYPE_ID");

ALTER TABLE "OLD_TITLE_STANDING" ADD PRIMARY KEY ("TITLE_STANDING_ID");

ALTER TABLE "COMMAND_LINE_OPT" ADD PRIMARY KEY ("COMMAND_LINE_OPTION_ID");

ALTER TABLE "INDIVIDUAL" ADD PRIMARY KEY ("INDIVIDUAL_ID");

ALTER TABLE "AUTH_USER" ADD PRIMARY KEY ("AUTH_USER_ID");

ALTER TABLE "ACTOR" ADD PRIMARY KEY ("ACTOR_ID");

ALTER TABLE "UNSENT_EMAIL" ADD PRIMARY KEY ("UNSENT_EMAIL_ID");

ALTER TABLE "TITLE_PREVIOUS_NAME" ADD PRIMARY KEY ("TITLE_HISTORY_ID");

ALTER TABLE "APP_MIME_TYPE" ADD PRIMARY KEY ("APPLICATION_ID", "MIME_TYPE_ID");

ALTER TABLE "AUTH_RESTR" ADD PRIMARY KEY ("TITLE_AUTH_RESTR_ID");

ALTER TABLE "SUBJECT_TITLES" ADD PRIMARY KEY ("SUBJECT_ID", "TITLE_ID");

ALTER TABLE "ACTION" ADD PRIMARY KEY ("ACTION_ID");

ALTER TABLE "ARCH_ISSUE" ADD PRIMARY KEY ("ISSUE_ID");

ALTER TABLE "TITLE_INDIVIDUAL" ADD PRIMARY KEY ("INDIVIDUAL_ID", "TITLE_ID");

ALTER TABLE "PERMISSION_TYPE" ADD PRIMARY KEY ("PERMISSION_TYPE_ID");

ALTER TABLE "OLD_INSTANCE_STATUS" ADD PRIMARY KEY ("INSTANCE_STATUS_ID");

ALTER TABLE "REASON" ADD PRIMARY KEY ("REASON_ID");

ALTER TABLE "OPTION_ARGUMENT" ADD PRIMARY KEY ("OPTION_ARGUMENT_ID");

ALTER TABLE "TITLE" ADD PRIMARY KEY ("TITLE_ID");

ALTER TABLE "NOTIFICATION_STATUS" ADD PRIMARY KEY ("NOTIFICATION_STATUS_ID");

ALTER TABLE "ORGANISATION" ADD PRIMARY KEY ("ORGANISATION_ID");

ALTER TABLE "SUBJECT" ADD PRIMARY KEY ("SUBJECT_ID");

ALTER TABLE "AUTH_GROUP" ADD PRIMARY KEY ("AUTH_GROUP_ID");

ALTER TABLE "MIME_EXTENSION" ADD PRIMARY KEY ("MIME_EXTENSION_ID");

ALTER TABLE "INSTANCE_OPT_ARG" ADD PRIMARY KEY ("INSTANCE_ID", "INS_OPT_ARG_ID");

ALTER TABLE "REPORT" ADD PRIMARY KEY ("REPORT_ID");

ALTER TABLE "GATHER_DATE" ADD PRIMARY KEY ("GATHER_DATE_ID");

ALTER TABLE "OWNER_HISTORY" ADD PRIMARY KEY ("OWNER_ID");

ALTER TABLE "STATE" ADD PRIMARY KEY ("STATE_ID");

ALTER TABLE "PROFILE" ADD PRIMARY KEY ("PROFILE_ID");

ALTER TABLE "TITLE_HISTORY" ADD PRIMARY KEY ("TITLE_HISTORY_ID");

ALTER TABLE "PERIOD_TYPE" ADD PRIMARY KEY ("PERIOD_TYPE_ID");

ALTER TABLE "COPYRIGHT_TYPE" ADD PRIMARY KEY ("COPYRIGHT_TYPE_ID");

ALTER TABLE "DATE_RESTR" ADD PRIMARY KEY ("TITLE_DATE_RESTRICTION_ID");

ALTER TABLE "GATHER_FILTER_PRESET" ADD PRIMARY KEY ("GATHER_FILTER_PRESET_ID");

ALTER TABLE "GATHER_SCHEDULE" ADD PRIMARY KEY ("GATHER_SCHEDULE_ID");

ALTER TABLE "FORMAT" ADD PRIMARY KEY ("FORMAT_ID");

ALTER TABLE "TEP" ADD PRIMARY KEY ("TEP_ID");

ALTER TABLE "TITLE_GATHER" ADD PRIMARY KEY ("TITLE_ID");

ALTER TABLE "NOTIFICATION" ADD PRIMARY KEY ("NOTIFICATION_ID");

ALTER TABLE "PUBLISHER_TYPE" ADD PRIMARY KEY ("PUBLISHER_TYPE_ID");

ALTER TABLE "INSTANCE_MIME" ADD PRIMARY KEY ("INSTANCE_ID", "MIME_TYPE_ID");

ALTER TABLE "GATHER_METHOD" ADD PRIMARY KEY ("GATHER_METHOD_ID");

ALTER TABLE "MIME_TYPE" ADD PRIMARY KEY ("MIME_TYPE_ID");

ALTER TABLE "STATUS" ADD PRIMARY KEY ("STATUS_ID");

ALTER TABLE "INS_GATHER" ADD PRIMARY KEY ("INSTANCE_ID");

ALTER TABLE "STATE_HISTORY" ADD PRIMARY KEY ("STATE_HISTORY_ID");

ALTER TABLE "ACTOR_ACTION" ADD PRIMARY KEY ("ACTION_ID", "ACTOR_ID");

ALTER TABLE "TITLE_COL" ADD PRIMARY KEY ("COLLECTION_ID", "TITLE_ID");

ALTER TABLE "CONTACT" ADD PRIMARY KEY ("CONTACT_ID");

ALTER TABLE "STATUS_HISTORY" ADD PRIMARY KEY ("STATUS_HISTORY_ID");

ALTER TABLE "INDIV_NOTIF" ADD PRIMARY KEY ("INDIVIDUAL_ID", "NOTIFICATION_ID");

ALTER TABLE "ISSUE_GROUP" ADD PRIMARY KEY ("ISSUE_GROUP_ID");

ALTER TABLE "AGENCY" ADD PRIMARY KEY ("AGENCY_ID");

ALTER TABLE "COL" ADD PRIMARY KEY ("COL_ID");

ALTER TABLE "INDEXER" ADD PRIMARY KEY ("INDEXER_ID");

ALTER TABLE "ROLE" ADD PRIMARY KEY ("ROLE_ID");

ALTER TABLE "COL_SUBS" ADD PRIMARY KEY ("COL_ID", "SUBJECT_ID");

ALTER TABLE "CONTACT_TYPE" ADD PRIMARY KEY ("CONTACT_TYPE_ID");

ALTER TABLE "CONTACT_METHOD" ADD PRIMARY KEY ("CONTACT_METHOD_ID");

ALTER TABLE "OPTION_GROUP" ADD PRIMARY KEY ("OPTION_GROUP_ID");

ALTER TABLE "PERMISSION" ADD PRIMARY KEY ("PERMISSION_ID");

ALTER TABLE "PANDAS_EXCEPTION_LOG" ADD PRIMARY KEY ("EXCEPTION_LOG_ID");

ALTER TABLE "OLD_INSTANCE_STATE" ADD PRIMARY KEY ("INSTANCE_STATE_ID");

ALTER TABLE "APPLICATION" ADD PRIMARY KEY ("APPLICATION_ID");

ALTER TABLE "TITLE_PAR_CHILD" ADD PRIMARY KEY ("TITLE_PAR_CHILD_ID");

ALTER TABLE "OPT_ARG_PROFILE" ADD PRIMARY KEY ("OPTION_ARGUMENT_ID", "PROFILE_ID");

ALTER TABLE "AGENCY_AREA" ADD PRIMARY KEY ("AGENCY_AREA_ID");

ALTER TABLE "CONDITION" ADD PRIMARY KEY ("CONDITION_ID");

ALTER TABLE "OLD_TITLE_STATUS" ADD PRIMARY KEY ("TITLE_STATUS_ID");

ALTER TABLE "INSTANCE" ADD PRIMARY KEY ("INSTANCE_ID");

ALTER TABLE "PERMISSION_STATE" ADD PRIMARY KEY ("PERMISSION_STATE_ID");

ALTER TABLE "PUBLISHER" ADD PRIMARY KEY ("PUBLISHER_ID");

ALTER TABLE "AUTH_RES_GROUP" ADD PRIMARY KEY ("AUTH_GROUP_ID", "AUTH_RESTRICT_ID");

ALTER TABLE "INS_RESOURCE" ADD PRIMARY KEY ("INSTANCE_ID");

ALTER TABLE "QA_PROBLEM" ADD PRIMARY KEY ("PROBLEM_ID");

ALTER TABLE "REPORT_SCHEDULE" ADD CONSTRAINT "REPORT_SCHEDULE_PK" PRIMARY KEY ("REPORT_SCHEDULE_ID");

ALTER TABLE "PERIOD_RESTR" ADD PRIMARY KEY ("PERIOD_RESTRICTION_ID");

ALTER TABLE "ACTOR" ADD CONSTRAINT "ACTOR_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ACTOR_ACTION" ADD CONSTRAINT "ACTOR_ACTION_ACTION_FK" FOREIGN KEY ("ACTION_ID")
  REFERENCES "ACTION" ("ACTION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "ACTOR_ACTION" ADD CONSTRAINT "ACTOR_ACTION_ACTOR_FK" FOREIGN KEY ("ACTOR_ID")
  REFERENCES "ACTOR" ("ACTOR_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "AGENCY" ADD CONSTRAINT "AGENCY_ORGANISATION_FK" FOREIGN KEY ("ORGANISATION_ID")
  REFERENCES "ORGANISATION" ("ORGANISATION_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "AGENCY_AREA" ADD CONSTRAINT "AGENCY_AREA_AGENCY_FK" FOREIGN KEY ("AGENCY_ID")
  REFERENCES "AGENCY" ("AGENCY_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "AGENCY_AREA_IP" ADD CONSTRAINT "AGENCY_AREA_IP_AGENCYAREA_FK" FOREIGN KEY ("AGENCY_AREA_ID")
  REFERENCES "AGENCY_AREA" ("AGENCY_AREA_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "APP_MIME_TYPE" ADD CONSTRAINT "APP_MIME_TYPE_APPLICATION_FK" FOREIGN KEY ("APPLICATION_ID")
  REFERENCES "APPLICATION" ("APPLICATION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "APP_MIME_TYPE" ADD CONSTRAINT "APP_MIME_TYPE_MIMETYPE_FK" FOREIGN KEY ("MIME_TYPE_ID")
  REFERENCES "MIME_TYPE" ("MIME_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ARCH_ISSUE" ADD CONSTRAINT "ARCH_ISSUE_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "ARCH_ISSUE" ADD CONSTRAINT "ARCH_ISSUE_ISSUEGROUP_FK" FOREIGN KEY ("ISSUE_GROUP_ID")
  REFERENCES "ISSUE_GROUP" ("ISSUE_GROUP_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "AUTH_RESTR" ADD CONSTRAINT "AUTH_RESTR_CONDITION_FK" FOREIGN KEY ("CONDITION_ID")
  REFERENCES "CONDITION" ("CONDITION_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "AUTH_RES_GROUP" ADD CONSTRAINT "AUTH_RES_GROUP_AUTHGROUP_FK" FOREIGN KEY ("AUTH_GROUP_ID")
  REFERENCES "AUTH_GROUP" ("AUTH_GROUP_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "AUTH_RES_GROUP" ADD CONSTRAINT "AUTH_RES_GROUP_AUTHREST_FK" FOREIGN KEY ("AUTH_RESTRICT_ID")
  REFERENCES "AUTH_RESTR" ("TITLE_AUTH_RESTR_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "AUTH_USER" ADD CONSTRAINT "AUTH_USER_AUTHGROUP_FK" FOREIGN KEY ("AUTH_GROUP_ID")
  REFERENCES "AUTH_GROUP" ("AUTH_GROUP_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "AUTH_USER" ADD CONSTRAINT "AUTH_USER_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "COL" ADD CONSTRAINT "COL_COLLECTION_FK" FOREIGN KEY ("COL_PARENT_ID")
  REFERENCES "COL" ("COL_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "COL_SUBS" ADD CONSTRAINT "COL_SUBS_COLLECTIONS_FK" FOREIGN KEY ("COL_ID")
  REFERENCES "COL" ("COL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "COL_SUBS" ADD CONSTRAINT "COL_SUBS_SUBJECTS_FK" FOREIGN KEY ("SUBJECT_ID")
  REFERENCES "SUBJECT" ("SUBJECT_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "COMMAND_LINE_OPT" ADD CONSTRAINT "COMMAND_LINE_OPT_GROUP_FK" FOREIGN KEY ("OPTION_GROUP_ID")
  REFERENCES "OPTION_GROUP" ("OPTION_GROUP_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_CONTACTPERSON_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_INDEXER_FK" FOREIGN KEY ("INDEXER_ID")
  REFERENCES "INDEXER" ("INDEXER_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_METHOD_FK" FOREIGN KEY ("CONTACT_METHOD_ID")
  REFERENCES "CONTACT_METHOD" ("CONTACT_METHOD_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_PUBLISHER_FK" FOREIGN KEY ("PUBLISHER_ID")
  REFERENCES "PUBLISHER" ("PUBLISHER_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_TYPE_FK" FOREIGN KEY ("CONTACT_TYPE_ID")
  REFERENCES "CONTACT_TYPE" ("CONTACT_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "CONTACT" ADD CONSTRAINT "CONTACT_USER_FK" FOREIGN KEY ("USER_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "DATE_RESTR" ADD CONSTRAINT "DATE_RESTR_AGENCYAREA_FK" FOREIGN KEY ("AGENCY_AREA_ID")
  REFERENCES "AGENCY_AREA" ("AGENCY_AREA_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "DATE_RESTR" ADD CONSTRAINT "DATE_RESTR_CONDITION_FK" FOREIGN KEY ("CONDITION_ID")
  REFERENCES "CONDITION" ("CONDITION_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "GATHER_DATE" ADD CONSTRAINT "GATHER_DATE_TITLEGATHER_FK" FOREIGN KEY ("TITLE_GATHER_ID")
  REFERENCES "TITLE_GATHER" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INDEXER" ADD CONSTRAINT "INDEXER_ORGANISATION_FK" FOREIGN KEY ("ORGANISATION_ID")
  REFERENCES "ORGANISATION" ("ORGANISATION_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INDIV_NOTIF" ADD CONSTRAINT "INDIV_NOTIF_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INDIV_NOTIF" ADD CONSTRAINT "INDIV_NOTIF_NOTIFICATION_FK" FOREIGN KEY ("NOTIFICATION_ID")
  REFERENCES "NOTIFICATION" ("NOTIFICATION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INDIV_NOTIF" ADD CONSTRAINT "INDIV_NOTIF_STATUS_FK" FOREIGN KEY ("NOTIFICATION_STATUS_ID")
  REFERENCES "NOTIFICATION_STATUS" ("NOTIFICATION_STATUS_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INSTANCE" ADD CONSTRAINT "INSTANCE_CURRENTSTATE_FK" FOREIGN KEY ("CURRENT_STATE_ID")
  REFERENCES "STATE" ("STATE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE" ADD CONSTRAINT "INSTANCE_INSTANCEGATHER_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INS_GATHER" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE" ADD CONSTRAINT "INSTANCE_INSTANCERESOURCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INS_RESOURCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE" ADD CONSTRAINT "INSTANCE_OLDINSTANCESTATE_FK" FOREIGN KEY ("INSTANCE_STATE_ID")
  REFERENCES "OLD_INSTANCE_STATE" ("INSTANCE_STATE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE" ADD CONSTRAINT "INSTANCE_OLDINSTANCESTATUS_FK" FOREIGN KEY ("INSTANCE_STATUS_ID")
  REFERENCES "OLD_INSTANCE_STATUS" ("INSTANCE_STATUS_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE" ADD CONSTRAINT "INSTANCE_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INSTANCE_MIME" ADD CONSTRAINT "INSTANCE_MIME_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE_MIME" ADD CONSTRAINT "INSTANCE_MIME_MIMETYPE_FK" FOREIGN KEY ("MIME_TYPE_ID")
  REFERENCES "MIME_TYPE" ("MIME_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INSTANCE_OPT_ARG" ADD CONSTRAINT "INSTANCE_OPT_ARG_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "INSTANCE_OPT_ARG" ADD CONSTRAINT "INSTANCE_OPT_ARG_OPTARG_FK" FOREIGN KEY ("INS_OPT_ARG_ID")
  REFERENCES "OPTION_ARGUMENT" ("OPTION_ARGUMENT_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INS_GATHER" ADD CONSTRAINT "INS_GATHER_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "INS_RESOURCE" ADD CONSTRAINT "INS_RESOURCE_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ISSUE_GROUP" ADD CONSTRAINT "ISSUE_GROUP_TITLEENTRYPAGE_FK" FOREIGN KEY ("TEP_ID")
  REFERENCES "TEP" ("TEP_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OPTION_ARGUMENT" ADD CONSTRAINT "OPTION_ARGUMENT_OPTION_FK" FOREIGN KEY ("COMMAND_LINE_OPTION_ID")
  REFERENCES "COMMAND_LINE_OPT" ("COMMAND_LINE_OPTION_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OPT_ARG_PROFILE" ADD CONSTRAINT "OPT_ARG_PROFILE_OPTIONARG_FK" FOREIGN KEY ("OPTION_ARGUMENT_ID")
  REFERENCES "OPTION_ARGUMENT" ("OPTION_ARGUMENT_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OPT_ARG_PROFILE" ADD CONSTRAINT "OPT_ARG_PROFILE_PROFILE_FK" FOREIGN KEY ("PROFILE_ID")
  REFERENCES "PROFILE" ("PROFILE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "ORGANISATION" ADD CONSTRAINT "ORGANISATION_AGENCY_FK" FOREIGN KEY ("AGENCY_ID")
  REFERENCES "AGENCY" ("AGENCY_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "ORGANISATION" ADD CONSTRAINT "ORGANISATION_INDEXER_FK" FOREIGN KEY ("INDEXER_ID")
  REFERENCES "INDEXER" ("INDEXER_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "ORGANISATION" ADD CONSTRAINT "ORGANISATION_PUBLISHER_FK" FOREIGN KEY ("PUBLISHER_ID")
  REFERENCES "PUBLISHER" ("PUBLISHER_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "OWNER_HISTORY" ADD CONSTRAINT "OWNER_HISTORY_AGENCY_FK" FOREIGN KEY ("AGENCY_ID")
  REFERENCES "AGENCY" ("AGENCY_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OWNER_HISTORY" ADD CONSTRAINT "OWNER_HISTORY_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OWNER_HISTORY" ADD CONSTRAINT "OWNER_HISTORY_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "OWNER_HISTORY" ADD CONSTRAINT "OWNER_HISTORY_TRANSFERRER_FK" FOREIGN KEY ("TRANSFERRER_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "PANDAS_EXCEPTION_LOG" ADD CONSTRAINT "PANDAS_EXCEPTION_LOG_INST_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PANDAS_EXCEPTION_LOG" ADD CONSTRAINT "PANDAS_EXCEPTION_LOG_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "PERIOD_RESTR" ADD CONSTRAINT "PERIOD_RESTR_AGENCYAREA_FK" FOREIGN KEY ("AGENCY_AREA_ID")
  REFERENCES "AGENCY_AREA" ("AGENCY_AREA_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PERIOD_RESTR" ADD CONSTRAINT "PERIOD_RESTR_CONDITION_FK" FOREIGN KEY ("CONDITION_ID")
  REFERENCES "CONDITION" ("CONDITION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PERIOD_RESTR" ADD CONSTRAINT "PERIOD_RESTR_PERIODTYPE_FK" FOREIGN KEY ("PERIOD_TYPE_ID")
  REFERENCES "PERIOD_TYPE" ("PERIOD_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "PERMISSION" ADD CONSTRAINT "PERMISSION_GRANTEDBY_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PERMISSION" ADD CONSTRAINT "PERMISSION_PUBLISHER_FK" FOREIGN KEY ("PUBLISHER_ID")
  REFERENCES "PUBLISHER" ("PUBLISHER_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PERMISSION" ADD CONSTRAINT "PERMISSION_STATE_FK" FOREIGN KEY ("PERMISSION_STATE_ID")
  REFERENCES "PERMISSION_STATE" ("PERMISSION_STATE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PERMISSION" ADD CONSTRAINT "PERMISSION_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PERMISSION" ADD CONSTRAINT "PERMISSION_TYPE_FK" FOREIGN KEY ("PERMISSION_TYPE_ID")
  REFERENCES "PERMISSION_TYPE" ("PERMISSION_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "PUBLISHER" ADD CONSTRAINT "PUBLISHER_ORGANISATION_FK" FOREIGN KEY ("ORGANISATION_ID")
  REFERENCES "ORGANISATION" ("ORGANISATION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "PUBLISHER" ADD CONSTRAINT "PUBLISHER_PUBLISHERTYPE_FK" FOREIGN KEY ("PUBLISHER_TYPE_ID")
  REFERENCES "PUBLISHER_TYPE" ("PUBLISHER_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "QA_PROBLEM" ADD CONSTRAINT "QA_PROBLEM_CREATOR_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "QA_PROBLEM" ADD CONSTRAINT "QA_PROBLEM_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "REASON" ADD CONSTRAINT "REASON_STATUS_FK" FOREIGN KEY ("STATUS_ID")
  REFERENCES "STATUS" ("STATUS_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "REPORT" ADD FOREIGN KEY ("AGENCY_ID")
  REFERENCES "AGENCY" ("AGENCY_ID");
ALTER TABLE "REPORT" ADD FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID");
ALTER TABLE "REPORT" ADD FOREIGN KEY ("REPORT_TYPE_ID")
  REFERENCES "REPORT_TYPE" ("REPORT_TYPE_ID");
ALTER TABLE "REPORT" ADD FOREIGN KEY ("PUBLISHER_TYPE_ID")
  REFERENCES "PUBLISHER_TYPE" ("PUBLISHER_TYPE_ID");
ALTER TABLE "REPORT" ADD FOREIGN KEY ("REPORT_SCHEDULE_ID")
  REFERENCES "REPORT_SCHEDULE" ("REPORT_SCHEDULE_ID");

ALTER TABLE "ROLE" ADD CONSTRAINT "ROLE_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "ROLE" ADD CONSTRAINT "ROLE_ORGANISATION_FK" FOREIGN KEY ("ORGANISATION_ID")
  REFERENCES "ORGANISATION" ("ORGANISATION_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "STATE_HISTORY" ADD CONSTRAINT "STATE_HISTORY_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "STATE_HISTORY" ADD CONSTRAINT "STATE_HISTORY_INSTANCE_FK" FOREIGN KEY ("INSTANCE_ID")
  REFERENCES "INSTANCE" ("INSTANCE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "STATE_HISTORY" ADD CONSTRAINT "STATE_HISTORY_STATE_FK" FOREIGN KEY ("STATE_ID")
  REFERENCES "STATE" ("STATE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "STATUS_HISTORY" ADD CONSTRAINT "STATUS_HISTORY_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "STATUS_HISTORY" ADD CONSTRAINT "STATUS_HISTORY_REASON_FK" FOREIGN KEY ("REASON_ID")
  REFERENCES "REASON" ("REASON_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "STATUS_HISTORY" ADD CONSTRAINT "STATUS_HISTORY_STATUS_FK" FOREIGN KEY ("STATUS_ID")
  REFERENCES "STATUS" ("STATUS_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "STATUS_HISTORY" ADD CONSTRAINT "STATUS_HISTORY_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "SUBJECT" ADD CONSTRAINT "SUBJECT_SUBJECT_FK" FOREIGN KEY ("SUBJECT_PARENT_ID")
  REFERENCES "SUBJECT" ("SUBJECT_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "SUBJECT_TITLES" ADD CONSTRAINT "SUBJECT_TITLES_SUBJECTS_FK" FOREIGN KEY ("SUBJECT_ID")
  REFERENCES "SUBJECT" ("SUBJECT_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "SUBJECT_TITLES" ADD CONSTRAINT "SUBJECT_TITLES_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TEP" ADD CONSTRAINT "TEP_COPYRIGHT_FK" FOREIGN KEY ("COPYRIGHT_TYPE_ID")
  REFERENCES "COPYRIGHT_TYPE" ("COPYRIGHT_TYPE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TEP" ADD CONSTRAINT "TEP_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_AGENCY_FK" FOREIGN KEY ("AGENCY_ID")
  REFERENCES "AGENCY" ("AGENCY_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_CURRENTOWNER_FK" FOREIGN KEY ("CURRENT_OWNER_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_CURRENTSTATUS_FK" FOREIGN KEY ("CURRENT_STATUS_ID")
  REFERENCES "STATUS" ("STATUS_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_DEFAULTPERMISSION_FK" FOREIGN KEY ("DEFAULT_PERMISSION_ID")
  REFERENCES "PERMISSION" ("PERMISSION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_FORMAT_FK" FOREIGN KEY ("FORMAT_ID")
  REFERENCES "FORMAT" ("FORMAT_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_INDEXER_FK" FOREIGN KEY ("INDEXER_ID")
  REFERENCES "INDEXER" ("INDEXER_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_OLDTITLESTANDING_FK" FOREIGN KEY ("STANDING_ID")
  REFERENCES "OLD_TITLE_STANDING" ("TITLE_STANDING_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_OLDTITLESTATUS_FK" FOREIGN KEY ("STATUS_ID")
  REFERENCES "OLD_TITLE_STATUS" ("TITLE_STATUS_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_PERMISSION_FK" FOREIGN KEY ("PERMISSION_ID")
  REFERENCES "PERMISSION" ("PERMISSION_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_PUBLISHER_FK" FOREIGN KEY ("PUBLISHER_ID")
  REFERENCES "PUBLISHER" ("PUBLISHER_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_TITLEENTRYPAGE_FK" FOREIGN KEY ("TEP_ID")
  REFERENCES "TEP" ("TEP_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE" ADD CONSTRAINT "TITLE_TITLEGATHER_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE_GATHER" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TITLE_COL" ADD CONSTRAINT "TITLE_COL_COLLECTION_FK" FOREIGN KEY ("COLLECTION_ID")
  REFERENCES "COL" ("COL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_COL" ADD CONSTRAINT "TITLE_COL_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TITLE_GATHER" ADD CONSTRAINT "TITLE_GATHER_GATHERMETHOD_FK" FOREIGN KEY ("GATHER_METHOD_ID")
  REFERENCES "GATHER_METHOD" ("GATHER_METHOD_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_GATHER" ADD CONSTRAINT "TITLE_GATHER_GATHERSCHEDULE_FK" FOREIGN KEY ("GATHER_SCHEDULE_ID")
  REFERENCES "GATHER_SCHEDULE" ("GATHER_SCHEDULE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_GATHER" ADD CONSTRAINT "TITLE_GATHER_PROFILE_FK" FOREIGN KEY ("ACTIVE_PROFILE_ID")
  REFERENCES "PROFILE" ("PROFILE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_GATHER" ADD CONSTRAINT "TITLE_GATHER_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TITLE_HISTORY" ADD CONSTRAINT "TITLE_HISTORY_CONTINUEDBY_FK" FOREIGN KEY ("CEASED_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_HISTORY" ADD CONSTRAINT "TITLE_HISTORY_CONTINUES_FK" FOREIGN KEY ("CONTINUES_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TITLE_INDIVIDUAL" ADD CONSTRAINT "TITLE_INDIVIDUAL_INDIVIDUAL_FK" FOREIGN KEY ("INDIVIDUAL_ID")
  REFERENCES "INDIVIDUAL" ("INDIVIDUAL_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_INDIVIDUAL" ADD CONSTRAINT "TITLE_INDIVIDUAL_TITLE_FK" FOREIGN KEY ("TITLE_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "TITLE_PAR_CHILD" ADD CONSTRAINT "TITLE_PAR_CHILD_CHILD_FK" FOREIGN KEY ("CHILD_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "TITLE_PAR_CHILD" ADD CONSTRAINT "TITLE_PAR_CHILD_PARENT_FK" FOREIGN KEY ("PARENT_ID")
  REFERENCES "TITLE" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE "T_GATHER_ARG" ADD CONSTRAINT "T_GATHER_ARG_OPTIONARGUMENT_FK" FOREIGN KEY ("OPTION_ARGUMENT_ID")
  REFERENCES "OPTION_ARGUMENT" ("OPTION_ARGUMENT_ID") DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE "T_GATHER_ARG" ADD CONSTRAINT "T_GATHER_ARG_TITLEGATHER_FK" FOREIGN KEY ("TITLE_GATHER_ID")
  REFERENCES "TITLE_GATHER" ("TITLE_ID") DEFERRABLE INITIALLY DEFERRED;

Insert into APPLICATION (APPLICATION_ID,NAME) values (1,'Adobe Acrobat Viewer');
Insert into APPLICATION (APPLICATION_ID,NAME) values (2,'Ghostview');
Insert into APPLICATION (APPLICATION_ID,NAME) values (3,'Internet Explorer 5+');
Insert into APPLICATION (APPLICATION_ID,NAME) values (4,'Netscape 6+');
Insert into APPLICATION (APPLICATION_ID,NAME) values (5,'a PNG viewer');
Insert into APPLICATION (APPLICATION_ID,NAME) values (6,'Microsoft Word');
Insert into APPLICATION (APPLICATION_ID,NAME) values (7,'StarOffice');
Insert into APPLICATION (APPLICATION_ID,NAME) values (8,'QuickTime');
Insert into APPLICATION (APPLICATION_ID,NAME) values (9,'Windows Media Player');
Insert into APPLICATION (APPLICATION_ID,NAME) values (10,'RealPlayer');
Insert into APPLICATION (APPLICATION_ID,NAME) values (11,'Apple QuickTime');
Insert into APPLICATION (APPLICATION_ID,NAME) values (12,'WinZip');
Insert into APPLICATION (APPLICATION_ID,NAME) values (13,'Microsoft Media Player');
Insert into APPLICATION (APPLICATION_ID,NAME) values (14,'Micorosft Powerpoint');
Insert into APPLICATION (APPLICATION_ID,NAME) values (15,'Macromedia Flash plugin');
Insert into APPLICATION (APPLICATION_ID,NAME) values (16,'Microsoft Paint');
Insert into APPLICATION (APPLICATION_ID,NAME) values (17,'Wordpad');
Insert into APPLICATION (APPLICATION_ID,NAME) values (18,'Macromedia Shockwave');
Insert into APPLICATION (APPLICATION_ID,NAME) values (19,'WinAmp');
Insert into APPLICATION (APPLICATION_ID,NAME) values (20,'GhostView');
Insert into APPLICATION (APPLICATION_ID,NAME) values (21,'Microsoft Excel');
Insert into APPLICATION (APPLICATION_ID,NAME) values (22,'LaTeX');
Insert into APPLICATION (APPLICATION_ID,NAME) values (23,'Adobe Acobat Viewer of Ghostview');
alter sequence APPLICATION_SEQ increment by 24;


Insert into CONDITION (CONDITION_DESCRIPTION,CONDITION_ID,NAME) values ('Enabled means that the current restriction will always be enforced during the restrictions cycle',1,'Enabled');
Insert into CONDITION (CONDITION_DESCRIPTION,CONDITION_ID,NAME) values ('This means that a user has manually disabled this restriction',2,'Disabled');
Insert into CONDITION (CONDITION_DESCRIPTION,CONDITION_ID,NAME) values ('This means that the restrictions has expired naturally over time',3,'Expired');
alter sequence CONDITION_SEQ increment by 4;

Insert into CONTACT_METHOD (CONTACT_METHOD,CONTACT_METHOD_ID) values ('Email',1);
Insert into CONTACT_METHOD (CONTACT_METHOD,CONTACT_METHOD_ID) values ('Phone',2);
Insert into CONTACT_METHOD (CONTACT_METHOD,CONTACT_METHOD_ID) values ('Fax',3);
Insert into CONTACT_METHOD (CONTACT_METHOD,CONTACT_METHOD_ID) values ('Post',4);
alter sequence CONTACT_METHOD_SEQ increment by 5;

Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Enquiry',1,'%o: Enquiry');
Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Initial request',2,'%o: Seeking Permission to Archive %t');
Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Follow-up request',3,'%o: Seeking Permission to Archive %t');
Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Acknowledgment of reply',4,'%o: Acknowledgement of Reply');
Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Ack. of permission granted',5,'%o: Acknowledgement of Permission Granted');
Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Rights management',6,'%o: Rights Management');
Insert into CONTACT_TYPE (CONTACT_TYPE,CONTACT_TYPE_ID,EMAIL_SUBJECT) values ('Publication notification',7,'%o: Publication Notification');
alter sequence CONTACT_TYPE_SEQ increment by 8;

Insert into FORMAT (FORMAT_ID,NAME) values (1,'Serial');
Insert into FORMAT (FORMAT_ID,NAME) values (2,'Mono');
Insert into FORMAT (FORMAT_ID,NAME) values (3,'Integrating');
alter sequence FORMAT_SEQ increment by 4;

Insert into GATHER_FILTER_PRESET (FILTER_NAME,FILTER_PRESET,GATHER_FILTER_PRESET_ID) values ('Disallow all - allow URL/images/style','-* +*target_site_url_here/* +*.jpg +*.gif +*.png +*.css +*.js',4);
Insert into GATHER_FILTER_PRESET (FILTER_NAME,FILTER_PRESET,GATHER_FILTER_PRESET_ID) values ('Disallow all - allow images/style/PDF/DOC','-* +*.pdf +*.doc +*.jpg +*.gif +*.png +*.css +*.js',3);
Insert into GATHER_FILTER_PRESET (FILTER_NAME,FILTER_PRESET,GATHER_FILTER_PRESET_ID) values ('Disallow all - allow images/style','-* +*.jpg +*.gif +*.png +*.css +*.js',1);
Insert into GATHER_FILTER_PRESET (FILTER_NAME,FILTER_PRESET,GATHER_FILTER_PRESET_ID) values ('Disallow external PDFs','-*.pdf +*target_site_url_here/*.pdf',2);
alter sequence GATHER_FILTER_PRESET_SEQ increment by 5;

Insert into GATHER_METHOD (GATHER_METHOD_ID,METHOD_DESC,METHOD_NAME) values (1,null,'HTTrack');
Insert into GATHER_METHOD (GATHER_METHOD_ID,METHOD_DESC,METHOD_NAME) values (2,null,'Upload');
alter sequence GATHER_METHOD_SEQ increment by 3;

Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (1,'None');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (2,'Daily');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (3,'Weekly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (4,'Fortnightly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (5,'Monthly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (6,'Quarterly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (7,'Annual');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (8,'Half-Yearly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (9,'9-Monthly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (10,'18-Monthly');
Insert into GATHER_SCHEDULE (GATHER_SCHEDULE_ID,SCHEDULE_NAME) values (11,'Biennial');
alter sequence GATHER_SCHEDULE_SEQ increment by 12;


Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',117,null,'charset=ISO-','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',118,null,'charset=ISO-885','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',119,null,'charset=IS','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-10-05 00:00:00',120,null,'charset=ISO-8859-','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',121,null,'charset=ISO-8','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',122,null,'charset=ISO-8859','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',123,null,'charset=ISO','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-11-05 00:00:00',124,null,'charset=ISO-88','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',125,null,null,'x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',126,null,'ch','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',127,null,'cha','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',128,null,'charset=','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',129,null,'char','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',130,null,'cha','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',131,null,'charset=','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',132,null,'charset=I','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',133,null,'c','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',134,null,'charse','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',135,null,'charset','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',136,null,null,'javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',137,null,'ch','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',138,null,'char','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',139,null,'chars','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',140,null,'charse','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',141,null,'charset=utf-','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',142,null,'charset=ut','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',143,null,'chars','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',144,null,'charset=utf','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',145,null,'charset','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',146,null,'charset=u','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-2-06 00:00:00',147,null,'c','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-9-02 00:00:00',148,null,null,'tiff','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-10-02 00:00:00',149,null,null,'x-excel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-3-03 00:00:00',150,null,null,'x-midi','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-9-04 00:00:00',151,null,'charset=iso-8859-','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-7-06 00:00:00',152,null,null,'vocaltec-media-desc','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-7-06 00:00:00',153,null,null,'tsplayer','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-7-06 00:00:00',154,null,null,'x-gsm','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-4-04 00:00:00',155,null,null,'winhlp','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',156,null,'charset=ISO-','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-4-03 00:00:00',157,null,null,'text','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',158,null,'charset=I','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',159,null,'charset=ISO','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',160,null,'charset=ISO-88','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',161,null,'charset=ISO-8859','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',162,null,'charset=IS','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',163,null,'charset=','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',164,null,'charset=ISO-8859-','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',165,null,'charset=ISO-885','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',166,null,'charset=ISO-8','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',167,null,null,'x-ms-asx','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-11-04 00:00:00',168,null,'charset=ISO-8859-','jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-9-02 00:00:00',169,null,null,'x-png','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-9-05 00:00:00',170,null,null,'JPEG','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-05 00:00:00',171,null,null,'JPG','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',172,null,'charset=ISO-885','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',173,null,'charset=IS','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',174,null,'charset=ISO-8','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',175,null,'charset=ISO-8859','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',176,null,'charset=ISO','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',177,null,'charset=ISO-','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',178,null,'charset=ISO-88','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-12-05 00:00:00',179,null,null,'x-vCard','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-9-05 00:00:00',180,null,null,'type','bad');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',181,null,'charset=8859_','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-10-02 00:00:00',182,null,null,'x-zip','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '31-5-05 00:00:00',183,null,'charset=UTF-','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-10-04 00:00:00',184,null,null,'vbscript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '30-10-06 00:00:00',185,null,null,'PDF','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-3-03 00:00:00',186,null,null,'x-java-vm','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-8-02 00:00:00',187,'TeX DVI',null,'x-dvi','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-9-02 00:00:00',188,null,null,'x-httpd-boa-parsed','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-8-02 00:00:00',189,'Powerpoint Presentation',null,'powerpoint','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-10-03 00:00:00',190,null,null,'rfc822','message');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-11-02 00:00:00',191,null,null,'x-trash','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-11-02 00:00:00',192,null,null,'x-msdos-program','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-10-05 00:00:00',193,null,'charset=UTF-','atom+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',194,null,'charset=ISO-8859-','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',195,null,'char','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',196,null,'cha','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',197,null,'ch','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',198,null,'charset=ut','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',199,null,'charset=u','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',200,null,'charset=utf-','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',201,null,'charse','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',202,null,'charset','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',203,null,'charset=','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',204,null,null,'gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',205,null,'chars','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',206,null,'c','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-05 00:00:00',207,null,'charset=utf','gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-12-02 00:00:00',208,null,null,'unknown','www');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-11-02 00:00:00',209,null,null,'x-perl','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-7-03 00:00:00',210,null,null,'octet_stream','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-9-03 00:00:00',211,null,null,'x-gtar','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-9-04 00:00:00',212,null,'charset=U','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-9-04 00:00:00',213,null,'charset=UT','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',214,null,'charset=8859','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',215,null,'charset=88','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',216,null,'charset=8','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',217,null,'charset=885','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-11-05 00:00:00',218,null,'charset=UTF','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-1-04 00:00:00',219,null,null,'x-pdb','chemical');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-6-03 00:00:00',220,null,null,'unknown','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-11-02 00:00:00',221,null,null,'excel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-5-04 00:00:00',222,null,null,'msexcel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-10-02 00:00:00',223,'RealMedia Stream',null,'x-pn-realmedia','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-10-05 00:00:00',224,null,null,'XML','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-7-04 00:00:00',225,null,null,'jpg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-12-02 00:00:00',226,null,null,'richtext','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-1-07 00:00:00',227,null,'charset=UTF-','plain','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',228,null,null,'java-archive','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-4-04 00:00:00',229,null,null,'x-component','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-2-03 00:00:00',230,null,null,'mspowerpoint','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-03 00:00:00',231,null,null,'x-ipix','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-2-05 00:00:00',232,null,null,'js','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-7-03 00:00:00',233,null,null,'pgp-signature','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',234,null,'charset=iso-88','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-9-03 00:00:00',235,null,null,'binary','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',236,null,'charset=i','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',237,null,'charset=iso-885','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',238,null,'charset=iso-8','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',239,null,'charset=iso-8859','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',240,null,'charset=is','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',241,null,'charset=iso-8859-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',242,null,'charset=iso','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',243,null,'charset=iso-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-12-03 00:00:00',244,null,null,'x-java-archive','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-6-04 00:00:00',245,null,null,'x-flash','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '30-5-06 00:00:00',246,null,null,'java-vm','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-1-07 00:00:00',247,null,null,'x-photoshop','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-11-02 00:00:00',248,null,null,'vnd.ms-works','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-7-05 00:00:00',249,null,null,'xml-dtd','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-6-05 00:00:00',250,null,null,'unknown','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',251,null,'charset=gb','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',252,null,'charset=KSC56','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',253,null,'charset=gb23','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',254,null,'charset=e','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',255,null,'charset=KSC5','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',256,null,'charset=g','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',257,null,'charset=KSC560','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',258,null,'charset=eu','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',259,null,'charset=euc-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',260,null,'charset=euc-j','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',261,null,'charset=euc-k','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',262,null,'charset=EUC','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',263,null,'charset=euc','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',264,null,null,'x.djvu','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',265,null,'charset=gb2','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',266,null,'charset=EUC-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',267,null,'charset=gb231','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',268,null,'charset=EUC-J','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',269,null,null,'xml-external-parsed-entity','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-1-06 00:00:00',270,null,'charset=U','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-1-06 00:00:00',271,null,'charset=UT','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-3-03 00:00:00',272,null,null,'html','ext');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-10-02 00:00:00',273,null,null,'*','*');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-7-04 00:00:00',274,null,null,'x-unknown','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '12-10-04 00:00:00',275,null,null,'mpg','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '21-7-05 00:00:00',276,null,null,'mp3','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-1-03 00:00:00',277,null,null,'ms-excel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-3-06 00:00:00',278,null,'charset=utf-','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-8-06 00:00:00',279,null,null,'calendar','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-8-06 00:00:00',280,null,null,'x-vCalendar','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-2-03 00:00:00',281,null,null,'x-troff','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-7-05 00:00:00',282,null,null,'force-download','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-7-03 00:00:00',283,null,null,'x-macbinary','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-1-03 00:00:00',284,null,null,'x-pn-realaudio','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-3-05 00:00:00',285,null,null,'x-shockwave-flash2-preview','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '31-3-06 00:00:00',286,null,null,'x-unkown','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-4-06 00:00:00',287,null,null,'rsd+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '31-1-06 00:00:00',288,null,null,'vnd.ms-pps','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '31-1-06 00:00:00',289,null,null,'brf','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '21-3-03 00:00:00',290,null,null,'octet-stream','binary');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',291,null,'charset=utf-','xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-1-07 00:00:00',292,null,null,'png','img');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '31-5-04 00:00:00',293,null,null,'Jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '31-5-04 00:00:00',294,null,null,'puz','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',295,null,'charset=utf-','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',296,null,'charset=','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',297,null,'charset=utf','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',298,null,'charset=u','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',299,null,'charset=ut','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-5-06 00:00:00',300,null,null,'GIF','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-3-03 00:00:00',301,null,null,'doc','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',302,null,'charset','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',303,null,'ch','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',304,null,'chars','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',305,null,'charse','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',306,null,'char','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',307,null,'cha','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-7-06 00:00:00',308,null,null,'x-bitmap','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-7-03 00:00:00',309,null,null,'x-sh','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-8-05 00:00:00',310,null,null,'vnd.djvu','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-8-05 00:00:00',311,null,null,'earthviewer','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-12-06 00:00:00',312,null,null,'mpegurl','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-4-03 00:00:00',313,null,null,'x-scpls','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '17-10-03 00:00:00',314,null,null,'x-msdownload','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-9-06 00:00:00',315,null,null,'vnd.google-earth.kml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-1-03 00:00:00',316,null,null,'x-x-ipscript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-9-02 00:00:00',317,null,null,'x-java','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-2-06 00:00:00',318,null,null,'x-mspublisher','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '14-5-04 00:00:00',319,null,null,'x-guffaw','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-5-03 00:00:00',320,null,null,'x-http-cgi','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-6-03 00:00:00',321,null,null,'x-pn-RealVideo','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-6-03 00:00:00',322,null,null,'x-pn-windows-acm','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',323,null,null,'xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-04 00:00:00',324,null,'c','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '12-7-05 00:00:00',325,null,null,'MSWORD','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-8-04 00:00:00',326,null,null,'x-Research-Info-Systems','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-8-04 00:00:00',327,null,null,'x-endnote-refer','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-8-02 00:00:00',328,null,null,'java','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-7-05 00:00:00',329,null,null,'x-download-application-pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-3-06 00:00:00',330,null,null,'x-download-application-msword','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-3-06 00:00:00',331,null,null,'x-download-application-x-msdos-program','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-4-07 00:00:00',332,null,null,'x-httpd-cgi','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-5-03 00:00:00',333,null,null,'x-ns-proxy-autoconfig','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-2-03 00:00:00',334,null,null,'*','java');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-5-06 00:00:00',335,null,null,'msaccess','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-10-04 00:00:00',336,null,null,'unknown','unknown');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-8-03 00:00:00',337,null,null,'svg+xml','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-8-05 00:00:00',338,null,null,'ppt','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-10-05 00:00:00',339,null,null,'ogg','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-10-05 00:00:00',340,null,null,'x-httpd-php3','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-6-03 00:00:00',341,null,null,'x-netcdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-4-03 00:00:00',342,null,null,'exe','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-10-03 00:00:00',343,'Acrobat',null,'x-pdf','Application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',344,null,null,'x-JavaScript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',345,null,null,'gif','Image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',346,null,'charset=ASCI','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',347,null,null,'xml+rdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',348,null,null,'x-eprint','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',349,null,'charset=iso8','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',350,null,null,'HTML','Text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',351,null,'charset=iso885','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',352,null,'charset=C','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',353,null,'charset=Cp','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',354,null,'charset=iso88','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',355,null,'charset=Cp1','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',356,null,'charset=iso8859','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',357,null,'charset=iso8859-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',358,null,'charset=Cp125','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',359,null,'charset=Cp12','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '03-9-04 00:00:00',360,null,null,'xml+rss','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-4-05 00:00:00',361,null,null,'pdf','Application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '14-7-03 00:00:00',362,null,null,'x-powerpoint','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '14-11-03 00:00:00',363,null,null,'x-internet-signup','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-3-07 00:00:00',364,null,null,'x-mswrite','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-2-05 00:00:00',365,null,null,'windows-screensaver','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-6-04 00:00:00',366,null,null,'x-msaccess','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-10-04 00:00:00',367,null,null,'x-mmxp','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-8-05 00:00:00',368,null,null,'xhtml+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-8-05 00:00:00',369,null,null,'mp4v-es','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-12-03 00:00:00',370,null,null,'x-java-class','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '21-1-04 00:00:00',371,null,null,'x-vcard','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-4-04 00:00:00',372,null,null,'xxx','xxx');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-11-06 00:00:00',373,null,null,'OCTET-STREAM','APPLICATION');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-3-04 00:00:00',374,null,null,'i-vrml','i-world');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-11-06 00:00:00',375,null,null,'x-wais-source','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-12-06 00:00:00',376,null,null,'appledouble','multipart');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',377,null,null,'plain','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',378,null,null,'plains','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',379,null,'charset=ISO-8859','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',380,null,'charset=ISO-885','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',381,null,'charset=iso-8859-1','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-05 00:00:00',382,null,null,'vnd.sun.xml.impress','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-2-05 00:00:00',383,null,'charset=UTF-','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '12-10-06 00:00:00',384,null,'charset=UTF','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-2-05 00:00:00',385,null,null,'x-httpd-html','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-8-06 00:00:00',386,null,null,'x-invalid','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-10-04 00:00:00',387,null,null,'devil','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-7-05 00:00:00',388,null,null,'x-navi-animation','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-6-05 00:00:00',389,null,null,'xslt+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-7-05 00:00:00',390,null,null,'x-msword','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-7-05 00:00:00',391,null,null,'rar','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-9-05 00:00:00',392,null,null,'x-download-audio-wav','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '12-12-05 00:00:00',393,null,null,'download','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-11-05 00:00:00',394,null,null,'text','plain');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-1-06 00:00:00',395,null,null,'x-word','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-1-06 00:00:00',396,null,'charset=iso-8859-','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-6-06 00:00:00',397,null,null,'css','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',398,null,'charset=u','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',399,null,'charset=utf','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',400,null,'charset=utf-','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',401,null,'charset','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',402,null,'charset=','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',403,null,'charset=ut','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-3-06 00:00:00',404,null,null,'unknown','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-6-06 00:00:00',405,null,null,'x-quicktimeplayer','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-7-06 00:00:00',406,null,null,'mp4','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-8-06 00:00:00',407,null,null,'x-ms-wmv','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-8-06 00:00:00',408,null,null,'svg-xml','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-9-06 00:00:00',409,null,null,'unknown','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',410,null,null,'css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',411,null,'charset=utf','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',412,null,'charse','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',413,null,'charset=ut','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',414,null,'chars','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',415,null,'char','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',416,null,'cha','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',417,null,'c','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',418,null,'ch','css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-06 00:00:00',419,null,'charset=u','javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-9-06 00:00:00',420,null,'Charset=ISO-8859-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-9-06 00:00:00',421,null,'Charset=ISO-8859','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '14-11-06 00:00:00',422,null,null,'jpeg','images');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-2-07 00:00:00',423,null,null,'GIF','Image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-12-06 00:00:00',424,null,'charset=ISO-8859-','vnd.ms-excel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-12-06 00:00:00',425,null,'charset=ISO-8859','vnd.ms-excel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '19-12-06 00:00:00',426,null,'charset=ISO-8859-','csv','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-2-07 00:00:00',427,null,null,'mpeg3','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-2-07 00:00:00',428,null,'charset=ISO-8859','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-2-07 00:00:00',429,null,'charset=ISO-8859-','xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-4-07 00:00:00',430,null,null,'Related','Multipart');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-6-07 00:00:00',431,null,null,'x-flv','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-6-07 00:00:00',432,null,'charset=UTF-','x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-8-02 00:00:00',1,null,null,'plain','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-8-02 00:00:00',2,null,null,'jpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-8-02 00:00:00',3,'Acrobat',null,'pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-8-02 00:00:00',4,null,null,'octet-stream','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-8-02 00:00:00',5,'Portable Network Graphic',null,'png','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-8-02 00:00:00',6,null,null,'x-javascript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-8-02 00:00:00',7,null,null,'css','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-8-02 00:00:00',8,null,null,'html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-8-02 00:00:00',9,null,null,'gif','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-8-02 00:00:00',10,'Word',null,'msword','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '22-8-02 00:00:00',11,null,null,'x-httpd-php','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-8-02 00:00:00',12,null,null,'pjpeg','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-9-02 00:00:00',13,null,null,'mpeg','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-10-02 00:00:00',14,null,null,'x-msvideo','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-8-02 00:00:00',15,'MIDI Audio',null,'mid','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-8-02 00:00:00',16,'QuickTime movie',null,'quicktime','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-8-02 00:00:00',17,null,null,'xml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-10-03 00:00:00',18,null,'charset=ISO-8859-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-8-02 00:00:00',19,'Zip',null,'x-zip-compressed','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '27-11-02 00:00:00',20,null,null,'x-xbitmap','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-03 00:00:00',21,null,null,'vnd.rn-realmedia','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-8-02 00:00:00',22,null,null,'x-icon','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-8-02 00:00:00',23,'MPEG movie',null,'mpeg','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-8-02 00:00:00',24,'Powerpoint Presentation',null,'vnd.ms-powerpoint','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-9-04 00:00:00',25,null,null,'atom+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '24-4-03 00:00:00',26,null,null,'javascript','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-4-03 00:00:00',27,null,null,'xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-6-04 00:00:00',28,null,null,'rss+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '15-8-02 00:00:00',29,'Shockwave / Flash',null,'x-shockwave-flash','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-9-02 00:00:00',30,null,null,'x-ms-wmv','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-9-02 00:00:00',31,null,null,'midi','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-8-02 00:00:00',32,'Bitmap Image',null,'bmp','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-8-02 00:00:00',33,'Zip',null,'zip','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-8-02 00:00:00',34,'WAV Audio',null,'x-wav','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-10-04 00:00:00',35,null,'charset=ISO-8859-','pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-8-02 00:00:00',36,'Rich Text Format',null,'rtf','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '20-8-02 00:00:00',37,'AU Basic Audio',null,'basic','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-8-02 00:00:00',38,'RealAudio Stream',null,'x-pn-realaudio','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-8-02 00:00:00',39,'Macromedia Director',null,'x-director','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-9-02 00:00:00',40,'RealMedia Stream',null,'x-realaudio','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-8-02 00:00:00',41,'Microsoft Active Stream File',null,'x-ms-asf','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '14-8-02 00:00:00',42,null,null,'rtf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '06-8-02 00:00:00',43,'AIFF Audio',null,'x-aiff','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '08-10-02 00:00:00',44,'RealMedia Stream',null,'x-pn-realaudio-plugin','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '21-8-02 00:00:00',45,'MPEG Audio',null,'x-mpeg','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-9-02 00:00:00',46,null,null,'x-ms-bmp','image');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '29-4-03 00:00:00',47,null,null,'x-mpegurl','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-10-02 00:00:00',48,null,null,'mac-binhex40','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-10-02 00:00:00',49,null,null,'x-stuffit','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '02-1-03 00:00:00',50,null,null,'smil','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-8-02 00:00:00',51,'Postscript',null,'postscript','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-6-03 00:00:00',52,null,null,'x-ms-wmz','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '25-10-02 00:00:00',53,null,null,'x-msmetafile','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-9-02 00:00:00',54,null,null,'wav','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '05-8-02 00:00:00',55,'Excel Spreadsheet',null,'vnd.ms-excel','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',56,null,null,'x-ms-wvx','video');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '07-7-04 00:00:00',57,null,null,'x-font','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',58,null,'charset=utf-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '23-6-03 00:00:00',59,null,null,'x-vrml','x-world');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '12-7-04 00:00:00',60,null,null,'vrml','model');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '09-7-03 00:00:00',61,null,null,'x-tcl','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-4-04 00:00:00',62,null,null,'vnd.sun.xml.writer','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-10-02 00:00:00',63,null,null,'vnd.wap.wml','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-10-02 00:00:00',64,null,null,'unix-directory','httpd');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '21-2-03 00:00:00',65,null,null,'x-gzip','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '26-8-02 00:00:00',66,'LaTeX',null,'x-tex','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '04-10-02 00:00:00',67,null,null,'x-tar','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',68,null,'charset=utf','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',69,null,'charset=ut','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '01-11-02 00:00:00',70,null,null,'x-ms-wma','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '28-6-04 00:00:00',71,null,null,'x-ms-wax','audio');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-8-04 00:00:00',72,null,null,'x-pdf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '21-10-02 00:00:00',73,null,null,'x-rtf','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-11-03 00:00:00',74,null,'charset=UTF','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '18-3-04 00:00:00',75,null,null,'x-java-applet','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',76,null,'charset=ISO-8859','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-11-03 00:00:00',77,null,'charset=UTF-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-8-04 00:00:00',78,null,null,'x-ogg','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',79,null,'charset=ISO-88','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '11-11-03 00:00:00',80,null,null,'rdf+xml','application');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',81,null,'charset=ISO-885','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-11-04 00:00:00',82,null,'ISO-8859','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-11-04 00:00:00',83,null,'charset=windows-1','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-11-04 00:00:00',84,null,'charset=windows-125','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-11-04 00:00:00',85,null,'charset=windows-12','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-11-04 00:00:00',86,null,'ISO-8859-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '16-11-04 00:00:00',87,null,'charset=windows-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',88,null,'charset=IS','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',89,null,'charset=wi','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',90,null,'charse','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',91,null,'ch','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',92,null,'cha','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',93,null,'charset=I','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',94,null,'charset=windows','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',95,null,'c','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',96,null,'ISO-88','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',97,null,'IS','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',98,null,'ISO','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',99,null,'charset=','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',100,null,'charset','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',101,null,'charset=win','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',102,null,'ISO-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',103,null,'charset=wind','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',104,null,'ISO-8','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',105,null,'charset=window','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',106,null,'ISO-885','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',107,null,'char','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',108,null,'charset=ISO','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',109,null,'charset=u','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',110,null,'charset=ISO-8','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',111,null,'I','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',112,null,'chars','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',113,null,'charset=windo','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '10-5-05 00:00:00',114,null,'charset=w','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',115,null,'charset=ISO-','html','text');
Insert into MIME_TYPE (DATE_ADDED,MIME_TYPE_ID,NOTE,PARAMETER,SUBTYPE,TYPE) values (timestamp '13-11-03 00:00:00',116,null,null,'html','text');
alter sequence MIME_TYPE_SEQ increment by 433;

Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (0,'sleeping');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (1,'creation');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (10,'awaitGaiter');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (12,'gathering');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (14,'gatherPause');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (16,'gatherProcess');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (20,'gathered');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (30,'transferring');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (40,'transferred');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (50,'deleting');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (60,'deleted');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (70,'checking');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (71,'IT to check');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (72,'IT checking');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (75,'IT return');
Insert into OLD_INSTANCE_STATE (INSTANCE_STATE_ID,INSTANCE_STATE_NAME) values (100,'unknown');
alter sequence OLD_INSTANCE_STATE_SEQ increment by 101;

Insert into OLD_INSTANCE_STATUS (INSTANCE_STATUS_ID,INSTANCE_STATUS_NAME) values (10,'Not Archived');
Insert into OLD_INSTANCE_STATUS (INSTANCE_STATUS_ID,INSTANCE_STATUS_NAME) values (20,'Archived');
Insert into OLD_INSTANCE_STATUS (INSTANCE_STATUS_ID,INSTANCE_STATUS_NAME) values (40,'Deleted');
alter sequence OLD_INSTANCE_STATUS_SEQ increment by 41;


Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (0,'Transfer');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (1,'Selected');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (2,'Current');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (3,'Ceased');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (4,'Ceased gathering');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (5,'Complete');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (6,'Disappeared');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (7,'Print version available');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (8,'Information available elsewhere');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (9,'Outside selection guidelines');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (10,'Responsibility of another agency');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (11,'Promotional');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (12,'Contents too slight');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (13,'Internal document');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (14,'Held in another format');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (15,'Site disappeared');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (16,'Awaiting development of site');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (17,'Checking status of other formats');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (18,'Other');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (19,'Unable to archive');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (20,'Site undeveloped');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (21,'Insufficient Australian content');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (22,'Similar site already selected');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (24,'Permission denied');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (25,'Site disappeared before archiving');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (26,'Unable to gain permission');
Insert into OLD_TITLE_STANDING (TITLE_STANDING_ID,TITLE_STANDING_NAME) values (28,'Catalogued');
alter sequence OLD_TITLE_STANDING_SEQ increment by 29;


Insert into OLD_TITLE_STATUS (TITLE_STATUS_ID,TITLE_STATUS_NAME,TITLE_STATUS_NOTES) values (0,'Transfer','Transfer titles are titles that have been transferred to another agency to await re-evaluation');
Insert into OLD_TITLE_STATUS (TITLE_STATUS_ID,TITLE_STATUS_NAME,TITLE_STATUS_NOTES) values (1,'National Preservation','National Preservation Titles are titles that have been selected for preservation');
Insert into OLD_TITLE_STATUS (TITLE_STATUS_ID,TITLE_STATUS_NAME,TITLE_STATUS_NOTES) values (3,'Monitor','Monitor titles are titles that may meet future requirements for National Preservation and are continually being evaluated');
Insert into OLD_TITLE_STATUS (TITLE_STATUS_ID,TITLE_STATUS_NAME,TITLE_STATUS_NOTES) values (4,'Reject','Reject titles are titles that may have not met selection criteria and may never meet any selection criteria');
Insert into OLD_TITLE_STATUS (TITLE_STATUS_ID,TITLE_STATUS_NAME,TITLE_STATUS_NOTES) values (5,'Pending selection','Pending Selection titles are titles that have been identified as possible future candidates for re-selection and preservation and do not yet meet any other selection status');
Insert into OLD_TITLE_STATUS (TITLE_STATUS_ID,TITLE_STATUS_NAME,TITLE_STATUS_NOTES) values (6,'External Resource','External');
alter sequence OLD_TITLE_STATUS_SEQ increment by 7;


Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,1,1,'Action Options');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,2,12,'Proxy Options');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (0,3,5,'Filters');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,4,2,'Limit Settings');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,5,7,'Flow Control');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,6,8,'Links Options');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,7,14,'Build Options');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,8,6,'Spider Options');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,9,3,'MIME types');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,10,4,'Browser ID');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,11,9,'Log, Index and Cache');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (1,12,10,'Expert Options');
Insert into OPTION_GROUP (ACCESS_LEVEL,OPTION_GROUP_ID,DISPLAY_ORDER,GROUP_NAME) values (null,13,null,'Guru Options');
alter sequence OPTION_GROUP_SEQ increment by 14;


Insert into PERIOD_TYPE (PERIOD_TYPE,PERIOD_TYPE_DESCRIPTION,PERIOD_TYPE_ID) values ('Forever','Never expire this restriction',0);
Insert into PERIOD_TYPE (PERIOD_TYPE,PERIOD_TYPE_DESCRIPTION,PERIOD_TYPE_ID) values ('Day(s)','Expire an instance/issue after x days',1);
Insert into PERIOD_TYPE (PERIOD_TYPE,PERIOD_TYPE_DESCRIPTION,PERIOD_TYPE_ID) values ('Week(s)','Expire an instance/issue after x weeks',2);
Insert into PERIOD_TYPE (PERIOD_TYPE,PERIOD_TYPE_DESCRIPTION,PERIOD_TYPE_ID) values ('Month(s)','Expire an instance/issue after x months',3);
Insert into PERIOD_TYPE (PERIOD_TYPE,PERIOD_TYPE_DESCRIPTION,PERIOD_TYPE_ID) values ('Year(s)','Expire an instance/issue after x years',4);
alter sequence PERIOD_TYPE_SEQ increment by 5;

Insert into PERMISSION_STATE (PERMISSION_STATE,PERMISSION_STATE_ID) values ('Granted',1);
Insert into PERMISSION_STATE (PERMISSION_STATE,PERMISSION_STATE_ID) values ('Denied',2);
Insert into PERMISSION_STATE (PERMISSION_STATE,PERMISSION_STATE_ID) values ('Unknown',3);
alter sequence PERMISSION_STATE_SEQ increment by 4;

Insert into PERMISSION_TYPE (PERMISSION_TYPE,PERMISSION_TYPE_ID) values ('Title Permission',1);
Insert into PERMISSION_TYPE (PERMISSION_TYPE,PERMISSION_TYPE_ID) values ('Publisher Permission',2);
alter sequence PERMISSION_TYPE_SEQ increment by 3;

Insert into PROFILE (NAME,PROFILE_DESCRIPTION,PROFILE_ID,IS_DEFAULT) values ('Httrack Defaults (Version 3.33)','The defaults used by httrack',1,0);
Insert into PROFILE (NAME,PROFILE_DESCRIPTION,PROFILE_ID,IS_DEFAULT) values ('Pandas Defaults','The default profile used for the creation of new titles.',2,1);
alter sequence PROFILE_SEQ increment by 3;

Insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION,PUBLISHER_TYPE,PUBLISHER_TYPE_ID) values ('Government bodies and agencies','Government',1);
Insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION,PUBLISHER_TYPE,PUBLISHER_TYPE_ID) values ('Organisations can be public or private bodies that provide non-commercial material.','Organisation',2);
Insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION,PUBLISHER_TYPE,PUBLISHER_TYPE_ID) values ('Educational Institutions','Education',3);
Insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION,PUBLISHER_TYPE,PUBLISHER_TYPE_ID) values ('Commercial bodies provide material on a cost basis.','Commercial',4);
Insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION,PUBLISHER_TYPE,PUBLISHER_TYPE_ID) values ('Individual','Personal',5);
Insert into PUBLISHER_TYPE (PUBLISHER_DESCRIPTION,PUBLISHER_TYPE,PUBLISHER_TYPE_ID) values ('Use when unknown','Other',6);
alter sequence PUBLISHER_TYPE_SEQ increment by 7;

Insert into REPORT_SCHEDULE (REPORT_SCHEDULE_ID,NAME) values (1,'Weekly');
Insert into REPORT_SCHEDULE (REPORT_SCHEDULE_ID,NAME) values (2,'Monthly');
Insert into REPORT_SCHEDULE (REPORT_SCHEDULE_ID,NAME) values (3,'Every 2 months');
Insert into REPORT_SCHEDULE (REPORT_SCHEDULE_ID,NAME) values (4,'Every 3 months');
alter sequence REPORT_SCHEDULE_SEQ increment by 5;

Insert into REPORT_TYPE (REPORT_TYPE_ID,NAME,JAVA_CLASS,HAS_DETAILS,HAS_PERIOD,HAS_AGENCY,HAS_PUBLISHER_TYPE,HAS_RESTRICTION_TYPE) values (1,'Total archived titles','au.gov.nla.webarchive.reports.ArchiveStatisticsReport',0,1,0,0,0);
Insert into REPORT_TYPE (REPORT_TYPE_ID,NAME,JAVA_CLASS,HAS_DETAILS,HAS_PERIOD,HAS_AGENCY,HAS_PUBLISHER_TYPE,HAS_RESTRICTION_TYPE) values (2,'Statistics By Status','au.gov.nla.webarchive.reports.StatisticsByStatusReport',0,1,1,0,0);
Insert into REPORT_TYPE (REPORT_TYPE_ID,NAME,JAVA_CLASS,HAS_DETAILS,HAS_PERIOD,HAS_AGENCY,HAS_PUBLISHER_TYPE,HAS_RESTRICTION_TYPE) values (3,'Scheduled Gathers','au.gov.nla.webarchive.reports.ScheduledGathersReport',1,1,1,0,0);
Insert into REPORT_TYPE (REPORT_TYPE_ID,NAME,JAVA_CLASS,HAS_DETAILS,HAS_PERIOD,HAS_AGENCY,HAS_PUBLISHER_TYPE,HAS_RESTRICTION_TYPE) values (4,'Newly Archived Titles','au.gov.nla.webarchive.reports.NewTitlesArchivedReport',0,1,1,1,0);
Insert into REPORT_TYPE (REPORT_TYPE_ID,NAME,JAVA_CLASS,HAS_DETAILS,HAS_PERIOD,HAS_AGENCY,HAS_PUBLISHER_TYPE,HAS_RESTRICTION_TYPE) values (5,'Archived titles by publisher type','au.gov.nla.webarchive.reports.TitlesByPublisherTypeReport',1,0,1,1,0);
Insert into REPORT_TYPE (REPORT_TYPE_ID,NAME,JAVA_CLASS,HAS_DETAILS,HAS_PERIOD,HAS_AGENCY,HAS_PUBLISHER_TYPE,HAS_RESTRICTION_TYPE) values (6,'Titles with current restriction','au.gov.nla.webarchive.reports.TitlesWithCurrentRestrictionReport',1,0,1,0,1);
alter sequence REPORT_TYPE_SEQ increment by 7;

Insert into STATE (STATE_ID,STATE_NAME) values (1,'archived');
Insert into STATE (STATE_ID,STATE_NAME) values (2,'awaitGather');
Insert into STATE (STATE_ID,STATE_NAME) values (3,'checked');
Insert into STATE (STATE_ID,STATE_NAME) values (4,'checking');
Insert into STATE (STATE_ID,STATE_NAME) values (5,'creation');
Insert into STATE (STATE_ID,STATE_NAME) values (6,'deleted');
Insert into STATE (STATE_ID,STATE_NAME) values (7,'deleting');
Insert into STATE (STATE_ID,STATE_NAME) values (8,'gatherPause');
Insert into STATE (STATE_ID,STATE_NAME) values (9,'gatherProcess');
Insert into STATE (STATE_ID,STATE_NAME) values (10,'gathered');
Insert into STATE (STATE_ID,STATE_NAME) values (12,'gathering');
Insert into STATE (STATE_ID,STATE_NAME) values (13,'archiving');
Insert into STATE (STATE_ID,STATE_NAME) values (14,'failed');
alter sequence STATE_SEQ increment by 15;


Insert into STATUS (STATUS_ID,STATUS_NAME) values (1,'nominated');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (2,'rejected');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (3,'selected');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (4,'monitored');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (5,'permission requested');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (6,'permission denied');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (7,'permission granted');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (8,'permission impossible');
Insert into STATUS (STATUS_ID,STATUS_NAME) values (11,'ceased');
alter sequence STATUS_SEQ increment by 11;

Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Awaiting development of site',1,4);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Checking status of other formats',2,4);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Other',16,4);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Contents too slight',3,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Held in another format',4,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Information available elsewhere',5,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Insufficient Australian content',6,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Internal document',7,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Other',8,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Outside selection guidelines',9,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Print version available',10,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Promotional',11,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Responsibility of another agency',12,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Similar site already selected',13,2);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Site undeveloped',15,4);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('No change to contents',17,11);
Insert into REASON (REASON,REASON_ID,STATUS_ID) values ('Other',18,11);
alter sequence REASON_SEQ increment by 19;

Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (10,'Health',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (11,'History',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (12,'Indigenous Australians',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (130,'Society & Social Issues',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (13,'Industry & Technology',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (140,'Tourism & Travel',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (15,'Government & Law',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (18,'Media',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (21,'Politics',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (22,'Sciences',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (23,'People & Culture',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (25,'Sports & Recreation',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (2,'Arts',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (3,'Business & Economy',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (53,'Defence',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (5,'Education',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (7,'Environment',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (83,'Humanities',null);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (16,'Literature',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (17,'Music',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (181,'Film & Cinema',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (201,'Performing Arts',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (241,'Poetry',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (41,'Architecture',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (42,'Dance',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (43,'Decorative Arts',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (44,'Design & Fashion',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (45,'Fine Arts',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (46,'Multi-Media and Digital Arts',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (47,'Photography',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (8,'Festivals & Events (Arts)',2);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (48,'Banking & Finance',3);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (49,'Commerce',3);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (50,'Economics',3);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (51,'Management',3);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (52,'Taxation',3);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (59,'Schooling',5);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (60,'Tertiary Education',5);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (61,'Vocational Education',5);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (62,'Climate Change',7);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (63,'Environmental Protection',7);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (64,'Forestry',7);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (65,'Water',7);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (72,'Alternative & Complementary Health Care',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (73,'Health Research',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (74,'Medical & Hospital Care',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (75,'Medical Conditions & Diseases',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (76,'Mental Health',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (77,'Pharmaceuticals',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (78,'Public Health',10);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (26,'Centenary of Federation',11);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (27,'Australian Republic Debate',11);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (79,'Constitution & Referenda',11);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (80,'Family History & Genealogy',11);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (81,'Local History',11);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (82,'Military History',11);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (87,'Government Indigenous Policy',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (88,'Indigenous Art',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (89,'Indigenous Business & Commerce',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (90,'Indigenous Culture',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (91,'Indigenous Education',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (92,'Indigenous Employment',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (93,'Indigenous Health',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (94,'Indigenous History',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (95,'Indigenous Land Rights',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (96,'Indigenous Languages',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (97,'Indigenous Native Title',12);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (100,'Energy',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (101,'Industrial & Manufacturing',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (102,'Mining',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (103,'Telecommunications',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (104,'Transportation',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (1,'Agriculture',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (4,'Computers & Internet',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (98,'Aquaculture & Fisheries',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (99,'Construction',13);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (161,'Foreign Affairs & Trade',15);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (162,'Law & Regulation',15);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (163,'State & Territory Government',15);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (67,'Commonwealth Government',15);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (70,'Local Government',15);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (105,'Radio',18);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (106,'Television',18);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (221,'Newspapers',18);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (281,'Comics & Zines',18);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (29,'Blogs',18);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (120,'Political Action',21);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (20,'Political Humour & Satire',21);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (28,'Political Parties and Politicians',21);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (6,'Election Campaigns',21);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (121,'Astronomy',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (122,'Biology',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (123,'Biotechnology',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (124,'Chemistry',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (125,'Geography and Mapping',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (126,'Geology',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (127,'Mathematics',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (128,'Physics',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (129,'Sociology',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (301,'Animals',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (302,'Plants',22);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (107,'Aged People',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (108,'Cultural Heritage Management',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (109,'Entertainment',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (110,'Ethnic Communities & Heritage',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (111,'Families',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (112,'Food & Drink',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (114,'Libraries & Cultural Institutions',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (116,'People with Disabilities',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (117,'Religion',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (118,'Women',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (119,'Youth',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (14,'Children',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (66,'Men',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (9,'Festivals & Events (Cultural)',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (113,'Gay, Lesbian & Transgender',23);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (142,'Games & Hobbies',25);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (143,'Sites for Children',25);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (144,'Sporting Events',25);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (145,'Sporting Organisations',25);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (19,'Olympic & Paralympic Games',25);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (24,'Sporting Personalities',25);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (54,'Air Force',53);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (55,'Army',53);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (56,'Navy',53);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (57,'Unit Associations',53);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (58,'Veterans',53);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (84,'Anthropology',83);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (85,'Archaeology',83);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (86,'Philosophy',83);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (131,'Community Issues & Volunteering',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (132,'Crime & Justice',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (133,'Drug & Alcohol Issues',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (134,'Employment & Industrial Relations',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (135,'Housing',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (137,'Social Institutions',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (138,'Social Problems and Action',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (139,'Social Welfare',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (261,'Immigration & Emigration',130);
Insert into SUBJECT (SUBJECT_ID,SUBJECT_NAME,SUBJECT_PARENT_ID) values (141,'Indigenous Tourism',140);
alter sequence SUBJECT_SEQ increment by 322;

Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (1,3);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (2,3);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (3,5);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (5,5);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (4,5);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (6,10);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (7,10);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (9,15);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (8,15);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,15);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (11,16);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (12,19);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (11,23);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,23);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (13,23);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (7,24);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (14,24);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (15,29);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (16,32);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (4,32);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (3,32);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (12,33);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (13,34);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,34);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (11,34);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (17,36);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (6,36);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,37);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (9,37);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (8,37);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,38);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (18,39);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,40);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (13,41);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (11,41);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,41);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,43);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (11,43);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (13,43);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,44);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (19,45);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,45);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (9,45);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (8,45);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (20,51);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (7,55);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (21,55);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (22,66);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (22,187);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (20,187);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (14,189);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (7,189);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (10,223);
Insert into APP_MIME_TYPE (APPLICATION_ID,MIME_TYPE_ID) values (23,343);

Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,50,null,'Make a searchable index for this mirror',null,1,null,0,0,0,'--search-index',null,11,'-',' ','%I','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,51,'1','Primary Scan Rule (scan mode)',null,null,null,1,0,0,'--priority','Select file types to be saved to disk',12,'-',' ','p','dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,52,'D','Travel mode',null,null,1,1,0,0,null,'Select parsing direction',12,'-',' ',null,'dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,53,'d','Global travel mode',null,null,1,1,0,0,null,'Select global parsing direction',12,'-',' ','global_travel','dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,54,null,'Debug HTTP headers in logfile',null,null,null,1,0,1,'--debug-headers','(Do not use if possible)',12,'-',' ','%H',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,55,null,'Filter test',null,null,null,0,0,0,null,'eg. *.gif www.bar.com/foo.gif (guru option)',13,'-',' ','#0','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,56,null,'Always flush log files',null,1,null,0,0,0,null,'(guru option)',13,'-',' ','#f','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,57,null,'Maximum number of filters',null,null,null,0,0,0,null,'Defaults to 500 (guru option)',13,'-',' ','#F','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,58,null,'Version info',null,null,null,0,0,0,null,'(guru option)',13,'-',' ','#h',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,59,null,'Scan stdin (debug)',null,null,null,0,0,0,null,'(guru option)',13,'-',' ','#K',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,60,null,'Display ugly progress information',null,1,null,0,0,0,null,'(guru option)',13,'-',' ','#p','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,61,null,'Catch URL',null,null,null,0,0,0,null,'Allows setup of a temporary proxy to capture complex URLs, often linked with the POST action (guru option)',13,'-',' ','#P',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,62,null,'Old FTP routines (debug)',null,null,null,0,0,0,null,'(guru option)',13,'-',' ','#R',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,63,null,'Generate transfer ops. log every minutes',null,null,null,0,0,0,null,'Generate a log file with transfer statistics (guru option)',13,'-',' ','#T',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,64,null,'Wait time',null,null,null,0,0,0,null,'"On hold" option, in seconds (guru option)',13,'-',' ','#u','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,65,null,'Generate transfer rate statistics every minutes',null,null,null,0,0,0,null,'Generate a log file with transfer statistics (guru option)',13,'-',' ','#Z',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,66,null,'Execute a shell command',null,null,null,0,1,0,null,'Eg. "echo hello" (guru option)',13,'-',' ','#! ',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,1,'w','Action','Choose an action',null,1,1,0,1,null,null,1,'-',' ',null,'dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,2,'proxy.nla.gov.au:3128','Proxy',null,null,null,0,0,1,'--proxy','Use a proxy. Format is [user:pass@]proxy:port',2,'-',' ','P','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,3,null,null,null,1,null,0,0,0,'--httpproxy-ftp','Use the proxy for FTP transfers (as well as HTTP)',2,'-',' ','%f','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,4,null,'Gather Filters','Filters enable you to refine exactly what web content is crawled',null,null,1,2,0,null,'Include or exclude particular directories, domains or file types',3,null,null,null,'GatherFilterPlugin');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,5,'5','Maximum mirroring depth','No value will mirror to any depth, within the specified site.',null,null,1,0,0,'--depth','Maximum mirroring depth from root location',4,'-',' ','r','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,6,'3','Maximum external depth','No value will mirror no external links. This overrides filters and the default engine limiter.',null,null,1,0,0,'--ext-depth','Maximum mirroring depth from external/forbidden addresses',4,'-',' ','%e','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,7,null,'Maximum file size','Avoid large files if desired.',null,null,1,0,0,'--max-files','Maximum file length for HTML and non-HTML files (Bytes)',4,'-',' ','m','SizeLimitsPlugin');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,8,'2000000000','Site size limit','Maximum amount of bytes to retrieve from the web',null,null,1,0,0,'--max-size','Maximum overall size that can be downloaded (Bytes)',4,'-',' ','M','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,9,'172800','Maximum time overall (s)','Maximum duration of the mirroring operation',null,null,1,0,0,'--max-time','Maximum mirror time in seconds (60 = 1 minute, 3600 = 1 hour)',4,'-',' ','E','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,10,'50000','Maximum transfer rate (B/S)','Maximum transfer rate',null,null,1,0,0,'--max-rate','Maximum transfer rate in bytes/seconds (1000=1kb/s max)',4,'-',' ','A','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,11,'6','Max connections / seconds','Maximum connections/second (avoid server overload)',null,null,1,0,0,null,'Maximum number of connections/seconds',4,'-',' ','%c','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,12,null,'Pause after downloading','Pause transfer if N bytes reached, and wait until lock file is deleted',null,null,1,0,0,'--max-pause','Temporarily stop gathering after downloading this number of bytes',4,'-',' ','G','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,13,'1000000','Maximum number of links','Maximum number of links that can be tested (not saved!) (-L#100000)',null,null,1,0,0,null,'Maximum number of links that may be analysed',4,'-',' ','#L','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,14,'6','Number of connections',null,null,null,1,0,1,'--sockets','Maximum number of simultaneous connections',5,'-',' ','c','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,15,'30','Time out','File timeout',null,null,1,0,0,'--timeout','Number of seconds before a non-responding link is abandoned',5,'-',' ','T','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,16,'3','Retries','This does not apply to fatal errors such as "404 Not Found", which will not be retried.',null,null,1,0,0,'--retries','Number of retries, in case of timeout or non-fatal errors',5,'-',' ','R','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,17,'150','Min transfer rate','Minimum admissible transfer rate before the link is deemed too slow and abandoned',null,null,1,0,0,'--min-rate','Minimum transfer rate tolerated for a link (bytes/seconds)',5,'-',' ','J','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,18,'3','Abandon host','Ignore all links from a host once a timeout or slow link is encountered on that host',null,null,1,0,0,'--host-control','When to abandon a host',5,'-',' ','H','popup');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,19,'1','Attempt to detect all links','This can generate bad requests, but may catch links in pages which use certain javascript tricks',2,null,1,0,0,'--extended-parsing','Extended parsing. Attempt to parse all links, even for unknown tags or javascript',6,'-',' ','%P','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,20,null,'Get non-HTML files related to a link','Follow external links',1,null,1,0,0,'--near','Get all non-html files linked to a html file (eg. an image stored on another server)',6,'-',' ','n','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,21,null,'Test validity of all links','Test all links in pages and log any errors found',1,null,1,0,0,'--test','Test all URLs (even external or forbidden ones)',6,'-',' ','t','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,22,'0','Site Structure Type',null,null,null,1,0,1,'--structure','How links are stored locally',7,'-',' ','N','dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,23,'1','DOS Names (8+3)','Generate DOS 8-3 filenames ONLY',null,null,0,0,0,'--long-names','Store all filenames using DOS 8-3 format',7,'-',' ','L','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,24,'0','Keep original links',null,null,null,0,0,1,'--keep-links',null,7,'-',' ','K','popup');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,25,null,'No external pages',null,1,null,0,0,0,'--replace-external','Replace external html links with error pages',7,'-',' ','x','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,26,null,'Hide passwords','Write external links without login/password',1,null,1,0,0,'--no-passwords','Do not include passwords for external password protected websites',7,'-',' ','%x','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,27,null,'Include query string','Write internal links with query string',1,null,0,0,0,'--include-query-string','include query string for local files (useless, for information purpose only)',7,'-',' ','%q','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,28,'1','Accept cookies',null,null,null,1,0,0,'--cookies','Accept cookies in order to retrieve pages which require a session',8,'-',' ','b','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,29,'1','Check document type','Eg. If a link called /cgi-bin/gen-image.cgi generates a gif image, the image will be named gen-image.gif rather than gen-image.cgi',null,null,1,0,0,'--check-type','Check document type for correct renaming of generated files',8,'-',' ','u','dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,30,'1','Parse Java files',null,1,null,1,0,0,'--parse-java','Parse Java classes to retrieve included files that must be downloaded',8,'-',' ','j','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,31,'2','Obey robot exclusion rules',null,null,null,1,0,0,'--robots','Follow local robots.txt and meta robot rules',8,'-',' ','s','dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,32,null,'Force old HTTP/1.0 requests (no 1.1)',null,1,null,1,0,0,'--http-10','Force HTTP/1.0 requests (Reduces update features. Only for old servers or proxies)',8,'-',' ','%h','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,33,null,'Tolerant requests (for servers)','Tolerate incorrect file sizes. Can cause files to become bogus.',1,null,1,0,0,'--tolerant','Use non-standard requests to get around some server bugs',8,'-',' ','%B','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,34,null,'Update hack (limit re-transfers)',null,1,null,1,0,0,'--updatehack','Use various hacks to limit re-transfers when updating (eg. Check for identically sized files)',8,'-',' ','%s','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,35,null,'Mime Type Mapping','These rules can speed up retrieval of files that contain many links of an unknown type',null,null,1,0,0,'--assume','Assume that a file type (cgi,asp..) is always linked to a mime type. Format: <file extension>=<mime type> (eg. php3=text/html)',9,'-',null,'%A ','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (1,36,null,'Browser Identity','This setting may cause the server to deliver different files',null,null,1,1,0,'--user-agent','Claim to be the selected user agent',10,'-',' ','F ','dropdown');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,37,null,'HTML footer',null,null,null,1,1,0,'--footer','footer string in Html code (-%F "Mirrored [from host %s [file %s [at %s]]]"',10,'-',' ','%F ','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (2,38,null,'Preferred language',null,null,null,1,1,0,'--language','(eg. "fr, en, jp, *")',10,'-',' ','%l ','textbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,39,null,'Cache usage',null,null,null,0,0,0,'--cache','Create/use a cache for updates and retries',11,'-',' ','C',null);
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,40,null,'Store ALL files in cache',null,1,null,0,0,0,'--store-all-in-cache','(Not useful if files on disk)',11,'-',' ','k','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,41,null,'Do not re-download locally erased files',null,1,null,1,0,0,'--do-not-recatch',null,11,'-',' ','%n','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,42,null,'Display on screen filenames downloaded',null,1,null,0,0,0,'--display','(in realtime)',11,'-',' ','%v','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,43,null,'No log',null,1,null,0,0,0,'--do-not-log','Quiet mode',11,'-',' ','Q','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,44,null,'No Questions',null,1,null,0,0,0,'--quiet','Quiet mode',11,'-',' ','q','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,45,null,'Extra logging info',null,1,null,0,0,0,'--extra-log',null,11,'-',' ','z','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,46,null,'Debug logging',null,1,null,0,0,0,'--debug-log',null,11,'-',' ','Z','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,47,null,'Log to screen',null,1,null,0,0,0,'--verbose',null,11,'-',' ','v','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,48,null,'Log in files',null,1,null,0,0,0,'--file-log',null,11,'-',' ','f','checkbox');
Insert into COMMAND_LINE_OPT (ACCESS_LEVEL,COMMAND_LINE_OPTION_ID,DEFAULT_VALUE,DISPLAY_NAME,EXPLANATION,HIDE_ARGUMENT,HIDE_OPTION,IS_ACTIVE,IS_ARGUMENT_QUOTED,IS_MANDATORY,LONG_OPTION,OPTION_DESCRIPTION,OPTION_GROUP_ID,OPTION_PREFIX,OPTION_SEPARATOR,SHORT_OPTION,UI_ELEMENT) values (0,49,null,'Make an index',null,null,null,0,0,0,'--index',null,11,'-',' ','I','checkbox');
alter sequence REASON_SEQ increment by 82;

Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('w','Download web site(s)',1,1);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('g','Get separated files',1,2);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('i','Continue interrupted download',1,3);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Y','Mirror ALL links located on top level pages',1,4);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t use HTTP proxy for FTP',3,5);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Use HTTP proxy for FTP',3,6);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Never',18,7);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','After timeout',18,8);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2','When slow',18,9);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('3','After timeout or when slow',18,10);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Use extended parsing',19,11);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t use extended parsing',19,12);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,20,13);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,20,14);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,21,15);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,21,16);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Site-structure',22,17);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','HTML in web/, images/other files in web/images/',22,18);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2','HTML in web/HTML, images/other in web/images',22,19);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('3','HTML in web/, images/other in web/',22,20);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('4','HTML in web/, images/other in web/xxx, where xxx is the file extension',22,21);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('5','Images/other in web/xxx and HTML in web/HTML',22,22);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('99','All files in web/, with random names (gadget!)',22,23);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('100','Site-structure, without www.domain.xxx/',22,24);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('101',' HTML in sitename/, images/other files in sitename/images/',22,25);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('102','HTML in sitename/, images/other in sitename/images',22,26);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('103','HTML in sitename/, images/other in sitename/',22,27);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('104','HTML in sitename/, images/other in sitename/xxx, where xxx is the file extension',22,28);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('105','HTML in sitename/, Images/other in sitename/xxx and HTML in sitname/HTML',22,29);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('199','All files in sitename/, with random names',22,30);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1001','No "web" directory, images/other files in web/images/',22,31);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1002','No "web" directory, HTML in web/HTML, images/other in web/images',22,32);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1003','No "web" directory, images/other in web/',22,33);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1004','No "web" directory, images/other in xxx, where xxx is the file extension',22,34);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1005','Images/other in xxx and HTML in HTML/, where xxx is the file extension',22,35);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1099','All files in single directory with random names',22,36);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Long names',23,37);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','8-3 conversion',23,38);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Relative link',24,39);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,'Absolute links',24,40);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('3','Absolute URI links',24,41);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,25,42);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,25,43);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,26,44);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Include passwords for external password protected sites',26,45);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,27,46);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t include query string',27,47);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Do not accept cookies in cookies.txt',28,48);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Accept cookies in cookies.txt',28,49);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Never',29,50);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','If unknown (except directories)',29,51);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2','If unknown',29,52);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Parse Java classes',30,53);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t parse Java classes',30,54);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Never',31,55);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Sometimes',31,56);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2','Always',31,57);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Force old HTTP/1.0 requests',32,58);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t force',32,59);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,33,60);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,33,61);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,34,62);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,34,63);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)','Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)',36,64);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)','Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)',36,65);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)','Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)',36,66);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.0 (compatible; MSIE 5.0; Win32)','Mozilla/4.0 (compatible; MSIE 5.0; Win32)',36,67);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 (compatible; MSIE 4.01; Windows 98)','Mozilla/4.5 (compatible; MSIE 4.01; Windows 98)',36,68);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 (compatible; MSIE 4.01; Windows 95)','Mozilla/4.5 (compatible; MSIE 4.01; Windows 95)',36,69);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 (compatible; MSIE 4.01; Windows NT)','Mozilla/4.5 (compatible; MSIE 4.01; Windows NT)',36,70);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.78 [en] (Windows NT 5.0; U)','Mozilla/4.78 [en] (Windows NT 5.0; U)',36,71);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.61 [en] (Win98; I)','Mozilla/4.61 [en] (Win98; I)',36,72);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.1) Gecko/20020826','Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.1) Gecko/20020826',36,73);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/3.0 (Win95; I)','Mozilla/3.0 (Win95; I)',36,74);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/2.0 (compatible; MSIE 3.01; Windows 95)','Mozilla/2.0 (compatible; MSIE 3.01; Windows 95)',36,75);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 [en] (X11; I; Linux 2.0.34 i686)','Mozilla/4.5 [en] (X11; I; Linux 2.0.34 i686)',36,76);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.06C-EMS-1.4 [en] (X11; U; SunOS 5.5.1 sun4m)','Mozilla/4.06C-EMS-1.4 [en] (X11; U; SunOS 5.5.1 sun4m)',36,77);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 (Macintosh; I; PPC)','Mozilla/4.5 (Macintosh; I; PPC)',36,78);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/3.01-C-MACOS8 (Macintosh; I; PPC)','Mozilla/3.01-C-MACOS8 (Macintosh; I; PPC)',36,79);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/2.0 (OS/2; I)','Mozilla/2.0 (OS/2; I)',36,80);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 [en] (X11; U; SunOS 5.6 sun4u)','Mozilla/4.5 [en] (X11; U; SunOS 5.6 sun4u)',36,81);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 [en] (X11; I; AIX 4.1)','Mozilla/4.5 [en] (X11; I; AIX 4.1)',36,82);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 [en] (X11; I; FreeBSD 2.2.6-RELEASE i386)','Mozilla/4.5 [en] (X11; I; FreeBSD 2.2.6-RELEASE i386)',36,83);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/3.01SGoldC-SGI (X11; I; IRIX 6.3 IP32)','Mozilla/3.01SGoldC-SGI (X11; I; IRIX 6.3 IP32)',36,84);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/3.0 WebTV/1.2 (compatible; MSIE 2.0)','Mozilla/3.0 WebTV/1.2 (compatible; MSIE 2.0)',36,85);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/2.0 (compatible; MS FrontPage Express 2.0)','Mozilla/2.0 (compatible; MS FrontPage Express 2.0)',36,86);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.05 [fr] (Win98; I)','Mozilla/4.05 [fr] (Win98; I)',36,87);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Lynx/2.8rel.3 libwww-FM/2.14','Lynx/2.8rel.3 libwww-FM/2.14',36,88);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Java1.1.4','Java1.1.4',36,89);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('Mozilla/4.5 (compatible; HTTrack 3.0x; Windows 98)','Mozilla/4.5 (compatible; HTTrack 3.0x; Windows 98)',36,90);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('HyperBrowser (Cray; I; OrganicOS 9.7.42beta-27)','HyperBrowser (Cray; I; OrganicOS 9.7.42beta-27)',36,91);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('HTTrack/3.0x','HTTrack/3.0x',36,92);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('HTTrack Website Copier/3.0x (offline browser; web mirror utility)','HTTrack Website Copier/3.0x (offline browser; web mirror utility)',36,93);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','No cache',39,94);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Cache is priority',39,95);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2','Test update before',39,96);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,40,97);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,40,98);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,41,99);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,41,100);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,42,101);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,42,102);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,43,103);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,43,104);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,44,105);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,44,106);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,45,107);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,45,108);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,46,109);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,46,110);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,47,111);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,47,112);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,48,113);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,48,114);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,49,115);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t make an index',49,116);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,50,117);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t make a searchable index',50,118);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Just scan',51,119);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1','Store html files',51,120);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2','Store non html files',51,121);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('3','Store all files',51,122);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('7','Store html files first',51,123);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('S','Stay in the same directory',52,124);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('D','Can go down',52,125);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('U','Can go up',52,126);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('B','Can both go up & down',52,127);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('a','Stay on the same address',53,128);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('d','Stay on the same domain',53,129);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('l','Stay on the same top level domain',53,130);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('e','Go everywhere on the web',53,131);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,54,132);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,56,133);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t make a searchable index',56,134);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,59,135);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,60,136);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0','Don''t make a searchable index',60,137);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,61,138);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,62,139);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,63,140);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,64,141);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,65,142);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('9999',null,5,143);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('0',null,6,144);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('10',null,11,145);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('8',null,11,146);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1',null,16,147);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('50',null,5,148);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('3',null,16,149);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('1000000',null,13,150);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('30',null,15,151);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('150',null,17,152);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('4',null,14,153);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('2000000000',null,8,154);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('172800',null,9,155);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values ('50000',null,10,156);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (null,null,36,157);
Insert into OPTION_ARGUMENT (ARGUMENT,ARGUMENT_DESCRIPTION,COMMAND_LINE_OPTION_ID,OPTION_ARGUMENT_ID) values (' 104857600,104857600',null,7,158);
alter sequence OPTION_ARGUMENT_SEQ increment by 159;


Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (1,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (143,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (144,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (145,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (147,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (17,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (37,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (39,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (48,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (51,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (57,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (96,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (116,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (118,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (122,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (125,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (128,1);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (1,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (148,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (144,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (145,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (149,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (17,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (37,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (39,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (49,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (51,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (55,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (96,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (122,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (125,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (128,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (150,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (151,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (152,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (7,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (153,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (154,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (155,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (156,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (14,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (62,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (158,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (59,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (60,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (11,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (15,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (45,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (43,2);
Insert into OPT_ARG_PROFILE (OPTION_ARGUMENT_ID,PROFILE_ID) values (116,2);


Insert into ORGANISATION(ALIAS,AGENCY_ID,AUDIT_DATE,AUDIT_USERID,COMMENTS,LONGCOUNTRY,EMAIL,FAX,INDEXER_ID,LINE1,LINE2,LOCALITY,MOBILE_PHONE,NAME,ORGANISATION_ID,PHONE,POSTCODE,PUBLISHER_ID,SERVICE_ID,LONGSTATE,URL) values ('EG',null,null,null,null,null,null,null,null,'Example Place','Example Place','Example',null,'Default Agency',1,null,'1234',null,null,'EXPL','http://www.example.org/');
Insert into AGENCY (AGENCY_ID,EXTERNAL_EMAIL,FORM_LETTER_URL,LOCAL_DATABASE_PREFIX,LOCAL_REFERENCE_PREFIX,ORGANISATION_ID) values (1,'agency@example.org','http://example.org/manual/general_procedures.html',null,'',1);
Insert into PUBLISHER (LOCAL_REFERENCE,NOTES,ORGANISATION_ID,PUBLISHER_ID,PUBLISHER_TYPE_ID) values (null,null,1,1,1);
update ORGANISATION set AGENCY_ID = 1, PUBLISHER_ID = 1 where ORGANISATION_ID = 1;

alter sequence ORGANISATION_SEQ increment by 2;
alter sequence AGENCY_SEQ increment by 2;
alter sequence PUBLISHER_SEQ increment by 2;


Insert into INDIVIDUAL (AUDIT_CREATE_DATE,AUDIT_CREATE_USERID,AUDIT_DATE,AUDIT_USERID,COMMENTS,EMAIL,FAX,FUNCTION,INDIVIDUAL_ID,IS_ACTIVE,MOBILE_PHONE,NAME_FAMILY,NAME_GIVEN,NAME_TITLE,PASSWORD,PHONE,URL,USERID) values (null,null,null,null,null,null,null,null,1,1,null,'Administrator','System',null,'admin',null,null,'admin');
alter sequence INDIVIDUAL_SEQ increment by 2;

Insert into ROLE (AUDIT_CREATE_DATE,AUDIT_DATE,AUDIT_USERID,COMMENTS,INDIVIDUAL_ID,ORGANISATION_ID,ROLE_ID,ROLE_TITLE,ROLE_TYPE) values (null,null,null,'These are the people that work directly with the PANDAS software as well as addressing any issues that may occur',1,1,1,'Pandas Administrator','PanAdmin');
alter sequence ROLE_SEQ increment by 2;
