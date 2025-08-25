-- Sample users for testing
-- Note: Password is BCrypt hash of "Password123!" (meets validation: capital, number, special char, 8+ chars)
INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at, updated_at) 
VALUES 
  ('550e8400-e29b-41d4-a716-446655440001', 'testuser', 'test@example.com', '$2a$10$e8mgbLygn.qeO60Qx1mTUOwQ3weG/WFkdoeI4wRxbbzZEwZFYdZJy', 'Test', 'User', 'PRIMARY_USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at, updated_at) 
VALUES 
  ('550e8400-e29b-41d4-a716-446655440002', 'admin', 'admin@example.com', '$2a$10$e8mgbLygn.qeO60Qx1mTUOwQ3weG/WFkdoeI4wRxbbzZEwZFYdZJy', 'Admin', 'User', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at, updated_at) 
VALUES 
  ('550e8400-e29b-41d4-a716-446655440003', 'doctor', 'doctor@example.com', '$2a$10$e8mgbLygn.qeO60Qx1mTUOwQ3weG/WFkdoeI4wRxbbzZEwZFYdZJy', 'Dr. Jane', 'Smith', 'SECONDARY_USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, created_at, updated_at) 
VALUES 
  ('550e8400-e29b-41d4-a716-446655440004', 'primaryuser', 'primaryuser@example.com', '$2a$10$e8mgbLygn.qeO60Qx1mTUOwQ3weG/WFkdoeI4wRxbbzZEwZFYdZJy', 'Primary', 'User', 'PRIMARY_USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);