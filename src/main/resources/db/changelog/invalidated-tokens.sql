--liquibase formatted sql


--changeset mrshoffen:2
CREATE TABLE invalidated_tokens
(
    id           uuid primary key,
    c_keep_until timestamp not null check ( c_keep_until > now() )
);