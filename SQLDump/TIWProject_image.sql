create table image
(
    id         int auto_increment
        primary key,
    date       date         not null,
    latitude   float        not null,
    longitude  float        not null,
    resolution varchar(50)  not null,
    source     varchar(100) not null,
    region     varchar(100) not null,
    town       varchar(100) not null,
    url        varchar(255) not null
);

