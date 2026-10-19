CREATE TYPE audit_action AS ENUM (
    'ORG_APPROVED',
    'ORG_REJECTED',
    'EVENT_UNPUBLISHED',
    'EVENT_DELETED',
    'COMMENT_HIDDEN'
);

CREATE TABLE audit_log (
    id            BIGSERIAL PRIMARY KEY,
    admin_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action        audit_action NOT NULL,

    target_org_id      BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    target_event_id    BIGINT REFERENCES events(id)        ON DELETE SET NULL,
    target_comment_id  BIGINT REFERENCES comments(id)      ON DELETE SET NULL,

    reason        TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_admin  ON audit_log (admin_id, created_at DESC);
CREATE INDEX idx_audit_event  ON audit_log (target_event_id) WHERE target_event_id IS NOT NULL;

COMMENT ON TABLE audit_log IS 'Журнал действий администратора, только для чтения';