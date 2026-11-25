ALTER TABLE resource
    ADD COLUMN owner_id UUID;

UPDATE resource
SET owner_id = (SELECT id FROM account WHERE role = 'ADMIN' ORDER BY created_at LIMIT 1)
WHERE owner_id IS NULL;

ALTER TABLE resource
    ALTER COLUMN owner_id SET NOT NULL;

ALTER TABLE resource
    ADD CONSTRAINT fk_resource_on_owner FOREIGN KEY (owner_id) REFERENCES account (id);