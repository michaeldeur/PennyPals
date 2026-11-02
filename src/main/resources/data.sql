-- 1. Insert default test users (with BCrypt passwords and security answers)
MERGE INTO users (id, username, password, email, security_question, security_answer)
    KEY(id)
    VALUES (1, 'demo_user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi', 'demo@pennypals.com', 1, '$2a$10$eJER5CE1gLYjNsJnOy4W3O1De.GOyuxjn7elTaWsjHX4aaJnN03XO');

MERGE INTO users (id, username, password, email, security_question, security_answer)
    KEY(id)
    VALUES (10, 'a@gmail.com', '$2a$10$eJER5CE1gLYjNsJnOy4W3O1De.GOyuxjn7elTaWsjHX4aaJnN03XO', 'a@gmail.com', 1, '$2a$10$eJER5CE1gLYjNsJnOy4W3O1De.GOyuxjn7elTaWsjHX4aaJnN03XO');

-- 2. Insert corresponding user profiles
MERGE INTO user_profiles (id, user_id, pennies, updated_at)
    KEY(id)
    VALUES (1, 1, 150, CURRENT_TIMESTAMP());

MERGE INTO user_profiles (id, user_id, pennies, updated_at)
    KEY(id)
    VALUES (2, 10, 100, CURRENT_TIMESTAMP());

-- 3. Insert initial transactions (Income & Expense)
MERGE INTO transactions (id, user_id, type, category, amount, date, notes)
    KEY(id)
    VALUES (1, 1, 'INCOME', 'Salary', 1200.00, '2026-10-01', 'Monthly stipend');

MERGE INTO transactions (id, user_id, type, category, amount, date, notes)
    KEY(id)
    VALUES (2, 1, 'EXPENSE', 'Food', 45.50, '2026-10-02', 'Grocery store run');

-- 4. Insert initial budget goals / spending limits
MERGE INTO budgets (id, user_id, type, category, monthly_limit, current_progress, is_completed)
    KEY(id)
    VALUES (1, 1, 'SPENDING_LIMIT', 'Food', 200.00, 45.50, FALSE);

-- 5. Insert shop items
MERGE INTO unlocked_items (id, user_id, item_name, item_type, color_hex, currency_type, cost, is_purchased, is_equipped)
    KEY(id)
    VALUES (1, 1, 'Goldfish', 'FISH', '#FFA500', 'PENNIES', 50, TRUE, TRUE);

MERGE INTO unlocked_items (id, user_id, item_name, item_type, color_hex, currency_type, cost, is_purchased, is_equipped)
    KEY(id)
    VALUES (2, 1, 'Treasure Chest', 'DECOR', '#8B4513', 'PENNIES', 100, FALSE, FALSE);

-- 6. Reset H2 auto-increment sequences for all tables to avoid primary key collisions on new inserts
ALTER TABLE users ALTER COLUMN id RESTART WITH 20;
ALTER TABLE user_profiles ALTER COLUMN id RESTART WITH 20;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 20;
ALTER TABLE budgets ALTER COLUMN id RESTART WITH 20;
ALTER TABLE unlocked_items ALTER COLUMN id RESTART WITH 20;