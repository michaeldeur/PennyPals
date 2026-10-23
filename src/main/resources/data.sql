-- 1. Insert a default test user (Password is plaintext for now; will be hashed in Checkpoint 3 with Spring Security)
INSERT INTO users (id, username, password, email, security_question, security_answer)
VALUES (1, 'demo_user', 'password123', 'demo@pennypals.com', 1, 'Blue');

-- 2. Insert corresponding user profile with initial pennies and timestamp
INSERT INTO user_profiles (id, user_id, pennies, updated_at)
VALUES (1, 1, 150, CURRENT_TIMESTAMP());

-- 3. Insert initial transactions (Income & Expense)
INSERT INTO transactions (id, user_id, type, category, amount, date, notes)
VALUES (1, 1, 'INCOME', 'Salary', 1200.00, '2026-10-01', 'Monthly stipend');

INSERT INTO transactions (id, user_id, type, category, amount, date, notes)
VALUES (2, 1, 'EXPENSE', 'Food', 45.50, '2026-10-02', 'Grocery store run');

-- 4. Insert initial budget goals / spending limits
INSERT INTO budgets (id, user_id, type, category, monthly_limit, current_progress, is_completed)
VALUES (1, 1, 'SPENDING_LIMIT', 'Food', 200.00, 45.50, FALSE);

-- 5. Insert shop items (Unlocked / Purchased items for the aquarium canvas)
INSERT INTO unlocked_items (id, user_id, item_name, item_type, color_hex, currency_type, cost, is_purchased, is_equipped)
VALUES (1, 1, 'Goldfish', 'FISH', '#FFA500', 'PENNIES', 50, TRUE, TRUE);

INSERT INTO unlocked_items (id, user_id, item_name, item_type, color_hex, currency_type, cost, is_purchased, is_equipped)
VALUES (2, 1, 'Treasure Chest', 'DECOR', '#8B4513', 'PENNIES', 100, FALSE, FALSE);