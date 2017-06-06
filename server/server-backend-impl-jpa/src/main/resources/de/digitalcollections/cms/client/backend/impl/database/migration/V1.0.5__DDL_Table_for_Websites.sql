create table websites (
  id int8 not null,
  title varchar(255) not null,
  url varchar(255) not null,
  primary key (id)
);

alter table websites 
  add constraint UK_k20s3hdbl4p9h6h3amombfaqd  unique (url);