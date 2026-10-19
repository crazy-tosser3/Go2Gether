-- Роли пользователей в системе
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,

    -- Основные данные
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,     -- BCrypt, никогда не храним пароль открытым текстом
    name          VARCHAR(100) NOT NULL,     -- отображаемое имя
    role          user_role    NOT NULL DEFAULT 'USER',

    -- Профиль (необязательные поля)
    about         TEXT,                      -- «о себе»
    city          VARCHAR(100),
    avatar_url    VARCHAR(500),

    -- Уровень подготовки (для будущего «подойдёт новичкам»)
    -- BEGINNER = новичок, AMATEUR = любитель, PRO = профи
    fitness_level VARCHAR(20) DEFAULT 'BEGINNER'
                  CHECK (fitness_level IN ('BEGINNER', 'AMATEUR', 'PRO')),

    -- Служебные
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,   -- деактивация без удаления
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Email уникален: два человека не могут зарегистрироваться с одним email
CREATE UNIQUE INDEX uq_users_email ON users (LOWER(email));

-- Поиск по имени (для будущего поиска организаторов)
CREATE INDEX idx_users_name ON users (LOWER(name));

-- Комментарий к таблице (хорошая практика, помогает разобраться через неделю)
COMMENT ON TABLE  users              IS 'Все пользователи системы включая администраторов';
COMMENT ON COLUMN users.role         IS 'USER — обычный пользователь, ADMIN — администратор платформы';
COMMENT ON COLUMN users.fitness_level IS 'Уровень подготовки: отображается на профиле';