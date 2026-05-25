-- Helper: create a MySQL database and user for the IAU portal.
-- Replace PASSWORD_PLACEHOLDER with a secure password before running.

CREATE DATABASE IF NOT EXISTS `iau_portal` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
USE `iau_portal`;

-- Create application user (change password)
CREATE USER IF NOT EXISTS 'iau_user'@'localhost' IDENTIFIED BY 'PASSWORD_PLACEHOLDER';
GRANT ALL PRIVILEGES ON `iau_portal`.* TO 'iau_user'@'localhost';
FLUSH PRIVILEGES;

-- Optionally run Flyway migrations (if using Flyway):
-- flyway -url=jdbc:mysql://localhost:3306/iau_portal -user=iau_user -password=PASSWORD run
