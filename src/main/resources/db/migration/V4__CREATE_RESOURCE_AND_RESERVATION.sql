CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE resource
(
    id           UUID          NOT NULL,
    name         VARCHAR(255)  NOT NULL,
    category     VARCHAR(255)  NOT NULL,
    location     VARCHAR(255)  NOT NULL,
    capacity     INTEGER       NOT NULL,
    amenities    VARCHAR(255),
    description  VARCHAR(2000),
    image_url    VARCHAR(500),
    opening_time TIME          NOT NULL,
    closing_time TIME          NOT NULL,
    active       BOOLEAN       NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_resource PRIMARY KEY (id)
);

CREATE TABLE reservation
(
    id          UUID        NOT NULL,
    resource_id UUID        NOT NULL,
    account_id  UUID        NOT NULL,
    start_time  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_reservation PRIMARY KEY (id)
);

ALTER TABLE reservation
    ADD CONSTRAINT fk_reservation_on_resource FOREIGN KEY (resource_id) REFERENCES resource (id);

ALTER TABLE reservation
    ADD CONSTRAINT fk_reservation_on_account FOREIGN KEY (account_id) REFERENCES account (id);

-- Database-level guarantee: no two CONFIRMED reservations for the same resource
-- may have overlapping time ranges, even under concurrent requests.
ALTER TABLE reservation
    ADD CONSTRAINT reservation_no_overlap EXCLUDE USING gist (
        resource_id WITH =,
        tsrange(start_time, end_time) WITH &&
    ) WHERE (status = 'CONFIRMED');