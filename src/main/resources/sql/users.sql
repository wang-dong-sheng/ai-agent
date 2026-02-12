create table users
(
    id              bigserial
        primary key,
    username        varchar(100) not null
        unique,
    password_hash   varchar(255) not null,
    created_at      timestamp default CURRENT_TIMESTAMP,
    updated_at      timestamp default CURRENT_TIMESTAMP
);

comment on table users is '用户表，存储用户信息';
comment on column users.id is '用户ID，主键';
comment on column users.username is '用户名，唯一';
comment on column users.password_hash is '密码哈希值，使用BCrypt加密';
comment on column users.created_at is '创建时间';
comment on column users.updated_at is '更新时间';

alter table users
    owner to root;

create index idx_users_username
    on users (username);

