CREATE TABLE waitlist_entry
(
    id          UUID        NOT NULL,
    resource_id UUID        NOT NULL,
    account_id  UUID        NOT NULL,
    start_time  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status      VARCHAR(20) NOT NULL,
    queued_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_waitlist_entry PRIMARY KEY (id)
);

ALTER TABLE waitlist_entry
    ADD CONSTRAINT fk_waitlist_entry_on_resource FOREIGN KEY (resource_id) REFERENCES resource (id);

ALTER TABLE waitlist_entry
    ADD CONSTRAINT fk_waitlist_entry_on_account FOREIGN KEY (account_id) REFERENCES account (id);

-- Prevents a user from queueing twice for the exact same resource and time interval.
CREATE UNIQUE INDEX uq_waitlist_entry_active
    ON waitlist_entry (resource_id, account_id, start_time, end_time)
    WHERE status = 'WAITING';