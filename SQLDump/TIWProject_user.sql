create table user
(
    id         int auto_increment
        primary key,
    username   varchar(100) not null,
    email      varchar(255) not null,
    password   varchar(400) not null,
    salt       varchar(110) null,
    role       varchar(10)  not null,
    level      varchar(10)  null,
    photo      varchar(255) null,
    authcookie varchar(255) null,
    cookietime datetime     null,
    constraint user_email_uindex
        unique (email),
    constraint user_username_uindex
        unique (username)
);

