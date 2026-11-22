CREATE TABLE users
(
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    id         UUID     NOT NULL  ,
    full_name  VARCHAR(255),
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50) NOT NULL,
    user_name  VARCHAR(255) NOT NULL  ,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT users_user_name_key UNIQUE (user_name);
