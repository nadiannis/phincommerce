CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    payment_method VARCHAR(255) NOT NULL,
    billing_address TEXT NOT NULL,
    shipping_address TEXT NOT NULL,
    order_status VARCHAR(255) NOT NULL,
    total_amount FLOAT8 NOT NULL,
    customer_id BIGINT NOT NULL,
    order_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price FLOAT8 NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    CONSTRAINT order_items_order_id_fkey
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
