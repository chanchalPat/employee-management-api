ALTER TABLE public.flyway_lab_probe
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NEW';
