-- Статус записи участника
CREATE TYPE registration_status AS ENUM (
    'CONFIRMED',  -- участник подтверждён (OPEN режим или принятая заявка)
    'PENDING',    -- заявка ожидает решения (APPROVAL режим)
    'REJECTED',   -- заявка отклонена организатором
    'CANCELLED',  -- пользователь сам отменил
    'WAITLIST'    -- лист ожидания (мест нет, но пользователь в очереди)
);

CREATE TABLE registrations (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT NOT NULL REFERENCES events(id)  ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    -- CASCADE: при удалении события/пользователя записи тоже удаляются

    status      registration_status NOT NULL DEFAULT 'PENDING',

    -- Сообщение при подаче заявки («у меня опыт 2 года»)
    message     TEXT,

    -- Когда организатор принял/отклонил
    decided_at  TIMESTAMPTZ,

    -- Позиция в листе ожидания (NULL если не в листе)
    waitlist_position INT,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- САМЫЙ ВАЖНЫЙ индекс: один человек — одна запись на событие
-- ON CONFLICT (event_id, user_id) DO NOTHING защитит от двойного клика
CREATE UNIQUE INDEX uq_registration ON registrations (event_id, user_id);

-- Организатор смотрит список участников своего события
CREATE INDEX idx_reg_event  ON registrations (event_id, status);

-- «Мои записи» пользователя
CREATE INDEX idx_reg_user   ON registrations (user_id, status, created_at DESC);

-- Лист ожидания: нужно знать кто первый
CREATE INDEX idx_reg_waitlist ON registrations (event_id, waitlist_position)
    WHERE status = 'WAITLIST';

COMMENT ON COLUMN registrations.waitlist_position IS 'Порядок в листе ожидания, 1 = первый на очереди';