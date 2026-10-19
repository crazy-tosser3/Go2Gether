CREATE TABLE comments (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT NOT NULL REFERENCES events(id)  ON DELETE CASCADE,
    author_id   BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,

    text        TEXT    NOT NULL CHECK (LENGTH(text) BETWEEN 1 AND 2000),
    -- Защита: минимум 1 символ, максимум 2000 (защита от флуда огромными сообщениями)

    -- Ответ на другой комментарий (только один уровень вложенности)
    parent_id   BIGINT  REFERENCES comments(id) ON DELETE SET NULL,

    -- Модерация
    is_hidden   BOOLEAN NOT NULL DEFAULT FALSE,   -- скрыт администратором
    hidden_by   BIGINT  REFERENCES users(id)      ON DELETE SET NULL,
    hidden_at   TIMESTAMPTZ,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
    -- updated_at нет намеренно: комментарии не редактируются (упрощение)
);

-- Загрузка обсуждения события
CREATE INDEX idx_comments_event ON comments (event_id, created_at ASC)
    WHERE is_hidden = FALSE;

-- Ответы на конкретный комментарий
CREATE INDEX idx_comments_parent ON comments (parent_id)
    WHERE parent_id IS NOT NULL;

COMMENT ON COLUMN comments.is_hidden IS 'Скрыт администратором, событие и остальные комментарии остаются';
COMMENT ON COLUMN comments.parent_id IS 'Только один уровень вложенности (ответ на комментарий)';