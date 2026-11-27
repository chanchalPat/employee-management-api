UPDATE flyway_lab_probe
SET department_code = 'UNASSIGNED'
WHERE department_code IS NULL;
