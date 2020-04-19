create table campaign
(
    id        int auto_increment
        primary key,
    managerId int                           null,
    name      varchar(255)                  not null,
    client    varchar(255)                  not null,
    state     varchar(50) default 'created' not null,
    constraint campaign_name_uindex
        unique (name),
    constraint campaign_manager_fk
        foreign key (managerId) references user (id)
            on update cascade on delete set null
);

