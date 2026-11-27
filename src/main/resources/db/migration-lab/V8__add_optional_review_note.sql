ALTER TABLE public.flyway_lab_deployment_precheck
    ADD COLUMN review_note VARCHAR(250);

COMMENT ON COLUMN public.flyway_lab_deployment_precheck.review_note
    IS 'Optional explanatory note for the deployment precheck.';
