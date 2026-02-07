-- Core ecommerce schema (Flyway V2)

CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'CHECKED_OUT')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (cart_id, product_id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id) ON DELETE RESTRICT,
    cart_id BIGINT REFERENCES cart(id) ON DELETE SET NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('CREATED', 'AWAITING_PAYMENT', 'PAID', 'CANCELLED')),
    total_amount NUMERIC(12, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (order_id, product_id)
);

CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('SUCCESS', 'FAIL')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE RESTRICT,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE inventory_movement (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    type VARCHAR(32) NOT NULL CHECK (type IN ('STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT')),
    quantity_delta INTEGER NOT NULL CHECK (quantity_delta <> 0),
    related_order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_cart_customer_id ON cart(customer_id);
CREATE INDEX idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX idx_cart_item_product_id ON cart_item(product_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_cart_id ON orders(cart_id);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);
CREATE INDEX idx_order_item_product_id ON order_item(product_id);
CREATE INDEX idx_payment_order_id ON payment(order_id);
CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_inventory_movement_product_id ON inventory_movement(product_id);
CREATE INDEX idx_inventory_movement_related_order_id ON inventory_movement(related_order_id);

-- VERIFY STEPS
-- A) Start app to run Flyway:
--    - Command (PowerShell): ./mvnw spring-boot:run
--    - In logs, confirm:
--      * Flyway starts
--      * Migrating schema to version "2" (or similar)
--      * Successfully applied 1 migration
--      * 0 failed
-- B) DB sanity checks (run in your SQL client):
--    1) List Flyway history rows:
--       - SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC;
--       - Expect: version '2' row with success=true
--    2) List newly created tables:
--       - SELECT table_name FROM information_schema.tables
--         WHERE table_schema='public'
--         ORDER BY table_name;
--       - Expect to see: customer, cart, cart_item, orders, order_item, payment, order_status_history, inventory_movement
--    3) Quick constraint presence check (no guessing columns beyond table names):
--       - SELECT tc.table_name, tc.constraint_type, tc.constraint_name
--         FROM information_schema.table_constraints tc
--         WHERE tc.table_schema='public'
--           AND tc.table_name IN ('customer','cart','cart_item','orders','order_item','payment','order_status_history','inventory_movement')
--         ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;
