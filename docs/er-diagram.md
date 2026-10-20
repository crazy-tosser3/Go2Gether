# Модель данных (ER)

Схема восстановлена по Flyway-миграциям `src/main/resources/db/migration/V01`–`V17` (состояние после `V17`). СУБД — PostgreSQL. В JPA-сущностях внешние ключи хранятся как числовые поля (`organizerUserId`, `sportId`, …) без `@ManyToOne`, поэтому связи ниже задаются только ограничениями в БД.

```mermaid
erDiagram
    users ||--o{ organizations : "owner_id (RESTRICT)"
    users ||--o{ events : "organizer_user_id (RESTRICT)"
    organizations ||--o{ events : "organizer_org_id (RESTRICT)"
    sports ||--o{ events : "sport_id (RESTRICT)"
    events ||--o{ registrations : "event_id (CASCADE)"
    users ||--o{ registrations : "user_id (CASCADE)"
    events ||--o{ comments : "event_id (CASCADE)"
    users ||--o{ comments : "author_id (CASCADE)"
    comments ||--o{ comments : "parent_id (SET NULL)"
    organizations ||--o{ comments : "as_organization_id (SET NULL)"
    users ||--o{ notifications : "recipient_id (CASCADE)"
    events ||--o{ notifications : "event_id (SET NULL)"
    users ||--o{ audit_log : "admin_id (RESTRICT)"
    organizations ||--o{ audit_log : "target_org_id (SET NULL)"
    events ||--o{ audit_log : "target_event_id (SET NULL)"
    comments ||--o{ audit_log : "target_comment_id (SET NULL)"
    users ||--o{ user_sports : "CASCADE"
    sports ||--o{ user_sports : "CASCADE"
    users ||--o{ user_sport_levels : "CASCADE"
    sports ||--o{ user_sport_levels : "CASCADE"
    users ||--o{ user_preferred_times : "CASCADE"
    users ||--o{ sports : "created_by (SET NULL)"
    sports ||--o{ safety_banners : "sport_id (SET NULL)"
    safety_banners ||--o{ safety_slides : "banner_id (CASCADE)"

    users {
        bigint id PK
        varchar email "UNIQUE LOWER(email)"
        varchar password_hash "Argon2id или BCrypt"
        varchar name
        user_role role "USER | ADMIN"
        varchar fitness_level "BEGINNER | AMATEUR | PRO"
        boolean is_active "мягкое удаление"
        boolean is_demo "вход без пароля"
        smallint birth_year "1920..2100"
        smallint radius_km "1..50"
        timestamptz consent_at
    }
    sports {
        bigint id PK
        varchar name
        varchar normalized_name "UNIQUE"
        varchar category "TEAM | INDIVIDUAL | MIND | FITNESS | OTHER"
        boolean is_custom
    }
    organizations {
        bigint id PK
        bigint owner_id FK
        varchar name
        varchar type "CLUB | COMMUNITY | FEDERATION | COMPANY"
        text contacts
        org_status status "PENDING | APPROVED | REJECTED"
        text reject_reason
        bigint decided_by FK
    }
    events {
        bigint id PK
        bigint organizer_user_id FK "ровно один из двух (CHECK)"
        bigint organizer_org_id FK
        bigint sport_id FK
        varchar title
        timestamptz starts_at
        timestamptz ends_at
        varchar address
        decimal latitude
        decimal longitude
        join_mode join_mode "OPEN | APPROVAL"
        int capacity "NULL = без лимита, > 0"
        varchar level
        event_status status "PUBLISHED | UNPUBLISHED | DELETED | CANCELLED"
        text moderation_reason
        text cancel_reason
        bigint version "@Version"
    }
    registrations {
        bigint id PK
        bigint event_id FK "UNIQUE(event_id, user_id)"
        bigint user_id FK
        registration_status status "PENDING | CONFIRMED | REJECTED | CANCELLED | WAITLIST"
        text message
        text decision_reason
        int waitlist_position
    }
    comments {
        bigint id PK
        bigint event_id FK
        bigint author_id FK
        bigint parent_id FK "один уровень вложенности"
        bigint as_organization_id FK
        text text "CHECK 1..2000"
        boolean is_hidden
        text hidden_reason
    }
    notifications {
        bigint id PK
        bigint recipient_id FK
        notification_type type
        varchar title
        bigint event_id FK
        boolean is_read
    }
    audit_log {
        bigint id PK
        bigint admin_id FK
        audit_action action
        text reason
    }
    user_sports {
        bigint user_id PK
        bigint sport_id PK
    }
    user_sport_levels {
        bigint user_id PK
        bigint sport_id PK
        varchar level
    }
    user_preferred_times {
        bigint user_id PK
        varchar time_slot PK "MORNING | DAY | EVENING | WEEKEND"
    }
    safety_banners {
        bigint id PK
        varchar category "FIRST_AID | SAFETY_RULES | EQUIPMENT | EMERGENCY"
        bigint sport_id FK
        boolean is_published
    }
    safety_slides {
        bigint id PK
        bigint banner_id FK "UNIQUE(banner_id, position)"
        int position
        text body
    }
```

## Ключевые ограничения

| Ограничение | Где | Что гарантирует |
|---|---|---|
| `chk_one_organizer` | `events` (V04) | Организатор — либо пользователь, либо организация, не оба и не ни одного |
| `uq_registration (event_id, user_id)` | `registrations` (V05) | Одна запись пользователя на мероприятие; повторная заявка переиспользует строку |
| `uq_sports_normalized` | `sports` (V02) | Нет дублей направлений с разным регистром и пробелами |
| `uq_users_email ON LOWER(email)` | `users` (V01) | Email уникален без учёта регистра |
| `CHECK (capacity > 0)` | `events` | Лимит мест положительный или `NULL` |
| `CHECK (LENGTH(text) BETWEEN 1 AND 2000)` | `comments` | Нет пустых и слишком длинных сообщений |
| `ON DELETE RESTRICT` для владельца организации и организатора | `organizations`, `events` | Пользователя нельзя физически удалить, пока у него есть организации или мероприятия; приложение использует мягкое удаление (`is_active = false`) |
| `version` + `SELECT … FOR UPDATE` | `events` | Параллельные записи не превышают `capacity` (`EventRepository.findByIdForUpdate`) |

Таблица `registrations` в коде представлена сущностью `Participation` (`Models/Participation.java`).

Роль «представитель организации» отдельно не хранится: это любой `USER`, у которого есть строка в `organizations` с его `owner_id`. Права организации появляются, только когда её `status = APPROVED`.
