CREATE TABLE reconciliation_suggestions (
    id              BIGSERIAL PRIMARY KEY,
    payment_id      BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    invoice_id      BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    confidence      DECIMAL(3,2) NOT NULL CHECK (confidence >= 0.00 AND confidence <= 1.00),
    reasoning       TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    ai_model        VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at    TIMESTAMP,
    confirmed_by    BIGINT REFERENCES users(id)
);

CREATE INDEX idx_suggestions_payment_status ON reconciliation_suggestions(payment_id, status);
CREATE INDEX idx_suggestions_invoice ON reconciliation_suggestions(invoice_id);
CREATE INDEX idx_suggestions_confidence ON reconciliation_suggestions(confidence DESC);
