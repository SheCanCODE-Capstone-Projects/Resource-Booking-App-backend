CREATE TABLE role_request
(
    id             UUID        NOT NULL,
    account_id     UUID        NOT NULL,
    requested_role VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    reviewed_by    UUID,
    requested_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reviewed_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_role_request PRIMARY KEY (id)
);

ALTER TABLE role_request
    ADD CONSTRAINT fk_role_request_on_account FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE role_request
    ADD CONSTRAINT fk_role_request_on_reviewer FOREIGN KEY (reviewed_by) REFERENCES account (id);

-- Only one pending request per account at a time.
CREATE UNIQUE INDEX uq_role_request_pending
    ON role_request (account_id)
    WHERE status = 'PENDING';