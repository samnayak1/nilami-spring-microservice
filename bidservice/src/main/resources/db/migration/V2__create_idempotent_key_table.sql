CREATE TABLE idempotent_keys (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 bid_amount NUMERIC(19,2) NOT NULL,
 creator_id UUID NOT NULL,
 item_id UUID NOT NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 bid_status VARCHAR(20) NOT NULL
);