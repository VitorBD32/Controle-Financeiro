-- fix_auth.sql
-- Use this script to create an app user that uses mysql_native_password
-- or to alter an existing user to use mysql_native_password if PHP's MySQL client
-- on your system does not support caching_sha2_password (MySQL 8 default).

-- Option A: create a dedicated user for the app (recommended)
-- Replace 'app_password_here' with a secure password before running.
CREATE USER IF NOT EXISTS 'app_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'app_password_here';
GRANT ALL PRIVILEGES ON PROVA1.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;

-- Option B: alter root (or another existing user) to use mysql_native_password
-- CAUTION: altering root's authentication can affect other services.
-- ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_root_password_here';
-- FLUSH PRIVILEGES;

-- Notes:
-- 1) Run with: mysql -u root -p < fix_auth.sql
-- 2) Prefer creating a dedicated app user (Option A) and update your PHP stubs
--    to use those credentials (edit $DB_USER / $DB_PASS at the top of the PHP files).
