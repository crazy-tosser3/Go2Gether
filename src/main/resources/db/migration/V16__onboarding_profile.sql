ALTER TABLE users ADD COLUMN birth_year         SMALLINT CHECK (birth_year BETWEEN 1920 AND 2100);
ALTER TABLE users ADD COLUMN radius_km          SMALLINT CHECK (radius_km BETWEEN 1 AND 50);
ALTER TABLE users ADD COLUMN preferred_age_from SMALLINT CHECK (preferred_age_from BETWEEN 10 AND 99);
ALTER TABLE users ADD COLUMN preferred_age_to   SMALLINT CHECK (preferred_age_to BETWEEN 10 AND 99);
ALTER TABLE users ADD CONSTRAINT chk_users_preferred_age
    CHECK (preferred_age_from IS NULL OR preferred_age_to IS NULL OR preferred_age_from <= preferred_age_to);

CREATE TABLE user_sport_levels (
    user_id  BIGINT      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    sport_id BIGINT      NOT NULL REFERENCES sports(id) ON DELETE CASCADE,
    level    VARCHAR(20) NOT NULL CHECK (level IN ('BEGINNER', 'AMATEUR', 'PRO')),
    PRIMARY KEY (user_id, sport_id)
);

CREATE TABLE user_preferred_times (
    user_id   BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    time_slot VARCHAR(10) NOT NULL CHECK (time_slot IN ('MORNING', 'DAY', 'EVENING', 'WEEKEND')),
    PRIMARY KEY (user_id, time_slot)
);
