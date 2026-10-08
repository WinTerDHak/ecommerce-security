-- Seed Admin User (Pre-hashed with Argon2id)
INSERT INTO users (email, password_hash, first_name, last_name, role, active, created_at, updated_at)
VALUES ('admin@ecommerce.local', '$argon2id$v=19$m=65536,t=3,p=1$F15Detx6d9SypTnG5CZ2iw$77DyWtePvRSiuI8p/lZJavfEMYcknc57v3nflDErRgg', 'System', 'Admin', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Seed Categories
INSERT INTO categories (name, description, active, created_at) VALUES 
('Electronics', 'Gadgets and electronic devices', true, CURRENT_TIMESTAMP),
('Books', 'Physical and digital books', true, CURRENT_TIMESTAMP),
('Clothing', 'Apparel and accessories', true, CURRENT_TIMESTAMP),
('Home & Kitchen', 'Home appliances and kitchenware', true, CURRENT_TIMESTAMP),
('Sports', 'Sports equipment and outdoors', true, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Seed Products (Idempotent to prevent duplicates on restart)
INSERT INTO products (name, description, price, stock_quantity, category_id, active, created_at, updated_at)
SELECT v.name, v.description, v.price, v.stock_quantity, c.id, v.active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES 
  ('Smartphone X', 'Latest smartphone with OLED display', 24990000, 50, 'Electronics', true),
  ('Laptop Pro', 'High-performance laptop for professionals', 37490000, 30, 'Electronics', true),
  ('Wireless Earbuds', 'Noise-cancelling wireless earbuds', 3750000, 100, 'Electronics', true),
  ('Programming in Java', 'Comprehensive guide to Java programming', 1250000, 200, 'Books', true),
  ('Cybersecurity Basics', 'Introduction to web security', 990000, 150, 'Books', true),
  ('Cotton T-Shirt', 'Comfortable 100% cotton t-shirt', 490000, 500, 'Clothing', true),
  ('Running Shoes', 'Lightweight athletic running shoes', 2250000, 120, 'Clothing', true),
  ('Coffee Maker', 'Programmable coffee maker', 1990000, 80, 'Home & Kitchen', true),
  ('Blender', 'High-speed blender for smoothies', 1490000, 60, 'Home & Kitchen', true),
  ('Yoga Mat', 'Non-slip exercise yoga mat', 620000, 300, 'Sports', true),
  ('Dumbbell Set', 'Adjustable dumbbell set for home gym', 3250000, 40, 'Sports', true)
) AS v(name, description, price, stock_quantity, category_name, active)
JOIN categories c ON c.name = v.category_name
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE p.name = v.name
);
