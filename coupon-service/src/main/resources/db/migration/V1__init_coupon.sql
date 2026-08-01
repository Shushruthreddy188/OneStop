-- Coupon Service schema + a few seeded demo coupons.

CREATE TABLE coupon (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(50)  NOT NULL UNIQUE,
    description       VARCHAR(200),
    discount_type     VARCHAR(20)  NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED')),
    discount_value    NUMERIC(12, 2) NOT NULL,
    min_order_amount  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    max_discount      NUMERIC(12, 2),
    active            BOOLEAN      NOT NULL DEFAULT true,
    expires_at        TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO coupon (code, description, discount_type, discount_value, min_order_amount, max_discount, expires_at) VALUES
    ('WELCOME10', '10% off your order (min ₹200, up to ₹100)', 'PERCENT', 10, 200, 100, now() + interval '1 year'),
    ('FLAT50',    'Flat ₹50 off (min ₹300)',                   'FIXED',   50, 300, NULL, now() + interval '1 year'),
    ('SAVE20',    '20% off (min ₹500, up to ₹300)',            'PERCENT', 20, 500, 300, now() + interval '1 year'),
    ('EXPIRED',   'Expired test coupon',                       'FIXED',   99, 0,   NULL, now() - interval '1 day');
