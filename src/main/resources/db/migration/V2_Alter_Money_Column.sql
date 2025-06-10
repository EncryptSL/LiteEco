--- SQL Migration Statements for Flyway ---
ALTER TABLE lite_eco_dollars MODIFY COLUMN money DECIMAL(30, 2) NOT NULL;
ALTER TABLE lite_eco_credits MODIFY COLUMN money DECIMAL(30, 2) NOT NULL;
------------------------------------------

--- CREATE TABLE statement for lite_eco_dollars ---
------------------------------------------------------------------
--- CREATE TABLE statement for lite_eco_credits ---
------------------------------------------------------------------