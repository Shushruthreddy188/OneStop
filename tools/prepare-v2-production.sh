#!/usr/bin/env bash
set -euo pipefail

# Run from ~/OneStop on the server before the first V2 production start.
# It is safe to rerun: databases and migrated addresses are created idempotently.
COMPOSE=(docker compose --env-file .env.production -f compose.prod.yml)
DATABASES=(search_db review_db wishlist_db coupon_db payment_db delivery_db address_db)

for database in "${DATABASES[@]}"; do
  "${COMPOSE[@]}" exec -T postgres psql -U onestop -d postgres -v db_name="$database" <<'SQL'
SELECT format('CREATE DATABASE %I OWNER onestop', :'db_name')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'db_name') \gexec
SQL
done

# Start Address Service once so Flyway creates its schema, then copy legacy MVP
# addresses without changing or deleting the Identity Service source rows.
"${COMPOSE[@]}" up -d address-service
until "${COMPOSE[@]}" exec -T postgres psql -U onestop -d address_db -tAc \
  "SELECT to_regclass('public.address')" | grep -qx address; do
  echo "Waiting for Address Service schema migration..."
  sleep 2
done
"${COMPOSE[@]}" exec -T postgres psql -U onestop -d identity_db -c \
  "COPY (SELECT id,user_id,NULL::text,NULL::text,NULL::text,line1,line2,city,state,postal_code,country,is_default FROM address ORDER BY id) TO STDOUT WITH CSV" |
"${COMPOSE[@]}" exec -T postgres psql -U onestop -d address_db -c \
  "CREATE TEMP TABLE legacy_address (id bigint,customer_id bigint,label text,recipient_name text,phone text,line1 text,line2 text,city text,state text,postal_code text,country text,is_default boolean); COPY legacy_address FROM STDIN WITH CSV; INSERT INTO address (id,customer_id,label,recipient_name,phone,line1,line2,city,state,postal_code,country,is_default) SELECT * FROM legacy_address ON CONFLICT (id) DO NOTHING; SELECT setval(pg_get_serial_sequence('address','id'), GREATEST((SELECT COALESCE(MAX(id),1) FROM address),1));"

echo "V2 databases and legacy addresses are ready."
