-- Creates one database per service (database-per-service pattern) inside the
-- single local PostgreSQL instance used by docker-compose. The `onestop` role
-- is created by the container from POSTGRES_USER and owns all of these.

CREATE DATABASE identity_db;
CREATE DATABASE catalog_db;
CREATE DATABASE cart_db;
CREATE DATABASE inventory_db;
CREATE DATABASE order_db;
CREATE DATABASE notification_db;
CREATE DATABASE search_db;
CREATE DATABASE review_db;
CREATE DATABASE wishlist_db;
CREATE DATABASE coupon_db;
CREATE DATABASE payment_db;
CREATE DATABASE delivery_db;
CREATE DATABASE address_db;
