CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    payment_date    DATE NOT NULL,
    payment_mode    VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH', 'UPI', 'CARD', 'BANK_TRANSFER')),
    remark          TEXT,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('UNRECONCILED', 'RECONCILED')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_user_status ON payments(user_id, status);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
