-- Режим записи
CREATE TYPE join_mode AS ENUM (
    'OPEN',      -- свободное присоединение, сразу становишься участником
    'APPROVAL'   -- нужна заявка, организатор принимает или отклоняет
);

-- Статус публикации события
CREATE TYPE event_status AS ENUM (
    'PUBLISHED',    -- видно всем, можно записаться
    'UNPUBLISHED',  -- снято с публикации администратором
    'DELETED',      -- удалено администратором
    'CANCELLED'     -- отменено организатором (дополнительная возможность)
);

CREATE TABLE events (
    id               BIGSERIAL PRIMARY KEY,

    -- Организатор: ЛИБО пользователь ЛИБО организация, не оба сразу
    -- Проверяем это через CHECK ниже
    organizer_user_id BIGINT REFERENCES users(id)         ON DELETE RESTRICT,
    organizer_org_id  BIGINT REFERENCES organizations(id) ON DELETE RESTRICT,

    -- Основное содержимое
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    sport_id         BIGINT NOT NULL REFERENCES sports(id) ON DELETE RESTRICT,

    -- Когда и где
    starts_at        TIMESTAMPTZ  NOT NULL,
    ends_at          TIMESTAMPTZ,                   -- необязательно
    address          VARCHAR(500) NOT NULL,
    latitude         DECIMAL(9,6),                  -- для карты (дополнительная возможность)
    longitude        DECIMAL(9,6),

    -- Условия участия
    join_mode        join_mode    NOT NULL DEFAULT 'OPEN',
    capacity         INT          CHECK (capacity > 0),  -- NULL = без ограничения
    requirements     TEXT,        -- «нужны бутсы, минимальный уровень»

    -- Публикация
    status           event_status NOT NULL DEFAULT 'PUBLISHED',
    moderation_reason TEXT,       -- причина снятия/удаления, видна организатору

    -- Модерация: кто и когда снял
    moderated_by     BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    moderated_at     TIMESTAMPTZ,

    -- Оптимистичная блокировка при конкурентной записи
    -- Spring Data JPA сам проверяет version при save()
    version          BIGINT       NOT NULL DEFAULT 0,

    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    -- Организатор обязан быть указан ровно один
    CONSTRAINT chk_one_organizer CHECK (
        (organizer_user_id IS NOT NULL AND organizer_org_id IS NULL) OR
        (organizer_user_id IS NULL AND organizer_org_id IS NOT NULL)
    )
);

-- Главная лента: опубликованные будущие события
CREATE INDEX idx_events_feed ON events (status, starts_at)
    WHERE status = 'PUBLISHED';

-- Фильтр по виду спорта
CREATE INDEX idx_events_sport ON events (sport_id, status, starts_at);

-- События организации (страница организации)
CREATE INDEX idx_events_org ON events (organizer_org_id, status, starts_at)
    WHERE organizer_org_id IS NOT NULL;

-- События пользователя-организатора
CREATE INDEX idx_events_user ON events (organizer_user_id, status, starts_at)
    WHERE organizer_user_id IS NOT NULL;

-- Полнотекстовый поиск по названию и описанию
-- pg_trgm позволяет искать подстроки: «футб» найдёт «Футбол»
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_events_search ON events
    USING gin (to_tsvector('russian', title || ' ' || COALESCE(description, '')));

COMMENT ON COLUMN events.version           IS '@Version для оптимистичной блокировки JPA при записи';
COMMENT ON COLUMN events.moderation_reason IS 'Причина снятия/удаления, показывается организатору';
COMMENT ON COLUMN events.capacity          IS 'NULL = мест неограничено';