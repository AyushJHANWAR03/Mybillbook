CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    mobile_number   VARCHAR(10) UNIQUE NOT NULL,
    name            VARCHAR(100),
    business_name   VARCHAR(200),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_mobile ON users(mobile_number);
