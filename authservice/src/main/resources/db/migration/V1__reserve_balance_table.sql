
ALTER TABLE users
ADD COLUMN reserved_balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE users
ADD CONSTRAINT chk_reserved_balance_non_negative 
CHECK (reserved_balance >= 0);


ALTER TABLE users
ADD CONSTRAINT chk_reserved_not_exceed_balance 
CHECK (reserved_balance <= balance);



CREATE TABLE balance_reservations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    idempotent_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_status_valid CHECK (
        status IN ('RESERVED', 'COMMITTED', 'CANCELLED', 'EXPIRED')
    ),
);

CREATE INDEX idx_reservations_user_id ON balance_reservations(user_id);
CREATE INDEX idx_reservations_status ON balance_reservations(status);
CREATE INDEX idx_reservations_idempotent_key ON balance_reservations(idempotent_key);
CREATE INDEX idx_reservations_status_expires ON balance_reservations(status, expires_at);
CREATE INDEX idx_reservations_user_status ON balance_reservations(user_id, status);