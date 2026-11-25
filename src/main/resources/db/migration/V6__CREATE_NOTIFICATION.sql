CREATE TABLE notification
(
    id             UUID          NOT NULL,
    account_id     UUID          NOT NULL,
    reservation_id UUID,
    message        VARCHAR(1000) NOT NULL,
    read_status    BOOLEAN       NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (id)
);

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_on_account FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_on_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id);