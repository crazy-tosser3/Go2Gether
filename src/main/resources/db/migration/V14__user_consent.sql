ALTER TABLE users ADD COLUMN consent_at             TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN consent_policy_version VARCHAR(20);

COMMENT ON COLUMN users.consent_at             IS 'Когда пользователь дал согласие на обработку ПДн (NULL — пользователь создан до V14 или сидом)';
COMMENT ON COLUMN users.consent_policy_version IS 'Версия политики обработки ПДн, с которой согласился пользователь';