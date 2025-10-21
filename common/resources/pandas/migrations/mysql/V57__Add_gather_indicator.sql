create table gather_indicator
(
    id              bigint        not null auto_increment primary key,
    instance_id     bigint        not null comment 'The gather instance for this indicator',
    indicator_value numeric(4,3) not null,
    indicator       varchar(64),
    constraint gather_indicator_instance_id_fk foreign key (instance_id) references ins_gather (instance_id) on delete cascade
) comment ='Quality metrics for each gather.';
