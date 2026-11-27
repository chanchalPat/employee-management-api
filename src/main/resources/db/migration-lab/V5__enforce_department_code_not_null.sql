-- V5: Enforce department_code as a mandatory field.
-- V4 backfilled existing NULL values before this constraint is applied.

ALTER TABLE public.flyway_lab_probe
ALTER COLUMN department_code SET NOT NULL;
