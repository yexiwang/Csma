ALTER TABLE orders
    ADD COLUMN delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER pack_amount,
    ADD COLUMN tableware_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER delivery_fee;
