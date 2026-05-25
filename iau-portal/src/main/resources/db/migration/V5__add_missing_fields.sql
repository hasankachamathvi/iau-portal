-- Flyway migration: add missing fields to complaints for evidence types, witness names, additional info and declaration flags

ALTER TABLE `complaints`
  ADD COLUMN IF NOT EXISTS `evidence_types` JSON DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS `witness_names` VARCHAR(1000),
  ADD COLUMN IF NOT EXISTS `additional_info` LONGTEXT,
  ADD COLUMN IF NOT EXISTS `declaration_confirmed` TINYINT(1) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `declaration_acknowledged` TINYINT(1) DEFAULT 0;

-- If your MySQL version doesn't support ADD COLUMN IF NOT EXISTS, run equivalent checks using INFORMATION_SCHEMA or create individual ALTER statements guarded by conditional logic.
