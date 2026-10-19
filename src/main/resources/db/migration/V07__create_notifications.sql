-- Тип уведомления
CREATE TYPE notification_type AS ENUM (
    'REGISTRATION_CONFIRMED',   -- заявка принята
    'REGISTRATION_REJECTED',    -- заявка отклонена
    'EVENT_UNPUBLISHED',        -- событие снято с публикации
    'EVENT_CANCELLED',          -- событие отменено организатором
    'WAITLIST_PROMOTED',        -- продвинут из листа ожидания
    'NEW_REGISTRATION'          -- организатору: новая заявка на его событие
);

CREATE TABLE notifications (
    id            BIGSERIAL PRIMARY KEY,
    recipient_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    type          notification_type NOT NULL,
    title         VARCHAR(200)      NOT NULL,
    body          TEXT,

    -- Ссылка на связанный объект (открыть событие одним кликом)
    event_id      BIGINT REFERENCES events(id) ON DELETE SET NULL,

    is_read       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Колокольчик: непрочитанные уведомления пользователя
CREATE INDEX idx_notif_recipient ON notifications (recipient_id, is_read, created_at DESC);

COMMENT ON TABLE notifications IS 'Внутренние уведомления, отображаются в колокольчике в шапке';