create table individual
(
    audit_create_date      timestamp comment 'The date and time on which this individual was created',
    audit_create_userid    bigint comment 'The system user who created this individual',
    audit_date             timestamp comment 'The date and time on which this individual was last updated',
    audit_userid           bigint comment 'The system user who last updated this individual',
    comments               varchar(100) comment 'Notes about this individual',
    email                  varchar(120) comment 'The email address for this individual',
    fax                    varchar(25) comment 'The fax number for this individual',
    `function`             varchar(120),
    individual_id          bigint not null auto_increment primary key comment 'Sequence generated ID for this individual',
    is_active              bigint,
    mobile_phone           varchar(25) comment 'The mobile phone number for this individual',
    name_family            varchar(30) comment 'The family (last) name of this individual',
    name_given             varchar(130) comment 'The given (first) name of this individual',
    name_title             varchar(12) comment 'The title of this individual (eg. Mr, Mrs)',
    password               varchar(100) comment 'The password for this individual''s user account',
    phone                  varchar(25) comment 'The phone number for this individual',
    url                    varchar(1024) comment 'A URL for this individual',
    userid                 varchar(20) comment 'The username for this individual''s user account',
    alt_contact_id         bigint comment 'An alternative contact for curators when sending template emails',
    email_signature        text comment 'Text to append to emails sent by this curator',
    pwdigest               varchar(100),
    prefers_sticky_filters tinyint comment 'Whether the user prefers sticky filters in UI'
) comment ='A person who is connected to the archive. These could be system users, or contact people linked to indexers, publishers or titles';

create table organisation
(
    alias           varchar(500) comment 'Abbreviation or alternate name for an organisation',
    agency_id       bigint comment 'Foreign key to the partner agency which this set of organisation details belongs to. Mutually exclusive with indexer_id and publisher_id',
    audit_date      timestamp comment 'The date and time on which this table was last updated',
    audit_userid    bigint comment 'The user who last updated this table',
    comments        varchar(100) comment 'Any notes on this organisation',
    longcountry     varchar(100) comment 'The full country name used in this organisation''s primary address',
    email           varchar(100) comment 'The email address for this organisation',
    fax             varchar(20) comment 'The fax number for this organisation',
    indexer_id      bigint comment 'Foreign key to the indexing agency which this set of organisation details belongs to. Mutually exclusive with agency_id and publisher_id',
    line1           varchar(200) comment 'First line of this organisation''s primary address.',
    line2           varchar(200) comment 'Second line of this organisation''s primary address.',
    locality        varchar(46) comment 'Suburb or town of this organisation''s primary address',
    mobile_phone    varchar(20) comment 'Mobile phone contact number for this organisation',
    name            varchar(256) comment 'The name of this organisation',
    organisation_id bigint not null auto_increment primary key comment 'Sequence generated ID for an organisation',
    phone           varchar(20) comment 'Phone number for an organisation',
    postcode        varchar(10) comment 'postcode for this organisation''s primary address',
    publisher_id    bigint comment 'Foreign key to the publishing agency which this set of organisation details belongs to. Mutually exclusive with agency_id and indexer_id',
    service_id      bigint,
    longstate       varchar(100) comment 'The state for this organisation''s primary address',
    url             varchar(1024) comment 'This organisation''s internet address or webpage',
    abn             varchar(11)
) comment ='An organisation connected to the archive. These could be archive partners, indexing agencies, or publishing organisations responsible for web content.';

create table agency
(
    agency_id              bigint not null auto_increment primary key comment 'Sequence generated Agency primary key',
    external_email         varchar(64) comment 'Email address for this agency, to be displayed to the public',
    form_letter_url        varchar(4000) comment 'The URL this agency uses to access any form letters they wish to use to communicate with publisher and title contacts.',
    local_database_prefix  varchar(64),
    local_reference_prefix varchar(64),
    logo                   blob comment 'This agency''s logo image file',
    organisation_id        bigint comment 'The organisation table which corresponds to this agency',
    legal_deposit          bigint default 0,
    transfer_contact_id    bigint,
    constraint agency_organisation_fk foreign key (organisation_id) references organisation (organisation_id),
    constraint fk_agency_transfer_contact foreign key (transfer_contact_id) references individual (individual_id) on delete set null
) comment ='A partner agency who is involved in selecting and archiving titles';

create table agency_area
(
    agency_area_id bigint not null auto_increment primary key comment 'Sequence generated agency area id',
    agency_id      bigint comment 'Foreign key to the agency which maintains this area',
    area_name      varchar(256) comment 'Name or label for the physical location being referred to',
    area_wording   varchar(2048),
    constraint agency_area_agency_fk foreign key (agency_id) references agency (agency_id)
) comment ='A location associated with an agency. It can be used when restricting titles so that they can only be viewed from particular areas. Each area is a label given to a set of IP addresses. Eg."National Library Reading Room"';

create table agency_area_ip
(
    address           varchar(256) comment 'An IP address located within a particular agency area',
    agency_area_id    bigint comment 'Foreign key to the agency area this IP address is located in',
    agency_area_ip_id bigint not null auto_increment primary key comment 'sequency generated id for this agency area ip',
    mask              varchar(256),
    constraint agency_area_ip_agencyarea_fk foreign key (agency_area_id) references agency_area (agency_area_id)
) comment ='An IP address that is located in a listed agency area. This is used to restrict access to a title.';

create table indexer
(
    do_notify       bigint comment 'Whether to notify this indexing agency when significant events occur, or not',
    indexer_id      bigint not null auto_increment primary key comment 'Sequence generated ID for an indexing agency',
    note            varchar(4000) comment 'Freeform notes about an indexing agency',
    organisation_id bigint comment 'Foreign key to the associated organisation',
    constraint indexer_organisation_fk foreign key (organisation_id) references organisation (organisation_id)
) comment ='An indexing agency who nominates titles, either by external methods of communicating with system users, or directly using informational user accounts linked to indexers rather than partner agencies';

create table publisher_type
(
    publisher_description varchar(4000) comment 'Description of available publisher types',
    publisher_type        varchar(256) not null comment 'Name of available publisher types',
    publisher_type_id     bigint       not null auto_increment primary key comment 'Sequence generated ID for this publisher type',
    domain_suffixes       varchar(512)
) comment ='Lookup table for the kind of publishing organisations available. eg. government, commercial, ...';

create table publisher
(
    local_reference   varchar(256) comment 'The local reference number for this publisher. For the NLA, this will be a TRIM file number.',
    notes             varchar(4000) comment 'Notes about this publisher',
    organisation_id   bigint not null comment 'Foreign key to the organisation details for this publisher',
    publisher_id      bigint not null auto_increment primary key comment 'Sequence generated ID for this publisher',
    publisher_type_id bigint comment 'Foreign key to the type of organisation this publisher is',
    constraint publisher_organisation_fk foreign key (organisation_id) references organisation (organisation_id),
    constraint publisher_publishertype_fk foreign key (publisher_type_id) references publisher_type (publisher_type_id)
) comment ='An organisation (which may consist of a single person) that holds the copyright to one or more titles.';

-- Constraints that would be circular
alter table organisation
    add constraint organisation_agency_fk
        foreign key (agency_id) references agency (agency_id),
    add constraint organisation_indexer_fk
        foreign key (indexer_id) references indexer (indexer_id),
    add constraint organisation_publisher_fk
        foreign key (publisher_id) references publisher (publisher_id);

create table `condition`
(
    condition_description varchar(4000) comment 'An explanation of what each condition value means',
    condition_id          bigint        not null auto_increment primary key comment 'A sequence generated id for a condition',
    name                  varchar(1024) not null comment 'The name or label for a particular condition/status'
) comment ='The status lookup table for title display restrictions. Eg. enabled, disabled, expired';


create table format
(
    format_id bigint      not null auto_increment primary key comment 'Sequence generated ID for a format',
    name      varchar(64) not null comment 'A label for a type of title format'
) comment ='Lookup table for the format of a title. Eg. mono, integrating, serial';

create table permission_type
(
    permission_type    varchar(256) not null,
    permission_type_id bigint       not null auto_increment primary key
) comment ='Lookup table for the types of permissions available - title level or blanket';

create table permission_state
(
    permission_state    varchar(256) not null,
    permission_state_id bigint       not null auto_increment primary key
) comment ='Lookup table for possible permission states. Eg. granted, unknown';

create table permission
(
    domain                 varchar(4000) comment 'The web domain a publisher blanket permission applies to. eg. www.act.com.au',
    individual_id          bigint comment 'The contact person who granted or denied this permission',
    is_blanket             bigint,
    local_reference        varchar(16) comment 'The local reference number for files or record pertaining to this permission(within the NLA, this will be a trim file number)',
    note                   varchar(4000) comment 'Any notes or extra conditions for this permission',
    permission_description varchar(256),
    permission_id          bigint not null auto_increment primary key comment 'Sequence generated ID for a permission',
    permission_state       varchar(64),
    permission_state_id    bigint comment 'Foreign key to the status of this permission. eg Granted, Denied',
    permission_type        varchar(50),
    permission_type_id     bigint comment 'Foreign key to the type of permission, ie. publisher (blanket) level or title level',
    publisher_id           bigint comment 'Foreign key to the publisher who has the authority to grant this permission, if this is a blanket permission.',
    status_set_date        timestamp comment 'The date on which this permission''s status was determined',
    title_id               bigint comment 'Foreign key to the title this permission refers to (if it is a title level permission)'
) comment ='Information about whether the publisher of a title has granted or denied access to archived versions of that title, or to a group of related titles which they have the rights to.';

create table status
(
    status_id   bigint not null auto_increment primary key comment 'Sequence generated ID for this title status',
    status_name varchar(128) comment 'The name or title of this title status'
) comment ='A lookup table for the values available for title status. Eg. selected, nominated, ...';


create table old_title_standing
(
    title_standing_id   bigint not null auto_increment primary key comment 'Sequence generated ID for a title standing from Pv2',
    title_standing_name varchar(256) comment 'A possible title standing from Pv2'
) comment ='Lookup table for title standings used in the previous version of PANDAS (version 2).';

create table old_title_status
(
    title_status_id    bigint not null auto_increment primary key comment 'Sequence generated ID for a title status from Pv2',
    title_status_name  varchar(256) comment 'A possible title status from Pv2',
    title_status_notes varchar(4000) comment 'Description of a possible title status from Pv2'
) comment ='Lookup table for title statuses used in the previous version of PANDAS (version 2).';

create table gather_method
(
    gather_method_id bigint not null auto_increment primary key comment 'Sequence generated ID for a gather method',
    method_desc      varchar(256) comment 'A description of a type of gather method',
    method_name      varchar(256) comment 'A label for a type of gather method'
) comment ='Lookup table for available methods of gathering a title';

create table profile
(
    name                varchar(512) not null comment 'The label for this predefined set of gather options',
    profile_description varchar(2048) comment 'A description of the settings stored in this predefined set of gather options',
    profile_id          bigint       not null auto_increment primary key comment 'Sequence generated ID for this gather profile',
    is_default          tinyint      not null comment 'Whether this profile is the default template',
    heritrix_config     text comment 'Heritrix configuration for this profile',
    gather_method_id    bigint comment 'Foreign key to the gather method used by this profile',
    crawl_limit_bytes   bigint comment 'Maximum total bytes to crawl',
    crawl_limit_seconds bigint comment 'Maximum total seconds for the crawl',
    browsertrix_config  text comment 'Browsertrix configuration for this profile',
    constraint profile_gather_method_fk foreign key (gather_method_id) references gather_method (gather_method_id)
) comment ='A named set/template of settings to control crawl behaviour.';

create table gather_schedule
(
    gather_schedule_id bigint       not null auto_increment primary key comment 'Sequence generated ID for a recurring gather frequency',
    schedule_name      varchar(256) not null comment 'A frequency available for recurring gather schedules',
    years              bigint       not null default 0,
    months             bigint       not null default 0,
    days               bigint       not null default 0,
    days_of_week       bigint       not null default 0,
    hours_of_day       bigint       not null default 0
) comment ='Lookup table of frequencies available for scheduling a recurring gather.';

create table scope
(
    scope_id           bigint       not null auto_increment primary key,
    depth              bigint,
    name               varchar(255) not null,
    include_subdomains tinyint      not null default 0
);

create table title_gather
(
    active_profile_id  bigint comment 'Foreign key to the gather profile currently applied to this title (if any)',
    authenticate_ip    bigint,
    authenticate_user  bigint,
    cal_start_date     timestamp,
    first_gather_date  timestamp comment 'The date on which this title was first gathered',
    gather_method_id   bigint comment 'Foreign key to the method used to gather this title',
    gather_schedule_id bigint comment 'Foreign key to the frequency of recurring gathers for this title',
    gather_url         varchar(4000),
    last_gather_date   timestamp comment 'The most recent date on which this title was gathered',
    next_gather_date   timestamp comment 'The next date on which this title will be gathered (could be a recurring or non-recurring gather)',
    notes              varchar(4000) comment 'Notes about gathering this title',
    password           varchar(128),
    queued             bigint,
    is_scheduled       bigint comment 'Controls whether the title gather will take place or not',
    scheduled_date     timestamp comment 'The next recurring gather date for this title',
    title_id           bigint  not null auto_increment primary key comment 'Foreign key to the title these gather details are for',
    username           varchar(128),
    gather_command     text,
    scope_id           bigint comment 'Foreign key to the scope for this gather',
    additional_urls    text,
    ignore_robots_txt  tinyint not null default 0 comment 'Whether to ignore robots.txt directives'
) comment ='Information about the gather settings and options for a title';

create index title_gather_next_gather_date
    on title_gather (next_gather_date);

create table title
(
    agency_id              bigint,
    anbd_number            varchar(22) comment 'Australian National Bibliographic Database catalogue record identifier for this archived resource',
    awaiting_confirmation  bigint       not null default 0 comment 'Flags whether this title is waiting to be acknowledged after a transfer of ownership',
    content_warning        varchar(256),
    current_owner_id       bigint,
    current_status_id      bigint,
    default_permission_id  bigint,
    disappeared            bigint       not null default 0,
    format_id              bigint,
    indexer_id             bigint,
    is_cataloguing_not_req bigint       not null default 0,
    is_subscription        bigint       not null default 0,
    legacy_purl            varchar(1024),
    local_database_no      varchar(25),
    local_reference        varchar(25),
    name                   varchar(256) comment 'The name or heading of this title',
    notes                  varchar(4000),
    permission_id          bigint,
    pi                     bigint,
    publisher_id           bigint,
    reg_date               timestamp,
    seed_url               varchar(1024),
    short_display_name     varchar(256),
    tep_id                 bigint,
    title_id               bigint       not null auto_increment primary key comment 'Sequence generated ID for this title',
    title_resource_id      bigint,
    standing_id            bigint,
    status_id              bigint,
    title_url              varchar(1024),
    unable_to_archive      bigint       not null default 0,
    legal_deposit          bigint       not null default 0,
    last_modified_date     timestamp not null default current_timestamp,

    constraint title_agency_fk foreign key (agency_id) references agency (agency_id),
    constraint title_currentowner_fk foreign key (current_owner_id) references individual (individual_id),
    constraint title_currentstatus_fk foreign key (current_status_id) references status (status_id),
    constraint title_defaultpermission_fk foreign key (default_permission_id) references permission (permission_id),
    constraint title_format_fk foreign key (format_id) references format (format_id),
    constraint title_indexer_fk foreign key (indexer_id) references indexer (indexer_id),
    constraint title_permission_fk foreign key (permission_id) references permission (permission_id),
    constraint title_publisher_fk foreign key (publisher_id) references publisher (publisher_id),
    constraint title_oldtitlestanding_fk foreign key (standing_id) references old_title_standing (title_standing_id),
    constraint title_oldtitlestatus_fk foreign key (status_id) references old_title_status (title_status_id)
) comment ='An online resource selected for archiving';

create index title_pi on title (pi);
create index title_title_url on title (title_url(512));
create index title_last_modified_date on title (last_modified_date);

create table copyright_type
(
    copyright_type    varchar(64) not null,
    copyright_type_id bigint      not null auto_increment primary key
) comment ='Lookup table for the type of copyright/disclaimer URL and note displayed on a title''s TEP';

create table tep
(
    copyright_note      varchar(4000),
    copyright_type_id   bigint,
    copyright_url       varchar(1024),
    display_date        timestamp,
    display_title       varchar(4000),
    do_collection       bigint,
    do_search           bigint,
    do_subject          bigint,
    general_note        varchar(4000),
    has_copyright       bigint,
    has_disclaimer      bigint,
    hit_count           bigint,
    is_published        bigint,
    tep_id              bigint  not null auto_increment primary key comment 'Sequence generated ID for this TEP',
    title_id            bigint  not null comment 'Foreign key to the title which this TEP displays',
    metadata            text,
    publish_immediately tinyint not null default 0,

    constraint tep_title_fk foreign key (title_id) references title (title_id),
    constraint tep_copyright_fk foreign key (copyright_type_id) references copyright_type (copyright_type_id),
    constraint title_id_unique unique (title_id)
) comment ='Information about, or to be displayed on, a Title Entry Page (TEP)';

create table issue_group
(
    issue_group_id    bigint not null auto_increment primary key comment 'Sequence generated ID for an issue group',
    name              varchar(256) comment 'The name or label of an issue group',
    notes             varchar(4000) comment 'Any display notes or description for this group of issues',
    issue_group_order bigint comment 'The display order for this issue group, compared to other issue groups for a particular title',
    tep_id            bigint,
    constraint issue_group_titleentrypage_fk foreign key (tep_id) references tep (tep_id)
) comment ='A label under which to group issues (ARCH_ISSUE) on the title''s TEP';

create table state
(
    state_id   bigint not null auto_increment primary key comment 'Sequence generated ID for an instance state',
    state_name varchar(64) comment 'Possible instance states'
) comment ='A lookup table for the values available for instance state. Eg. gathered, deleted, ...';

create unique index state_name_idx
    on state (state_name);

create table old_instance_status
(
    instance_status_id   bigint not null auto_increment primary key comment 'Sequence generated ID for an instance status from Pv2',
    instance_status_name varchar(256) comment 'A possible instance status from Pv2'
) comment ='Lookup table for instance statuses used in the previous version of PANDAS (version 2).';

create table old_instance_state
(
    instance_state_id   bigint not null auto_increment primary key comment 'Sequence generated ID for an instance state from Pv2',
    instance_state_name varchar(256) comment 'A possible instance state from Pv2'
) comment ='Lookup table for instance states used in the previous version of PANDAS (version 2).';

create table instance
(
    current_state_id      bigint comment 'Foreign key to the state of this instance',
    display_note          varchar(4000) comment 'A note to be displayed with this instance on the Title TEP',
    gather_method_name    varchar(256) comment 'The method used to gather this instance',
    gathered_url          varchar(1024) comment 'The Seed URL used for this instance',
    instance_date         timestamp comment 'The date and time at which this instance was gathered',
    instance_id           bigint       not null auto_increment primary key comment 'Sequence generated ID for the title instance',
    instance_state_id     bigint comment 'Foreign key to the old Pv2 state for this instance',
    instance_status_id    bigint comment 'Foreign key to the old Pv2 status for this instance',
    is_displayed          bigint comment 'Indicated whether this instance is to be displayed on the Title''s TEP or not',
    prefix                varchar(256) comment 'The folder prefix used when accessing the display version of this instance',
    processable           bigint comment 'Whether this instance can be processed (legacy instances may not be processable)',
    removeable            bigint comment 'Whether this instance can be removed (legacy instances may not be supported for removal by the current framework)',
    resource_id           bigint,
    restrictable          bigint comment 'Whether this instance can have restrictions placed on it using the current restricter engine. (Due to different filesystem structures used with pre-pandas archiving, some instances can not be restricted)',
    restriction_enabled_t bigint,
    tep_url               varchar(1024) comment 'The URL to be used on the TEP for this instance',
    title_id              bigint comment 'Foreign key to the title this instance belongs to',
    transportable         bigint comment 'Whether transporting this instance is supported or not. (legacy instances may not be supported for removal by the current framework)',
    type_name             varchar(256) comment 'Which pandas system created this instance (pre-pandas, pandas 1, etc)',
    gather_command        text,
    profile_id            bigint,
    scope_id              bigint,
    last_modified_date    timestamp not null default current_timestamp comment 'Last modification timestamp'
) comment ='A snapshot of the online resource, including status, date and summary statistics.';

create index instance_tep_url_title_id
    on instance (tep_url(512), title_id);

create index instance_last_modified_date
    on instance (last_modified_date);

create index instance_date_index
    on instance (current_state_id, title_id, instance_date);


create table ins_resource
(
    display_url  varchar(1024),
    gathered_url varchar(1024),
    instance_id  bigint not null auto_increment primary key,
    local_url    varchar(1024),
    constraint ins_resource_instance_fk foreign key (instance_id) references instance (instance_id)
);

create table ins_gather
(
    gather_files  bigint comment 'The number of files gathered for this instance',
    gather_finish timestamp comment 'The date and time this instance finished gathering',
    gather_rate   bigint comment 'The average download rate for this instance''s gather (bytes/s)',
    gather_size   bigint comment 'How large the gather for this instance is (bytes)',
    gather_start  timestamp comment 'The date and time this instance started gathering',
    gather_time   bigint comment 'How long the gather for this instance took',
    instance_id   bigint not null auto_increment primary key,
    constraint ins_gather_instance_fk foreign key (instance_id) references instance (instance_id)
) comment ='Gather statistics for a particular title instance.';

create table arch_issue
(
    instance_id    bigint comment 'Sequence generated ID for an issue',
    is_displayed   bigint,
    issue_group_id bigint comment 'Foreign key to the group which contains this issue',
    issue_id       bigint not null auto_increment primary key comment 'Sequence generated ID for an issue',
    issue_order    bigint comment 'The display order for this issue within its group',
    title          varchar(1024) comment 'The label for this issue',
    url            varchar(1024) comment 'The URL for this issue within the display system. This will be a point somwehere within its related instance',
    constraint arch_issue_instance_fk foreign key (instance_id) references instance (instance_id),
    constraint arch_issue_issuegroup_fk foreign key (issue_group_id) references issue_group (issue_group_id)
) comment ='An alternate entry point into a archived instance. This is published in the display system in addition to the instance''s main entry URL.';

create index arch_issue_url_instance_id
    on arch_issue (url(512), instance_id);

create table auth_group
(
    auth_group_id bigint not null auto_increment primary key,
    group_name    varchar(256)
) comment ='A group of usernames which are used to restrict who can view an archived title.';

create table auth_restr
(
    condition_date      timestamp comment 'The date the current condition (status) for this restriction was applied',
    condition_id        bigint comment 'Foreign key to the status associated with this restriction (enabled, disabled, etc)',
    title_auth_restr_id bigint not null auto_increment primary key comment 'Sequence generated id for an authorisation restriction',
    title_id            bigint comment 'Foreign key to the title this authorisation restriction refers to',
    constraint auth_restr_condition_fk foreign key (condition_id) references `condition` (condition_id)
) comment ='An authorisation restriction which can be applied to a title to restrict who can view the archived file/s.';

create table auth_res_group
(
    auth_group_id    bigint not null,
    auth_restrict_id bigint not null,
    primary key (auth_group_id, auth_restrict_id),
    constraint auth_res_group_authgroup_fk foreign key (auth_group_id) references auth_group (auth_group_id),
    constraint auth_res_group_authrest_fk foreign key (auth_restrict_id) references auth_restr (title_auth_restr_id)
) comment ='The relationship between an authorisation restriction and the group of usernames associated with it.';

create table auth_user
(
    auth_group_id bigint comment 'Foreign key to auth_group',
    auth_user_id  bigint not null auto_increment primary key,
    individual_id bigint,
    password      varchar(128),
    username      varchar(128),
    constraint auth_user_authgroup_fk foreign key (auth_group_id) references auth_group (auth_group_id),
    constraint auth_user_individual_fk foreign key (individual_id) references individual (individual_id)
) comment ='A username and password associated with an authorisation group. These groups can be associated with an authorisation restriction for a title.';

create table attachment_archiver_state
(
    id               bigint not null auto_increment primary key,
    resumption_token varchar(255),
    warc_id          bigint,
    warc_offset      bigint
);

create table thumbnail
(
    id                 bigint        not null auto_increment primary key comment 'Sequence generated ID for this thumbnail',
    title_id           bigint comment 'Foreign key to the title for this thumbnail',
    status             bigint        not null comment 'Thumbnail capture status',
    priority           bigint        not null comment 'Thumbnail display priority',
    url                varchar(4000),
    capture_date       timestamp  not null comment 'When the thumbnail was captured',
    source_type        varchar(4000),
    width              bigint        not null,
    height             bigint        not null,
    crop_x             bigint        not null,
    crop_y             bigint        not null,
    crop_width         bigint        not null,
    crop_height        bigint        not null,
    content_type       varchar(4000) not null,
    data               blob          not null,
    created_date       timestamp  not null,
    last_modified_date timestamp  not null,

    constraint thumbnail_title_fk foreign key (title_id) references title (title_id) on delete cascade
) comment ='Lookup table of thumbnail images for titles, with capture metadata';

create table subject
(
    subject_id        bigint       not null auto_increment primary key comment 'Sequence generated ID for this subject',
    subject_name      varchar(256) not null comment 'The name or label for this subject',
    subject_parent_id bigint comment 'Foreign key to the parent subject',
    thumbnail_url     text,
    description       text,
    thumbnail_id      bigint comment 'Foreign key to a thumbnail for this subject',
    icon              blob,

    constraint subject_subject_fk foreign key (subject_parent_id) references subject (subject_id),
    constraint subject_thumbnail_id_fk foreign key (thumbnail_id) references thumbnail (id)
) comment ='A lookup table of topics that a title or a collection of titles may belong to.';

create table subject_titles
(
    subject_id bigint not null comment 'Foreign key to a subject this title belongs to',
    title_id   bigint not null comment 'Foreign key to a title this subject contains',
    primary key (subject_id, title_id),

    constraint subject_titles_subjects_fk foreign key (subject_id) references subject (subject_id),
    constraint subject_titles_title_fk foreign key (title_id) references title (title_id)
) comment ='The relationship between titles and subjects (many-to-many).';

create table col
(
    col_id             bigint       not null auto_increment primary key comment 'Sequence generated id for a collection',
    display_comment    varchar(4000) comment 'Description for a collection',
    display_order      bigint comment 'Order in which the collection will be displayed (Not used??)',
    is_displayed       bigint comment 'Can prevent a collection from appearing in the display system',
    name               varchar(256) not null comment 'The name of a collection',
    col_parent_id      bigint comment 'Optional foreign key to the parent collection',
    thumbnail_url      text,
    thumbnail_id       bigint comment 'Foreign key to the thumbnail for this collection',
    start_date         timestamp,
    end_date           timestamp,
    created_date       timestamp,
    created_by         bigint,
    last_modified_date timestamp,
    last_modified_by   bigint,
    gather_schedule_id bigint       not null default 1 comment 'Foreign key to gather_schedule',
    closed             tinyint      not null default 0 comment 'Whether this collection is closed',

    constraint col_collection_fk foreign key (col_parent_id) references col (col_id),
    constraint col_thumbnail_id_fk foreign key (thumbnail_id) references thumbnail (id),
    constraint col_gather_schedule_fk foreign key (gather_schedule_id) references gather_schedule (gather_schedule_id)
) comment ='A named set (collection) to which online resources can be assigned. These can be associated with a particular subject and are more specific than a subject. Eg. 2006 Referendum for new toothbrushes.';

create table col_subs
(
    col_id     bigint not null comment 'Foreign key to the collection related to a particular subject',
    subject_id bigint not null comment 'Foreign key to the subject related to a particular collection',
    primary key (col_id, subject_id),

    constraint col_subs_collections_fk foreign key (col_id) references col (col_id),
    constraint col_subs_subjects_fk foreign key (subject_id) references subject (subject_id)
) comment ='The relationship between a collection and related subjects.';


create table option_group
(
    access_level    bigint comment 'Defines the level at which this group of options can be changed (0 = system level, 1 = title or profile level)',
    option_group_id bigint not null auto_increment primary key comment 'Sequence generated ID for a logical group of command line gather options',
    display_order   bigint comment 'Order in which this option group is displayed',
    group_name      varchar(256) comment 'Label for a logical grouping on command line gather options'
) comment ='A named set of logically related command line options for gathering';

create table command_line_opt
(
    access_level           bigint comment 'Defines the level at which this option can be changed. 0 = system level, 1 = title or profile level',
    command_line_option_id bigint not null auto_increment primary key comment 'Sequence generated ID for this command line option',
    default_value          varchar(128) comment 'The default value for this option',
    display_name           varchar(256) comment 'The name to be displayed on the GUI for this option',
    explanation            varchar(2000) comment 'A descriptive string to explain in detail what this option does and what format it should be entered in.',
    hide_argument          tinyint comment 'Indicates whether or not to hide the argument from the gatherer application',
    hide_option            tinyint comment 'Indicates whether or not to hide the option from the gatherer application',
    is_active              tinyint comment 'Indicates whether or not the option can be set within the GUI (either at a system or title level)',
    is_argument_quoted     tinyint comment 'Whether or not this option''s argument should be quoted by the system when being passed to the gathering application',
    is_mandatory           tinyint comment 'Whether this option *must* be provided to the gathering application',
    long_option            varchar(64) comment 'The longer, more descriptive command line version of this option. eg. --help as opposed to -h',
    option_description     varchar(256) comment 'A short description of this option',
    option_group_id        bigint comment 'Foreign key to the logical group this option belongs to',
    option_prefix          varchar(16) comment 'A string to be added to the beginning of the option, when passed to the gathering application',
    option_separator       varchar(16) comment 'String to be used after this option, to separate it from other options when constructing the command line string.',
    short_option           varchar(16) comment 'The (short) command line string for this option. eg. -h',
    ui_element             varchar(64) comment 'The type of user interface component that should be used to edit this option',
    constraint command_line_opt_group_fk
        foreign key (option_group_id) references option_group (option_group_id)
) comment ='Command line options available to be passed to the application being used to gather titles. eg. -P';

create table option_argument
(
    argument               varchar(4000) comment 'The possible argument value for a gather option, or the argument value used or to be used for a particular gather',
    argument_description   varchar(512) comment 'A description of a possible argument value for a gather option',
    command_line_option_id bigint comment 'Foreign key to the command line option this argument can be or was used for',
    option_argument_id     bigint not null auto_increment primary key comment 'Sequence generated id for a command line argument',
    constraint option_argument_option_fk foreign key (command_line_option_id) references command_line_opt (command_line_option_id)
) comment ='Possible values for particular command line arguments which can be passed to the application doing the gathering, as well as the values actually set for a particular profile/title or values used for a particular instance.';

create table opt_arg_profile
(
    option_argument_id bigint not null comment 'Foreign key to the argument used for a particular gather profile',
    profile_id         bigint not null comment 'Foreign key to the profile associated with a particular argument',
    primary key (option_argument_id, profile_id),
    constraint opt_arg_profile_optionarg_fk foreign key (option_argument_id) references option_argument (option_argument_id),
    constraint opt_arg_profile_profile_fk foreign key (profile_id) references profile (profile_id)
) comment ='The relationship between a profile of gather command line options and the profile they belong to.';

create table t_gather_arg
(
    option_argument_id bigint not null comment 'An option argument related to this title gather',
    title_gather_id    bigint not null comment 'A title gather related to this option argument',
    primary key (option_argument_id, title_gather_id),
    constraint t_gather_arg_optionargument_fk foreign key (option_argument_id) references option_argument (option_argument_id),
    constraint t_gather_arg_titlegather_fk foreign key (title_gather_id) references title_gather (title_id)
) comment ='The gather options used for gathering a particular title';


create table contact_method
(
    contact_method    varchar(64) not null,
    contact_method_id bigint      not null auto_increment primary key
);

create table contact_type
(
    contact_type    varchar(32)   not null,
    contact_type_id bigint        not null auto_increment primary key,
    email_subject   varchar(1024) not null
);

create table contact
(
    contact_date      timestamp comment 'The date and time on which a communication occurred',
    contact_id        bigint not null auto_increment primary key comment 'A sequence generated id for a contact event (communication)',
    contact_method_id bigint comment 'Foreign key to a lookup of methods of communication',
    contact_type_id   bigint comment 'Foreign key to a lookup of types of communications',
    indexer_id        bigint comment 'An optional foreign key to the indexing agency which this communication was with',
    individual_id     bigint not null comment 'The contact person this communication was with. This person should be associated with a publisher or an indexing agency.',
    note              varchar(4000) comment 'A note on what this communication was about',
    publisher_id      bigint comment 'An optional foreign key to the publisher which this communication was with',
    title_id          bigint comment 'A foreign key to the title this communication was in reference to',
    user_id           bigint comment 'Foreign key to the system user who initiated the contact',

    constraint contact_method_fk foreign key (contact_method_id) references contact_method (contact_method_id),
    constraint contact_type_fk foreign key (contact_type_id) references contact_type (contact_type_id),
    constraint contact_indexer_fk foreign key (indexer_id) references indexer (indexer_id),
    constraint contact_contactperson_fk foreign key (individual_id) references individual (individual_id),
    constraint contact_publisher_fk foreign key (publisher_id) references publisher (publisher_id),
    constraint contact_title_fk foreign key (title_id) references title (title_id),
    constraint contact_user_fk foreign key (user_id) references individual (individual_id)
) comment ='A communication (contact event) between a pandas user and a contact person for a publishing agency or title';

create table date_restr
(
    agency_area_id            bigint comment 'Foreign key to the location which this restriction limits viewing of the title to',
    condition_date            timestamp comment 'The date the current condition (status) for this restriction was applied',
    condition_id              bigint comment 'Foreign key to the condition (status) of this title display restriction',
    restriction_end_date      timestamp comment 'The date on which this display restriction expires',
    restiction_start_date     timestamp comment 'The date on which this display restriction first becomes effective (if enabled)',
    title_date_restriction_id bigint not null auto_increment primary key comment 'Sequence generated ID for this restriction',
    title_id                  bigint comment 'Foreign key to the title this restriction applies to',

    constraint date_restr_agencyarea_fk foreign key (agency_area_id) references agency_area (agency_area_id),
    constraint date_restr_condition_fk foreign key (condition_id) references `condition` (condition_id)
) comment ='A restriction on where and when a title''s TEP can be displayed.';

create table discovery_source
(
    discovery_source_id    bigint not null auto_increment primary key,
    created_date           timestamp,
    item_description_query varchar(255),
    item_link_query        varchar(255),
    item_name_query        varchar(255),
    item_query             varchar(255),
    last_modified_date     timestamp,
    link_query             varchar(255),
    name                   varchar(255),
    url                    varchar(255),
    created_by             bigint comment 'FK to individual who created this source',
    last_modified_by       bigint comment 'FK to individual who last modified this source',
    constraint fk_discovery_source_created_by foreign key (created_by) references individual (individual_id),
    constraint fk_discovery_source_modified foreign key (last_modified_by) references individual (individual_id)
) comment ='Lookup of external discovery sources and their query templates';

create table discovery
(
    discovery_id        bigint not null auto_increment primary key,
    created_date        timestamp,
    description         varchar(1024),
    last_modified_date  timestamp,
    locality            varchar(255),
    name                varchar(1024),
    postcode            varchar(255),
    source_url          varchar(255),
    state               varchar(255),
    url                 varchar(1024),
    created_by          bigint comment 'FK to individual who created this discovery',
    last_modified_by    bigint comment 'FK to individual who last modified this discovery',
    discovery_source_id bigint comment 'FK to discovery_source',
    title_id            bigint comment 'FK to title this discovery refers to',
    constraint fk_discovery_created_by foreign key (created_by) references individual (individual_id),
    constraint fk_discovery_last_modified_by foreign key (last_modified_by) references individual (individual_id),
    constraint fk_discovery_source foreign key (discovery_source_id) references discovery_source (discovery_source_id),
    constraint fk_discovery_title foreign key (title_id) references title (title_id)
) comment ='Individual discovery events linked to sources and titles';

create table email_template
(
    email_template_id bigint       not null auto_increment primary key,
    agency_id         bigint       not null comment 'FK to agency using this template',
    name              varchar(256) not null,
    template          text,
    contact_type_id   bigint comment 'FK to contact_type to create when using this template',
    constraint email_template_agency_fk foreign key (agency_id) references agency (agency_id),
    constraint email_template_contact_type_fk foreign key (contact_type_id) references contact_type (contact_type_id)
) comment ='Predefined email templates for agencies and contact types';

create table gather_date
(
    gather_date     timestamp comment 'A date on which a non-recurring gather should take place for a title',
    gather_date_id  bigint not null auto_increment primary key comment 'Sequence generated ID for a non-recurring gather date',
    title_gather_id bigint not null comment 'Foreign key to the gather details for a particular title',
    constraint gather_date_titlegather_fk
        foreign key (title_gather_id) references title_gather (title_id)
) comment ='A one-off date on which a title is to be gathered (non-recurring)';

create table gather_filter_preset
(
    filter_name             varchar(256),
    filter_preset           varchar(256),
    gather_filter_preset_id bigint not null auto_increment primary key
);

create table instance_opt_arg
(
    instance_id    bigint not null comment 'The instance a particular argument was used to gather',
    ins_opt_arg_id bigint not null comment 'The argument used to gather a particular instance',
    primary key (instance_id, ins_opt_arg_id),
    constraint instance_opt_arg_instance_fk foreign key (instance_id) references instance (instance_id),
    constraint instance_opt_arg_optarg_fk foreign key (ins_opt_arg_id) references option_argument (option_argument_id)
) comment ='The relationship between an instance and the command line options specified when it was gathered.';

create table instance_seed
(
    id          bigint        not null auto_increment primary key,
    instance_id bigint        not null comment 'The instance for this seed URL',
    url         varchar(1024) not null,
    status      bigint,
    redirect    varchar(1024),
    constraint instance_seed_instance_id_fk foreign key (instance_id) references instance (instance_id) on delete cascade
) comment ='Seed URLs recorded for each instance, with status and redirect info.';

create table instance_thumbnail
(
    instance_id        bigint       not null,
    data               blob         not null,
    created_date       timestamp not null,
    last_modified_date timestamp not null,
    content_type       varchar(255) not null,
    status             bigint       not null,
    type               smallint     not null default 0,

    constraint instance_thumbnail_pk primary key (instance_id, type),
    constraint instance_thumbnail_instance_fk foreign key (instance_id) references instance (instance_id) on delete cascade
);

create table linked_account
(
    linked_account_id  bigint       not null auto_increment primary key,
    individual_id      bigint       not null,
    provider           varchar(255) not null,
    external_id        varchar(255) not null,
    created_date       timestamp not null,
    last_modified_date timestamp,
    last_login_date    timestamp,

    constraint linked_account_individual_fk
        foreign key (individual_id) references individual (individual_id)
);

create index linked_account_extid_index
    on linked_account (provider, external_id);

create table owner_history
(
    agency_id      bigint comment 'Foreign key to the partner agency the listed user belongs to',
    individual_id  bigint       not null comment 'Foreign key to the pandas user who owns or previously owned a particular title',
    note           varchar(4000) comment 'A note written by a user who was transferring a title to another user',
    owner_id       bigint       not null auto_increment primary key,
    ownership_date timestamp not null comment 'The date on which ownership of the title began for this user',
    title_id       bigint       not null comment 'Foreign key to the title this ownership record refers to',
    transferrer_id bigint comment 'Foreign key to the individual who transferred ownership of this title',

    constraint owner_history_agency_fk foreign key (agency_id) references agency (agency_id),
    constraint owner_history_individual_fk foreign key (individual_id) references individual (individual_id),
    constraint owner_history_title_fk foreign key (title_id) references title (title_id),
    constraint owner_history_transferrer_fk foreign key (transferrer_id) references individual (individual_id)
) comment ='A log of any change in the system user responsible for the title.';

create table pandas_exception_log
(
    exception_date       timestamp,
    instance_id          bigint,
    exception_originator varchar(100),
    exception_log_id     bigint not null auto_increment primary key,
    pi                   bigint,
    exception_summary    varchar(4000),
    title_id             bigint,
    exception_viewed     bigint,
    exception_detail     text,

    constraint pandas_exception_log_inst_fk foreign key (instance_id) references instance (instance_id),
    constraint pandas_exception_log_title_fk foreign key (title_id) references title (title_id)
);

create table period_type
(
    period_type             varchar(20) comment 'Time periods available for period restrictions',
    period_type_description varchar(1024) comment 'A description of what a period type means or when it should be used',
    period_type_id          bigint not null auto_increment primary key comment 'Sequence generated id for a period type'
) comment ='A lookup table for the intervals of time available when defining a period display restriction. eg. day/s, week/s, ...';

create table period_restr
(
    agency_area_id        bigint comment 'Foreign key to the location which access to a title is limited to',
    condition_date        timestamp comment 'The date the current condition (status) for this restriction was applied',
    condition_id          bigint comment 'Foreign key to the status fo this restriction',
    period_multiplier     bigint comment 'How many times the period type must pass after the start date, before this restriction expires',
    period_restriction_id bigint not null auto_increment primary key comment 'Sequence generated id for this period restriction',
    period_type_id        bigint comment 'Foreign key to the time period for this restriction',
    title_id              bigint comment 'Foreign key to the title for this retriction',

    constraint period_restr_agencyarea_fk foreign key (agency_area_id) references agency_area (agency_area_id),
    constraint period_restr_condition_fk foreign key (condition_id) references `condition` (condition_id),
    constraint period_restr_periodtype_fk foreign key (period_type_id) references period_type (period_type_id)
) comment ='A title display restriction which prevents a title instance from being publically available for a certain period of time after it is gathered.';


create table qa_problem
(
    affects_whole_site     bigint comment 'Whether or not this problem affects the whole site or just one or two pages',
    creation_date          timestamp comment 'The date and time at which the QA problem was first created',
    example_link           varchar(4000) comment 'Links to any examples of the problem',
    external_id            varchar(16) comment 'The ID of the problem in the external QA system',
    individual_id          bigint comment 'The user who reported the problem',
    instance_id            bigint comment 'The instance this problem was found in',
    is_recurring           bigint comment 'Whether this problem is always/often found in instances of this title',
    modification_date      timestamp comment 'The date and time on which this QA problem was last updated',
    problem_description    varchar(4000) comment 'A description of this QA problem',
    problem_id             bigint not null auto_increment primary key comment 'A sequence generated ID for this QA problem',
    problem_name           varchar(1024) comment 'A short label for this QA problem',
    requires_external_save bigint comment 'Indicates whether a synchronization is needed between the database values and the version of this problem stored in the external QA system',
    state                  varchar(100) comment 'The current state of this QA problem. eg. closed, open',

    constraint qa_problem_creator_fk foreign key (individual_id) references individual (individual_id),
    constraint qa_problem_instance_fk foreign key (instance_id) references instance (instance_id)
) comment ='A QA problem associated with a title instance which has been identified by a pandas user.';

create table reason
(
    reason    varchar(512) comment 'Possible reasons for a title status change',
    reason_id bigint not null auto_increment primary key comment 'Sequence generated ID for status reasons',
    status_id bigint comment 'Foreign key to the status this reason may be applied to'
) comment ='Lookup table for reasons that explain why a title has been set to a particular status.';

create table rejected_domain
(
    id           bigint       not null auto_increment primary key,
    domain       varchar(255) not null,
    reason_id    bigint       not null,
    created_date timestamp not null,
    user_id      bigint       not null,
    agency_id    bigint,

    constraint rejected_domain_reason_fk foreign key (reason_id) references reason (reason_id),
    constraint rejected_domain_user_fk foreign key (user_id) references individual (individual_id),
    constraint rejected_domain_agency_fk foreign key (agency_id) references agency (agency_id)
);

create unique index rejected_domain_ag_dom_idx
    on rejected_domain (agency_id, domain);

create table report_type
(
    report_type_id       bigint       not null auto_increment primary key comment 'Primary key for report_type',
    name                 varchar(512) not null comment 'A friendly name for this report type.',
    java_class           varchar(256) not null comment 'The java class that implements this report type. eg ''au.gov.nla.webarchive.reports.ArchiveStatisticsReport''',
    has_details          bigint       not null default 0 comment '1 if this report has both "show details" and "numbers only" modes.',
    has_period           bigint       not null default 0 comment '1 if this report requires a period to be specified, else 0.',
    has_agency           bigint       not null default 0 comment '1 if this report requires an agency (or all agencies) to be specified, else 0.',
    has_publisher_type   bigint       not null default 0 comment '1 if this report requires a publisher type, else 0.',
    has_restriction_type bigint       not null default 0 comment '1 if this report requires a restriction type, else 0.'
) comment ='Lookup of available report types.';

create table report_schedule
(
    report_schedule_id bigint       not null auto_increment primary key comment 'Primary key for report_schedule.',
    name               varchar(256) not null comment 'Name of this schedule (''Weekly'', ''Monthly'' etc.)'
) comment ='Lookup of report delivery schedules.';

create table report
(
    report_id            bigint not null auto_increment primary key comment 'Unique identifier for this report (either a scheduled or a particular ad-hoc report).',
    individual_id        bigint not null comment 'Id of the individual who requested this report.',
    report_type_id       bigint not null comment 'Key of the type of report that was selected, see report_type table.',
    agency_id            bigint comment 'Key of the agency that this report covers.',
    period_start         date comment 'Start date for the period this report covers. Should be null for scheduled reports.',
    period_end           date comment 'End date for the period this report covers. Should be null for scheduled reports.',
    show_details         bigint not null comment 'Detail level to be included in the report. 0 = "Numbers only" summary. 1 = full details.',
    publisher_type_id    bigint comment 'Publisher type this report should cover. Null if not applicable for this report type.',
    restriction_type     bigint comment 'Restriction type this report should cover (if applicable). null=N/A, 0=period, 1=date, 2=auth',
    last_generation_date date comment 'Date this report was last generated.',
    next_generation_date date comment 'Date this report should next be generated. Null if the report is not scheduled (ie ad-hoc report).',
    report_schedule_id   bigint comment 'Key of the schedule this report is on. Null if the report is not scheduled.',
    scheduled_day        bigint comment 'Day of the week or month (depending on schedule_id) that this report should be delivered upon.',
    is_visible           bigint not null comment 'Should this report appear in the user''s report tray? 0=hidden, 1=visible',
    error_msg            varchar(512) comment 'Error message if the report generation failed',

    constraint report_individual_fk foreign key (individual_id) references individual (individual_id),
    constraint report_report_type_fk foreign key (report_type_id) references report_type (report_type_id),
    constraint report_agency_fk foreign key (agency_id) references agency (agency_id),
    constraint report_publisher_type_fk foreign key (publisher_type_id) references publisher_type (publisher_type_id),
    constraint report_schedule_fk foreign key (report_schedule_id) references report_schedule (report_schedule_id)
) comment ='Scheduled and ad-hoc reports requested by users.';

create table role
(
    audit_create_date timestamp comment 'The date and time at which this role was created',
    audit_date        timestamp comment 'The date and time at which this role was last modified',
    audit_userid      bigint comment 'The user who last modified this role',
    comments          varchar(200) comment 'Any notes about this role',
    individual_id     bigint       not null comment 'Foreign key to the individual who performs this role',
    organisation_id   bigint       not null comment 'Foreign key to the organisation the user performs this role in',
    role_id           bigint       not null auto_increment primary key comment 'Sequence generated ID for this role',
    role_title        varchar(100) not null comment 'The name of this role',
    role_type         varchar(20)  not null,

    constraint role_individual_fk foreign key (individual_id) references individual (individual_id),
    constraint role_organisation_fk foreign key (organisation_id) references organisation (organisation_id)
) comment ='The role a particular individual plays in an organisation.';

create table social_target
(
    social_target_id       bigint       not null auto_increment primary key,
    query                  varchar(255) not null,
    server                 varchar(255) not null,
    created_date           timestamp,
    last_modified_date     timestamp,
    last_visited_date      timestamp,
    newest_post_date       timestamp,
    newest_post_id         varchar(255),
    oldest_post_date       timestamp,
    oldest_post_id         varchar(255),
    post_count             bigint       not null default 0,
    created_by             bigint,
    last_modified_by       bigint,
    title_id               bigint,
    current_range_position varchar(255),
    current_range_end      varchar(255),

    constraint fk_social_target_created_by foreign key (created_by) references individual (individual_id),
    constraint fk_social_target_modified_by foreign key (last_modified_by) references individual (individual_id),
    constraint fk_social_target_title foreign key (title_id) references title (title_id)
);

create table state_history
(
    end_date         timestamp comment 'The date and time this state stopped being the current one for an instance',
    individual_id    bigint comment 'Foreign key to the individual who caused this state to be applied',
    instance_id      bigint comment 'Foreign key to the instance this state applies or once applied to',
    start_date       timestamp comment 'The date and time this state stopped being the current one for an instance',
    state_history_id bigint not null auto_increment primary key comment 'Sequence generated id for this status history entry',
    state_id         bigint comment 'Foreign key to the state for this status history entry',

    constraint state_history_individual_fk foreign key (individual_id) references individual (individual_id),
    constraint state_history_instance_fk foreign key (instance_id) references instance (instance_id),
    constraint state_history_state_fk foreign key (state_id) references state (state_id)
) comment ='The state a particular instance is in, or was previously in.';

create index state_history_state_date on state_history (state_id, start_date);

create table status_history
(
    end_date          timestamp comment 'The date and time this status stopped being the current one for a title',
    individual_id     bigint comment 'Foreign key to the individual who caused this status to be applied',
    reason_id         bigint comment 'Foreign key to a reason why this status was applied',
    start_date        timestamp comment 'The date and time this status stopped being the current one for a title',
    status_history_id bigint not null auto_increment primary key comment 'Sequence generated ID for this status history entry',
    status_id         bigint comment 'Foreign key to the status for this entry',
    title_id          bigint comment 'Foreign key to the title this status applies or applied to',

    constraint status_history_individual_fk foreign key (individual_id) references individual (individual_id),
    constraint status_history_reason_fk foreign key (reason_id) references reason (reason_id),
    constraint status_history_state_fk foreign key (status_id) references status (status_id),
    constraint status_history_title_fk foreign key (title_id) references title (title_id)
) comment ='A log of any change in the status of a title, as well as the title''s current status value.';

create table title_col
(
    collection_id bigint not null,
    title_id      bigint not null,
    primary key (collection_id, title_id),
    constraint title_col_collection_fk foreign key (collection_id) references col (col_id),
    constraint title_col_title_fk foreign key (title_id) references title (title_id)
) comment ='The relationship between a title and a collection';

create table title_flag
(
    individual_id bigint not null,
    title_id      bigint not null,
    primary key (individual_id, title_id),
    constraint title_flag_individual_fk foreign key (individual_id) references individual (individual_id) on delete cascade,
    constraint title_flag_title_fk foreign key (title_id) references title (title_id) on delete cascade
);

create table title_embedding
(
    title_id  bigint       not null auto_increment primary key,
    embedding blob         not null,
    created   timestamp not null,
    constraint title_embedding_title_title_id_fk foreign key (title_id) references title (title_id) on delete cascade
);

create table title_individual
(
    individual_id bigint not null comment 'Foreign key to a contact person for this title',
    title_id      bigint not null comment 'Foreign key to a title related to this contact person',
    primary key (individual_id, title_id),
    constraint title_individual_individual_fk foreign key (individual_id) references individual (individual_id),
    constraint title_individual_title_fk foreign key (title_id) references title (title_id)
) comment ='A many to many relationship between a title and its associated contact people.';

create table title_history
(
    ceased_id        bigint comment 'Foreign key to the title which has been replaced by a new (continuing) title',
    continues_id     bigint comment 'Foreign key to the title which took over from a ceased title',
    date_changed     timestamp comment 'The date the old title was replaced by the new title',
    title_history_id bigint not null auto_increment primary key comment 'Sequence generated ID for a record of historicly used titles',
    constraint title_history_continuedby_fk foreign key (ceased_id) references title (title_id),
    constraint title_history_continues_fk foreign key (continues_id) references title (title_id)
) comment ='A relationship between two "serial" format titles where one has replaced the other.';

create table title_par_child
(
    child_id           bigint comment 'Foreign key to a child title of this parent title',
    parent_id          bigint comment 'Foreign key to the parent title for this child title',
    related_date       timestamp comment 'The date the parent child relationship was created',
    title_par_child_id bigint not null auto_increment primary key comment 'Sequence generated ID for the child parent title relationship',
    constraint title_par_child_child_fk foreign key (child_id) references title (title_id),
    constraint title_par_child_parent_fk foreign key (parent_id) references title (title_id)
) comment ='The relationship between two titles where one is the parent of the other.';

create table title_previous_name
(
    date_changed     timestamp comment 'The date the previous name stopped being current',
    previous_name    varchar(256) comment 'A name which used to be used for a title, but has now been changed',
    title_history_id bigint not null auto_increment primary key comment 'Sequence generated ID for the previous title name',
    title_id         bigint comment 'Foreign key to the title this previous name is for'
) comment ='The name an "integrating" format title was previously known as';

create table mime_type
(
    date_added   timestamp comment 'The date this mime type was added',
    mime_type_id bigint not null auto_increment primary key comment 'Sequence generated id for this mime type',
    note         varchar(256) comment 'Notes about this mime type',
    parameter    varchar(256) comment 'Extra parameters specified with a mime type. Eg. charset',
    subtype      varchar(64) comment 'Mime subtype code (eg xml), listed after the mime type, and a "/", eg. text/xml',
    type         varchar(64) comment 'Mime type code, eg. text'
) comment ='The code for a particular type of file format';

create table instance_mime
(
    instance_id  bigint not null comment 'The instance associated with a particular mime type',
    mime_type_id bigint not null comment 'The mime type associated with a particular instance',
    primary key (instance_id, mime_type_id),
    constraint instance_mime_instance_fk foreign key (instance_id) references instance (instance_id),
    constraint instance_mime_mimetype_fk foreign key (mime_type_id) references mime_type (mime_type_id)
) comment ='The relationship between a title instance and a file mime type.';

create table application
(
    application_id bigint not null auto_increment primary key comment 'Sequency generated ID for an application',
    name           varchar(256) comment 'The name or title of a computer application'
) comment ='A piece of software associated with a particular type of file.';

create table app_mime_type
(
    application_id bigint not null comment 'Foreign key to an application associated with this mime type',
    mime_type_id   bigint not null comment 'Sequence generated ID for this mime type',
    primary key (application_id, mime_type_id),
    constraint app_mime_type_application_fk foreign key (application_id) references application (application_id),
    constraint app_mime_type_mimetype_fk foreign key (mime_type_id) references mime_type (mime_type_id)
) comment ='A particular type of file.';

create table mime_extension
(
    mime_extension    varchar(32) comment 'A file extension associated with a mime type',
    mime_extension_id bigint not null auto_increment primary key comment 'Sequence generated id for a mime extension',
    mime_type_id      bigint comment 'Foreign key to the mime type for this extension',
    constraint fk_mime_extension_mimetype foreign key (mime_type_id) references mime_type (mime_type_id)
) comment ='The file extension for a particular mime type.';

create table type_stats
(
    content_type varchar(255) not null,
    status       bigint       not null,
    year         bigint       not null,
    snapshots    bigint       not null,
    storage      bigint       not null,
    primary key (content_type, status, year)
);

create table spring_session
(
    primary_id            char(36) not null,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval int      not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100),
    constraint spring_session_pk primary key (primary_id)
);

create unique index spring_session_ix1 on spring_session (session_id);
create index spring_session_ix2 on spring_session (expiry_time);
create index spring_session_ix3 on spring_session (principal_name);

create table spring_session_attributes
(
    session_primary_id char(36)     not null,
    attribute_name     varchar(200) not null,
    attribute_bytes    blob         not null,
    constraint spring_session_attributes_pk primary key (session_primary_id, attribute_name),
    constraint spring_session_attributes_fk foreign key (session_primary_id) references spring_session (primary_id) on delete cascade
);

--
-- Constraints
--

alter table instance
    add constraint instance_currentstate_fk foreign key (current_state_id) references state (state_id),
    add constraint instance_oldinstancestate_fk foreign key (instance_state_id) references old_instance_state (instance_state_id),
    add constraint instance_oldinstancestatus_fk foreign key (instance_status_id) references old_instance_status (instance_status_id),
    add constraint instance_title_fk foreign key (title_id) references title (title_id),
    add constraint instance_profile_fk foreign key (profile_id) references profile (profile_id) on delete set null,
    add constraint instance_scope_fk foreign key (scope_id) references scope (scope_id) on delete set null;

alter table title_gather
    add constraint title_gather_profile_fk foreign key (active_profile_id) references profile (profile_id),
    add constraint title_gather_gathermethod_fk foreign key (gather_method_id) references gather_method (gather_method_id),
    add constraint title_gather_gatherschedule_fk foreign key (gather_schedule_id) references gather_schedule (gather_schedule_id),
    add constraint title_gather_title_fk foreign key (title_id) references title (title_id),
    add constraint scope_fk foreign key (scope_id) references scope (scope_id);

alter table permission
    add constraint permission_grantedby_fk foreign key (individual_id) references individual (individual_id),
    add constraint permission_state_fk foreign key (permission_state_id) references permission_state (permission_state_id),
    add constraint permission_type_fk foreign key (permission_type_id) references permission_type (permission_type_id),
    add constraint permission_publisher_fk foreign key (publisher_id) references publisher (publisher_id),
    add constraint permission_title_fk foreign key (title_id) references title (title_id);
