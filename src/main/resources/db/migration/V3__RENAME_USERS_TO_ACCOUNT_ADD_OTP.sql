
CREATE TABLE account
(
    id             UUID         NOT NULL,
    user_name      VARCHAR(255),
    full_name      VARCHAR(255),
    password       VARCHAR(255) NOT NULL,
    role           VARCHAR(255) NOT NULL,
    auth_provider  VARCHAR(255),
    email_verified BOOLEAN      NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_account PRIMARY KEY (id)
);

CREATE TABLE email_otps
(
    id         UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    purpose    VARCHAR(30) NOT NULL,
    code_hash  VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    attempts   INTEGER     NOT NULL,
    CONSTRAINT pk_email_otps PRIMARY KEY (id)
);
ALTER TABLE account
    ADD CONSTRAINT uc_account_username UNIQUE (user_name);

ALTER TABLE email_otps
    ADD CONSTRAINT FK_EMAIL_OTPS_ON_USER FOREIGN KEY (user_id) REFERENCES account (id);

DROP TABLE users CASCADE;