-- Статус заявки организации
CREATE TYPE org_status AS ENUM (
    'PENDING',   -- ожидает решения администратора
    'APPROVED',  -- допущена, может публиковать события
    'REJECTED'   -- отклонена администратором
);

CREATE TABLE organizations (
    id              BIGSERIAL PRIMARY KEY,
    owner_id        BIGINT       NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    -- ON DELETE RESTRICT: нельзя удалить пользователя пока у него есть организация

    -- Публичная информация
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    sports_description VARCHAR(500),   -- «Чем занимается организация»
    city            VARCHAR(100),
    website_url     VARCHAR(500),
    avatar_url      VARCHAR(500),

    -- Модерация
    status          org_status   NOT NULL DEFAULT 'PENDING',
    reject_reason   TEXT,              -- заполняет админ при отклонении
    decided_by      BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    decided_at      TIMESTAMPTZ,

    -- Служебные
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Один владелец может иметь несколько организаций (кружок и федерация)
CREATE INDEX idx_org_owner    ON organizations (owner_id);
CREATE INDEX idx_org_status   ON organizations (status);

-- Поиск организаций по имени в админке
CREATE INDEX idx_org_name     ON organizations (LOWER(name));

COMMENT ON COLUMN organizations.status        IS 'PENDING→APPROVED или PENDING→REJECTED администратором';
COMMENT ON COLUMN organizations.reject_reason IS 'Причина отклонения, видна представителю организации';
COMMENT ON COLUMN organizations.decided_by    IS 'ID администратора принявшего решение';