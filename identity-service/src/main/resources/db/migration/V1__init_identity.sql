-- Identity Service initial schema.
-- Owns credentials, roles, and (for MVP simplicity) basic profile + addresses.

CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(150),
    last_name     VARCHAR(150),
    phone         VARCHAR(30),
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE role (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES role (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE address (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    line1       VARCHAR(255) NOT NULL,
    line2       VARCHAR(255),
    city        VARCHAR(150) NOT NULL,
    state       VARCHAR(150),
    postal_code VARCHAR(30),
    country     VARCHAR(100) NOT NULL,
    is_default  BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_address_user ON address (user_id);

-- Seed baseline roles.
INSERT INTO role (name) VALUES ('ROLE_CUSTOMER'), ('ROLE_ADMIN');
