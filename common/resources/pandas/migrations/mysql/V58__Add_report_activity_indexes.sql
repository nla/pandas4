create index state_hist_state_date_inst
    on state_history (state_id, start_date, instance_id);

create index instance_title_date
    on instance (title_id, instance_date);
