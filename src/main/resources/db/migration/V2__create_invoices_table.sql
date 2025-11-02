CREATE TABLE invoices (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invoice_number  VARCHAR(50) UNIQUE NOT NULL,
    customer_name   VARCHAR(200) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    pending_amount  DECIMAL(10,2) NOT NULL CHECK (pending_amount >= 0),
    status          VARCHAR(20) NOT NULL CHECK (status IN ('UNPAID', 'PARTIALLY_PAID', 'FULLY_PAID')),
    invoice_date    DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoices_user_status ON invoices(user_id, status);
CREATE INDEX idx_invoices_customer_name ON invoices(customer_name);
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);
