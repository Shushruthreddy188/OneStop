-- Seed catalog_db from the DMart dataset staging table (dmart_raw).
--
-- Prerequisite: load the Kaggle DMart CSV into a staging table first:
--   CREATE TABLE dmart_raw (name text, brand text, price text, discounted_price text,
--     category text, sub_category text, quantity text, description text, breadcrumbs text);
--   COPY dmart_raw FROM '/path/DMart.csv' WITH (FORMAT csv, HEADER true);
--
-- This script is idempotent: it truncates and repopulates brand/category/product.

BEGIN;

TRUNCATE product, category, brand RESTART IDENTITY CASCADE;

-- Brands (distinct, non-empty).
INSERT INTO brand (name)
SELECT DISTINCT trim(brand)
FROM dmart_raw
WHERE NULLIF(trim(brand), '') IS NOT NULL;

-- Top-level categories.
INSERT INTO category (name, parent_category_id)
SELECT DISTINCT trim(category), NULL::bigint
FROM dmart_raw
WHERE NULLIF(trim(category), '') IS NOT NULL;

-- Sub-categories, parented to their category. A leading "<Category>/" prefix
-- in the sub_category value is stripped so "Grocery/Dry Fruits" and "Dry Fruits"
-- collapse to one leaf under Grocery.
WITH sub AS (
    SELECT DISTINCT
        trim(category) AS cat,
        CASE
            WHEN left(trim(sub_category), length(trim(category)) + 1) = trim(category) || '/'
                THEN substr(trim(sub_category), length(trim(category)) + 2)
            ELSE trim(sub_category)
        END AS sub_clean
    FROM dmart_raw
    WHERE NULLIF(trim(sub_category), '') IS NOT NULL
      AND NULLIF(trim(category), '') IS NOT NULL
)
INSERT INTO category (name, parent_category_id)
SELECT DISTINCT s.sub_clean, p.id
FROM sub s
JOIN category p ON p.name = s.cat AND p.parent_category_id IS NULL
WHERE s.sub_clean <> '';

-- Products. Generate stable sequential SKUs; map to the leaf category when a
-- sub-category exists, else the top-level category. Prices are sanitized to
-- digits/decimal point to avoid cast failures; selling price falls back to MRP.
WITH prepared AS (
    SELECT
        r.name,
        r.description,
        r.quantity,
        NULLIF(trim(r.brand), '') AS brand_name,
        trim(r.category) AS cat,
        CASE
            WHEN left(trim(r.sub_category), length(trim(r.category)) + 1) = trim(r.category) || '/'
                THEN substr(trim(r.sub_category), length(trim(r.category)) + 2)
            ELSE NULLIF(trim(r.sub_category), '')
        END AS sub_clean,
        NULLIF(regexp_replace(trim(r.price), '[^0-9.]', '', 'g'), '')::numeric AS mrp_v,
        COALESCE(
            NULLIF(regexp_replace(trim(r.discounted_price), '[^0-9.]', '', 'g'), '')::numeric,
            NULLIF(regexp_replace(trim(r.price), '[^0-9.]', '', 'g'), '')::numeric
        ) AS sell_v,
        row_number() OVER (ORDER BY r.name, r.sub_category, r.quantity, r.price) AS rn
    FROM dmart_raw r
)
INSERT INTO product
    (sku, name, description, brand_id, category_id, package_size, image_url, mrp, selling_price, status)
SELECT
    'DMART-' || lpad(p.rn::text, 6, '0'),
    p.name,
    NULLIF(trim(p.description), ''),
    b.id,
    COALESCE(child.id, parent.id),
    NULLIF(trim(p.quantity), ''),
    NULL,
    p.mrp_v,
    p.sell_v,
    'ACTIVE'
FROM prepared p
LEFT JOIN brand b ON b.name = p.brand_name
LEFT JOIN category parent ON parent.name = p.cat AND parent.parent_category_id IS NULL
LEFT JOIN category child ON child.name = p.sub_clean AND child.parent_category_id = parent.id
WHERE p.sell_v IS NOT NULL
  AND NULLIF(trim(p.name), '') IS NOT NULL;

COMMIT;
