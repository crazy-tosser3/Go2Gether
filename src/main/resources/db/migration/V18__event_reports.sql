-- Жалобы пользователей на мероприятия
CREATE TYPE report_status AS ENUM (
    'PENDING',   -- ждёт решения администратора
    'ACCEPTED',  -- подтверждена: мероприятие снято или удалено
    'REJECTED'   -- отклонена администратором
);

CREATE TABLE event_reports (
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    reporter_id     BIGINT NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    reason          VARCHAR(20) NOT NULL
                    CHECK (reason IN ('SPAM', 'FRAUD', 'OFFENSIVE', 'DANGEROUS', 'MISLEADING', 'OTHER')),
    comment         TEXT CHECK (comment IS NULL OR LENGTH(comment) <= 1000),
    status          report_status NOT NULL DEFAULT 'PENDING',
    resolution_note TEXT,          -- причина решения, видна автору жалобы
    resolved_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    resolved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Одна нерассмотренная жалоба пользователя на мероприятие; после решения можно пожаловаться снова
CREATE UNIQUE INDEX uq_event_reports_pending ON event_reports (event_id, reporter_id)
    WHERE status = 'PENDING';
-- Очередь модерации
CREATE INDEX idx_event_reports_queue ON event_reports (status, created_at);
-- «Мои жалобы»
CREATE INDEX idx_event_reports_reporter ON event_reports (reporter_id, created_at DESC);

ALTER TYPE audit_action      ADD VALUE IF NOT EXISTS 'REPORT_REJECTED';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'REPORT_RESOLVED';

COMMENT ON TABLE event_reports IS 'Жалобы на мероприятия; принятие жалобы снимает мероприятие с публикации';

-- Демо: жалоба user@demo.local на «Зарядку во дворе» ждёт решения администратора
INSERT INTO event_reports (event_id, reporter_id, reason, comment)
SELECT e.id, u.id, 'MISLEADING', 'Во дворе идёт стройка, адрес неактуален'
FROM events e, users u
WHERE e.title = 'Зарядка во дворе' AND u.email = 'user@demo.local';
