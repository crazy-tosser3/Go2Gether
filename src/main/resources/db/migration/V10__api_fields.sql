-- Поля, которых не хватает для REST API из REST_API.drawio.
-- Существующие миграции V01–V09 не меняются.

-- Пользователи: демо-аккаунты для быстрого входа и интересы (направления)
ALTER TABLE users ADD COLUMN is_demo BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE users SET is_demo = TRUE WHERE email LIKE '%@demo.local';

CREATE TABLE user_sports (
    user_id  BIGINT NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    sport_id BIGINT NOT NULL REFERENCES sports(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, sport_id)
);

-- Направления: категория и иконка для каталога
ALTER TABLE sports ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'OTHER'
    CHECK (category IN ('TEAM', 'INDIVIDUAL', 'MIND', 'FITNESS', 'OTHER'));
ALTER TABLE sports ADD COLUMN icon VARCHAR(20);
UPDATE sports SET category = 'TEAM',       icon = '⚽' WHERE normalized_name = 'футбол';
UPDATE sports SET category = 'TEAM',       icon = '🏀' WHERE normalized_name = 'баскетбол';
UPDATE sports SET category = 'MIND',       icon = '♟'  WHERE normalized_name = 'шахматы';
UPDATE sports SET category = 'INDIVIDUAL', icon = '🏃' WHERE normalized_name = 'бег';
UPDATE sports SET category = 'FITNESS',    icon = '🧘' WHERE normalized_name = 'йога';
UPDATE sports SET category = 'FITNESS',    icon = '🤸' WHERE normalized_name = 'зарядка';
UPDATE sports SET category = 'INDIVIDUAL', icon = '🧭' WHERE normalized_name = 'спортивное ориентирование';

-- Организации: тип и контакты (контакты видят подтверждённые участники)
ALTER TABLE organizations ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'CLUB'
    CHECK (type IN ('CLUB', 'COMMUNITY', 'FEDERATION', 'COMPANY'));
ALTER TABLE organizations ADD COLUMN contacts TEXT;

-- События: город (фильтр ленты), уточнение места, уровень, инвентарь, причина отмены
ALTER TABLE events ADD COLUMN city          VARCHAR(100);
ALTER TABLE events ADD COLUMN location_note TEXT;
ALTER TABLE events ADD COLUMN level         VARCHAR(20)
    CHECK (level IN ('BEGINNER', 'AMATEUR', 'PRO'));
ALTER TABLE events ADD COLUMN equipment     TEXT;
ALTER TABLE events ADD COLUMN cancel_reason TEXT;
UPDATE events SET city = 'Красноярск' WHERE address LIKE 'Красноярск%';

-- Записи: причина отклонения видна пользователю
ALTER TABLE registrations ADD COLUMN decision_reason TEXT;

-- Комментарии: редактирование автором, ответ от имени организации, причина скрытия
ALTER TABLE comments ADD COLUMN updated_at         TIMESTAMPTZ;
ALTER TABLE comments ADD COLUMN as_organization_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL;
ALTER TABLE comments ADD COLUMN hidden_reason      TEXT;

-- Новые типы уведомлений и действий админа
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'ORG_APPROVED';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'ORG_REJECTED';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'EVENT_UPDATED';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'EVENT_DELETED';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'EVENT_RESTORED';
ALTER TYPE notification_type ADD VALUE IF NOT EXISTS 'COMMENT_REPLY';
ALTER TYPE audit_action ADD VALUE IF NOT EXISTS 'EVENT_RESTORED';
