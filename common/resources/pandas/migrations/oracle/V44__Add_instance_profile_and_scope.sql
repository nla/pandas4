alter table INSTANCE
    add (PROFILE_ID number,
         SCOPE_ID number,
         constraint INSTANCE_PROFILE_FK
             foreign key (PROFILE_ID) references PROFILE
                 on delete set null,
         constraint INSTANCE_SCOPE_FK
             foreign key (SCOPE_ID) references SCOPE
                 on delete set null);