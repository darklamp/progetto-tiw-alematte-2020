create table annotation
(
    workerId int                not null,
    imageId  int                not null,
    date     date               not null,
    validity smallint default 0 not null,
    trust    varchar(10)        not null,
    note     text               null,
    primary key (workerId, imageId),
    constraint annotation_image_fk
        foreign key (imageId) references image (id)
            on update cascade on delete cascade,
    constraint annotation_user_fk
        foreign key (workerId) references user (id)
            on update cascade
);

