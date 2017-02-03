
    alter table role_operation 
        drop 
        foreign key FK_eysxbp63s5u005db3nejdhdxk;

    alter table role_operation 
        drop 
        foreign key FK_h5xyyxwspleqh894axwiyeify;

    alter table user_role 
        drop 
        foreign key FK_it77eq964jhfqtu54081ebtio;

    alter table user_role 
        drop 
        foreign key FK_apcc8lxk2xnug8377fatvbn04;

    drop table if exists operations;

    drop table if exists role_operation;

    drop table if exists roles;

    drop table if exists user_role;

    drop table if exists users;

    drop table if exists SEQUENCE_TABLE;

    create table operations (
        id int8 not null,
        name varchar(45) not null,
        primary key (id)
    );

    create table role_operation (
        role_id int8 not null,
        operation_id int8 not null
    );

    create table roles (
        id int8 not null,
        name varchar(45) not null,
        primary key (id)
    );

    create table user_role (
        user_id int8 not null,
        role_id int8 not null
    );

    create table users (
        id int8 not null,
        email varchar(255) not null,
        enabled boolean,
        firstname varchar(255),
        lastname varchar(255),
        password varchar(255),
        primary key (id)
    );

    alter table users 
        add constraint UK_6dotkott2kjsp8vw4d0m25fb7  unique (email);

    alter table role_operation 
        add constraint FK_eysxbp63s5u005db3nejdhdxk 
        foreign key (operation_id) 
        references operations (id);

    alter table role_operation 
        add constraint FK_h5xyyxwspleqh894axwiyeify 
        foreign key (role_id) 
        references roles (id);

    alter table user_role 
        add constraint FK_it77eq964jhfqtu54081ebtio 
        foreign key (role_id) 
        references roles (id);

    alter table user_role 
        add constraint FK_apcc8lxk2xnug8377fatvbn04 
        foreign key (user_id) 
        references users (id);

    create table SEQUENCE_TABLE (
         SEQ_NAME varchar(255),
         SEQ_COUNT integer 
    );
