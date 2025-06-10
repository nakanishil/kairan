-- データベース作成
-- CREATE DATABASE IF NOT EXISTS kairan_db;
-- USE kairan_db;

CREATE TABLE IF NOT EXISTS init_log (
    message VARCHAR(255)
);

-- 1. 町内会情報管理テーブル
CREATE TABLE IF NOT EXISTS districts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    region_code VARCHAR(20) NOT NULL,
    association VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE (association, area)
);

-- 2. 役職テーブル
CREATE TABLE IF NOT EXISTS roles (
    id SMALLINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- 3. 委員区分テーブル
CREATE TABLE IF NOT EXISTS committee_classification (
    id SMALLINT PRIMARY KEY AUTO_INCREMENT,
    district_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE RESTRICT
    
);

-- 4. ユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(40) UNIQUE NOT NULL,
    user_id VARCHAR(32) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(35) NOT NULL,
    furigana VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    district_id INT NULL,
    role_id SMALLINT NOT NULL,
    committee_id SMALLINT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    google_id VARCHAR(255),
    google_linked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (committee_id) REFERENCES committee_classification(id) ON DELETE SET NULL
);

-- 5. 回覧板テーブル
CREATE TABLE IF NOT EXISTS circulars (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    author_id INT NULL,
    district_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 6. 回覧板ファイルテーブル
CREATE TABLE IF NOT EXISTS circular_files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    circular_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (circular_id) REFERENCES circulars(id) ON DELETE CASCADE
);

-- 7. 回覧板既読状況確認テーブル
CREATE TABLE IF NOT EXISTS circular_reads (
    id INT PRIMARY KEY AUTO_INCREMENT,
    circular_id INT NOT NULL,
    user_id INT NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (circular_id) REFERENCES circulars(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (circular_id, user_id)
);

-- 8. 掲示板タイプ（board_types）テーブル
CREATE TABLE IF NOT EXISTS board_types (
    id SMALLINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 9. 掲示板メッセージ（messages）テーブル
CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NULL,
    board_type_id SMALLINT NOT NULL, -- board_types(id)
    committee_type SMALLINT NULL, -- committee_classification(id)
    title VARCHAR(100),
    comment TEXT NOT NULL,
    parent_id INT NULL,
    status VARCHAR(50) NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (board_type_id) REFERENCES board_types(id) ON DELETE CASCADE
);


-- 9. 支払い方法テーブル
CREATE TABLE IF NOT EXISTS payment_methods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 支払い履歴テーブル
CREATE TABLE IF NOT EXISTS payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method_id INT NOT NULL,
    membership_fee_id INT NOT NULL,
    status VARCHAR(50),
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id) ON DELETE CASCADE
);

-- 11. メール認証用トークンテーブル
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    token VARCHAR(255) NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. パスワードリセット用トークンテーブル
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 13. 会計カテゴリテーブル
CREATE TABLE IF NOT EXISTS accounting_categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 14. 会計データテーブル
CREATE TABLE IF NOT EXISTS accounting (
    id INT PRIMARY KEY AUTO_INCREMENT,
    district_id INT NOT NULL,
    recorded_by INT NOT NULL,
    type ENUM('収入','支出') NOT NULL,
    account_category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    description TEXT NULL,
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_category_id) REFERENCES accounting_categories(id) ON DELETE CASCADE
);


-- 年会費金額管理テーブル
CREATE TABLE IF NOT EXISTS membership_fees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    district_id INT NOT NULL,
    recorded_by INT NOT NULL,
    year INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE,
    UNIQUE (district_id, year)
);
