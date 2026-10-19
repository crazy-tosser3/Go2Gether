CREATE TABLE sports (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,    -- «Футбол» (для отображения)
    normalized_name VARCHAR(100) NOT NULL,    -- «футбол» (для поиска дублей)
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,  -- TRUE = добавил пользователь
    created_by      BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ГЛАВНЫЙ индекс: именно по нему ищем дубли при find-or-create
-- Благодаря ему «Футбол», «футбол», «ФУТБОЛ » не создадут три разные записи
CREATE UNIQUE INDEX uq_sports_normalized ON sports (normalized_name);

-- Для автодополнения в форме создания события
CREATE INDEX idx_sports_name ON sports (LOWER(name));

COMMENT ON COLUMN sports.normalized_name IS 'trim().lowercase() от name, используется для поиска дублей';
COMMENT ON COLUMN sports.is_custom       IS 'TRUE если вид спорта добавил пользователь, а не команда';