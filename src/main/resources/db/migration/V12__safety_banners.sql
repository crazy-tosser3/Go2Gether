CREATE TABLE safety_banners (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(120) NOT NULL,
    subtitle     VARCHAR(255),
    category     VARCHAR(20)  NOT NULL
        CHECK (category IN ('FIRST_AID', 'SAFETY_RULES', 'EQUIPMENT', 'EMERGENCY')),
    cover_url    VARCHAR(500),
    accent_color VARCHAR(9),
    sport_id     BIGINT REFERENCES sports(id) ON DELETE SET NULL,
    sort_order   INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE safety_slides (
    id        BIGSERIAL PRIMARY KEY,
    banner_id BIGINT NOT NULL REFERENCES safety_banners(id) ON DELETE CASCADE,
    position  INT NOT NULL,
    title     VARCHAR(120),
    body      TEXT NOT NULL,
    image_url VARCHAR(500),
    UNIQUE (banner_id, position)
);

CREATE INDEX idx_safety_banners_feed ON safety_banners (sort_order) WHERE is_published;

COMMENT ON COLUMN safety_banners.sport_id     IS 'NULL = баннер для всех видов спорта';
COMMENT ON COLUMN safety_banners.accent_color IS 'Цвет карточки, если нет cover_url, например #B3261E';