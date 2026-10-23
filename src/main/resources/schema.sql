CREATE TABLE IF NOT EXISTS users (
                                     id IDENTITY PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    security_question INT NOT NULL,
    security_answer VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS user_profiles (
                                             id IDENTITY PRIMARY KEY,
                                             user_id BIGINT NOT NULL UNIQUE,
                                             pennies INT NOT NULL DEFAULT 0,
                                             updated_at TIMESTAMP NOT NULL,
                                             CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS transactions (
                                            id IDENTITY PRIMARY KEY,
                                            user_id BIGINT NOT NULL,
                                            type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    date DATE NOT NULL,
    notes VARCHAR(500),
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS budgets (
                                       id IDENTITY PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    monthly_limit DECIMAL(10, 2) NOT NULL,
    current_progress DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS unlocked_items (
                                              id IDENTITY PRIMARY KEY,
                                              user_id BIGINT NOT NULL,
                                              item_name VARCHAR(100) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    color_hex VARCHAR(20) NOT NULL,
    currency_type VARCHAR(50) NOT NULL,
    cost INT NOT NULL,
    is_purchased BOOLEAN NOT NULL DEFAULT FALSE,
    is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_item_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Reset H2 identity counters so new test inserts start safely past seeded data
ALTER TABLE users ALTER COLUMN id RESTART WITH 10;
ALTER TABLE user_profiles ALTER COLUMN id RESTART WITH 10;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 10;
ALTER TABLE budgets ALTER COLUMN id RESTART WITH 10;
ALTER TABLE unlocked_items ALTER COLUMN id RESTART WITH 10;