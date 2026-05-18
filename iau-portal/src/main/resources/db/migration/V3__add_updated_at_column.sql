-- Add updated_at column to complaints table if it does not exist
SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'complaints'
    AND COLUMN_NAME = 'updated_at'
);

SET @s = IF(@col_exists = 0,
  'ALTER TABLE `complaints` ADD COLUMN `updated_at` DATETIME NULL;',
  'SELECT "updated_at column already exists";'
);

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
