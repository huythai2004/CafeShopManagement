-- TẠO DATABASE
IF DB_ID('CafeShopManagement') IS NULL
BEGIN
    CREATE DATABASE CafeShopManagement;
END
GO

USE CafeShopManagement;
GO

-- Sequence cho order_number
IF NOT EXISTS (SELECT * FROM sys.sequences WHERE name = 'seq_order_number')
BEGIN
    CREATE SEQUENCE seq_order_number
    START WITH 1
    INCREMENT BY 1;
END
GO

-- ===========================
-- USERS (Admin & Staff)
-- ===========================
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL, -- đã hash
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    phone NVARCHAR(20),
    role NVARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- ===========================
-- CUSTOMERS (đăng ký khách hàng)
-- ===========================
CREATE TABLE customers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE,
    full_name NVARCHAR(100),
    email NVARCHAR(100) UNIQUE,
    phone NVARCHAR(20),
    notes NVARCHAR(500),
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- ===========================
-- CATEGORIES
-- ===========================
CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME()
);
GO

-- ===========================
-- PRODUCTS
-- ===========================
CREATE TABLE products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(1000),
    category_id BIGINT NOT NULL,
    default_price DECIMAL(12,2) NOT NULL, -- latest base price
    cost_price DECIMAL(12,2),
    image_url NVARCHAR(500),
    is_available BIT DEFAULT 1,
    unavailable_reason NVARCHAR(200),
    is_featured BIT DEFAULT 0,
    display_order INT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);
GO

CREATE INDEX IDX_products_category ON products(category_id);
GO

-- ===========================
-- PRODUCT_PRICES (history)
-- ===========================
CREATE TABLE product_prices (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    sale_price DECIMAL(12,2) NULL,
    start_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    end_at DATETIME2 NULL,
    note NVARCHAR(500),
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    created_by_user_id BIGINT NULL,
    CONSTRAINT FK_pp_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT FK_pp_user FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);
GO

CREATE INDEX IDX_product_prices_product ON product_prices(product_id);
GO

-- ===========================
-- PRODUCT_MODIFIERS (topping/size/option)
-- ===========================
CREATE TABLE product_modifiers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name NVARCHAR(100) NOT NULL, -- e.g., "Extra shot", "Syrup", "Size L"
    price_delta DECIMAL(12,2) DEFAULT 0, -- thêm chi phí
    is_default BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_pm_product FOREIGN KEY (product_id) REFERENCES products(id)
);
GO

CREATE INDEX IDX_product_modifiers_product ON product_modifiers(product_id);
GO

-- ===========================
-- PROMOTIONS
-- ===========================
CREATE TABLE promotions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) UNIQUE, -- optional promo code
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    discount_type NVARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENT','AMOUNT')),
    discount_value DECIMAL(12,2) NOT NULL, -- percent (0-100) or fixed amount
    starts_at DATETIME2 NOT NULL,
    ends_at DATETIME2 NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    created_by_user_id BIGINT NULL,
    CONSTRAINT FK_promo_user FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);
GO

-- Mapping promotions -> products (nếu áp cho từng sp)
CREATE TABLE promotion_products (
    promotion_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, product_id),
    CONSTRAINT FK_pprom_promo FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    CONSTRAINT FK_pprom_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
GO

-- ===========================
-- ORDERS
-- ===========================
CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_number NVARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NULL,                -- nếu khách đăng nhập
    guest_name NVARCHAR(100) NULL,          -- nếu khách vãng lai
    guest_phone NVARCHAR(20) NULL,
    status NVARCHAR(20) NOT NULL CHECK (status IN ('PENDING','APPROVED','COOKING','IN_PROGRESS','DONE','CANCELLED','REFUNDED')),
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    promotion_id BIGINT NULL, -- order-level promotion (optional)
    customer_notes NVARCHAR(1000),
    created_by_staff_id BIGINT NULL,         -- staff who created or accepted
    approved_by_staff_id BIGINT NULL,
    estimated_completion_time DATETIME2 NULL,
    actual_completion_time DATETIME2 NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT FK_orders_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id),
    CONSTRAINT FK_orders_created_by FOREIGN KEY (created_by_staff_id) REFERENCES users(id),
    CONSTRAINT FK_orders_approved_by FOREIGN KEY (approved_by_staff_id) REFERENCES users(id)
);
GO

CREATE INDEX IDX_orders_status_createdat ON orders(status, created_at);
CREATE INDEX IDX_orders_customer ON orders(customer_id);
GO

-- ===========================
-- ORDER_ITEMS
-- ===========================
CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name NVARCHAR(150) NOT NULL, -- denormalized for history
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12,2) NOT NULL,  -- price at time of order (base)
    modifiers_price DECIMAL(12,2) DEFAULT 0, -- tổng modifier price
    total_price DECIMAL(12,2) NOT NULL, -- (unit_price + modifiers_price) * quantity
    customization_notes NVARCHAR(500),
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_oi_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_oi_product FOREIGN KEY (product_id) REFERENCES products(id)
);
GO

CREATE INDEX IDX_order_items_order ON order_items(order_id);
GO

-- ===========================
-- ORDER_ITEM_MODIFIERS (cho từng item)
-- ===========================
CREATE TABLE order_item_modifiers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_item_id BIGINT NOT NULL,
    modifier_name NVARCHAR(150) NOT NULL,
    price_delta DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_oim_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE
);
GO

-- ===========================
-- PAYMENTS
-- ===========================
CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE, -- 1-1 (một order một payment) - adjust if multiple partial payments needed
    payment_method NVARCHAR(20) NOT NULL CHECK (payment_method IN ('CASH','CARD','QR_CODE','WALLET')),
    amount DECIMAL(12,2) NOT NULL,
    qr_code_data NVARCHAR(2000) NULL,
    qr_expires_at DATETIME2 NULL,
    status NVARCHAR(20) NOT NULL CHECK (status IN ('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED')),
    transaction_id NVARCHAR(200) NULL,
    gateway_response NVARCHAR(2000) NULL,
    paid_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_pay_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
GO
CREATE TABLE tables (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    table_number INT NOT NULL UNIQUE,
    name NVARCHAR(50),
    capacity INT DEFAULT 4,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME()
);

CREATE INDEX IDX_payments_status ON payments(status);
GO

-- ===========================
-- INVOICES (tùy chọn)
-- ===========================
CREATE TABLE invoices (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    invoice_number NVARCHAR(100) NOT NULL UNIQUE,
    printed_by_user_id BIGINT NULL,
    printed_at DATETIME2 NULL,
    pdf_url NVARCHAR(500) NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_inv_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT FK_inv_user FOREIGN KEY (printed_by_user_id) REFERENCES users(id)
);
GO

-- ===========================
-- TRIGGER: CẬP NHẬT updated_at tự động
-- Applies to multiple tables using similar trigger logic
-- ===========================

-- Function to create trigger for a given table (we'll create for main tables)
-- For SQL Server we create separate triggers per table:

-- users
IF OBJECT_ID('TRG_users_upd_ts') IS NOT NULL DROP TRIGGER TRG_users_upd_ts;
GO
CREATE TRIGGER TRG_users_upd_ts
ON users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE users
    SET updated_at = SYSUTCDATETIME()
    FROM users u
    JOIN inserted i ON u.id = i.id;
END
GO

-- products
IF OBJECT_ID('TRG_products_upd_ts') IS NOT NULL DROP TRIGGER TRG_products_upd_ts;
GO
CREATE TRIGGER TRG_products_upd_ts
ON products
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE products
    SET updated_at = SYSUTCDATETIME()
    FROM products p
    JOIN inserted i ON p.id = i.id;
END
GO

-- orders
IF OBJECT_ID('TRG_orders_upd_ts') IS NOT NULL DROP TRIGGER TRG_orders_upd_ts;
GO
CREATE TRIGGER TRG_orders_upd_ts
ON orders
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE orders
    SET updated_at = SYSUTCDATETIME()
    FROM orders o
    JOIN inserted i ON o.id = i.id;
END
GO

-- payments
IF OBJECT_ID('TRG_payments_upd_ts') IS NOT NULL DROP TRIGGER TRG_payments_upd_ts;
GO
CREATE TRIGGER TRG_payments_upd_ts
ON payments
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE payments
    SET updated_at = SYSUTCDATETIME()
    FROM payments p
    JOIN inserted i ON p.id = i.id;
END
GO

-- ===========================
-- STORED PROCEDURE helper: CREATE_ORDER_NUMBER
-- ===========================
IF OBJECT_ID('sp_generate_order_number') IS NOT NULL
    DROP PROCEDURE sp_generate_order_number;
GO

CREATE PROCEDURE sp_generate_order_number
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @seq BIGINT = NEXT VALUE FOR seq_order_number;
    -- format: YYYYMMDD + seq (6 digits) e.g., 20251212-000001
    DECLARE @prefix NVARCHAR(20) = FORMAT(SYSUTCDATETIME(), 'yyyyMMdd');
    DECLARE @num NVARCHAR(20) = RIGHT('000000' + CAST(@seq AS NVARCHAR(20)), 6);
    DECLARE @order_number NVARCHAR(50) = @prefix + '-' + @num;
    SELECT @order_number AS order_number;
END
GO

-- ===========================
-- SAMPLE DATA (một vài bản ghi)
-- ===========================
-- USERS
INSERT INTO users (username, password, full_name, email, phone, role)
VALUES
('admin', 'bcrypt-hash-admin', 'System Administrator', 'admin@highland.com', '024-1234-5678', 'ADMIN'),
('staff1', 'bcrypt-hash-staff1', 'John Doe', 'john@highland.com', '024-1234-5679', 'STAFF');
GO

-- CATEGORIES
INSERT INTO categories (name, description, display_order) VALUES
('Coffee', 'All coffee drinks', 1),
('Tea', 'Tea varieties', 2),
('Smoothies', 'Fresh smoothies', 3),
('Pastries', 'Baked goods', 4);
GO

-- PRODUCTS
INSERT INTO products (name, description, category_id, default_price, cost_price, image_url, is_featured)
VALUES
('Americano', 'Classic black coffee', 1, 45000, 15000, 'https://images.unsplash.com/photo-1551030173-122aabc4489c', 1),
('Cappuccino', 'Espresso + steamed milk', 1, 55000, 20000, 'https://images.unsplash.com/photo-1572442388796-11668a67e53d', 0),
('Green Tea', 'Japanese green tea', 2, 35000, 10000, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc', 0),
('Mango Smoothie', 'Fresh mango + yogurt', 3, 70000, 30000, 'https://images.unsplash.com/photo-1553530666-ba11a7da3888', 1);
GO

-- PRODUCT_PRICES (initial)
INSERT INTO product_prices (product_id, price, sale_price, start_at, created_by_user_id)
SELECT id, default_price, NULL, SYSUTCDATETIME(), 1 FROM products;
GO

-- PRODUCT_MODIFIERS
INSERT INTO product_modifiers (product_id, name, price_delta, is_default)
VALUES
(1, 'Extra shot', 8000, 0),
(1, 'Large size (L)', 10000, 0),
(2, 'Vanilla syrup', 5000, 0),
(4, 'Add boba', 12000, 0);
GO

-- PROMOTION example
INSERT INTO promotions (code, title, discount_type, discount_value, starts_at, ends_at, is_active, created_by_user_id)
VALUES
('SUMMER10', 'Summer 10% off', 'PERCENT', 10, SYSUTCDATETIME(), DATEADD(day, 30, SYSUTCDATETIME()), 1, 1);
GO

-- MAP promotion to product (e.g., Mango Smoothie)
INSERT INTO promotion_products (promotion_id, product_id)
SELECT p.id, pr.id
FROM promotions p CROSS JOIN products pr
WHERE p.code = 'SUMMER10' AND pr.name = 'Mango Smoothie';
GO

-- Tạo 1 order mẫu
DECLARE @onr NVARCHAR(50);
EXEC sp_generate_order_number;
-- lấy result vào biến (do SP select), ta tạo bằng cách tái gọi sequence:
DECLARE @seq BIGINT = NEXT VALUE FOR seq_order_number;
SET @onr = FORMAT(SYSUTCDATETIME(), 'yyyyMMdd') + '-' + RIGHT('000000' + CAST(@seq AS NVARCHAR(20)), 6);

INSERT INTO orders (order_number, guest_name, guest_phone, status, subtotal, tax_amount, discount_amount, total_amount, customer_notes, created_at)
VALUES (@onr, 'Nguyen Van A', '0909123456', 'PENDING', 45000, 0, 0, 45000, 'No sugar', SYSUTCDATETIME());

DECLARE @order_id BIGINT = SCOPE_IDENTITY();

-- order item
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, modifiers_price, total_price)
SELECT @order_id, p.id, p.name, 1, p.default_price, 0, p.default_price
FROM products p WHERE p.name = 'Americano';

-- create payment record (pending)
INSERT INTO payments (order_id, payment_method, amount, status, created_at)
VALUES (@order_id, 'QR_CODE', 45000, 'PENDING', SYSUTCDATETIME());
GO

-- ===========================
-- END SCHEMA
-- ===========================
