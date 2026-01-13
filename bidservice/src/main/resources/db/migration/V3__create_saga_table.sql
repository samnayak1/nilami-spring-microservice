CREATE TABLE saga_logs (
    saga_id UUID PRIMARY KEY,
    item_id UUID NOT NULL,
    user_id UUID NOT NULL,
    bid_amount NUMERIC(19, 2) NOT NULL,
    current_state VARCHAR(50) NOT NULL, 
    saga_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);