-- =====================================================
-- ПОЛЬЗОВАТЕЛИ (пароль у всех: Demo1234!)
-- BCrypt хэш от «Demo1234!» (можно сгенерировать один раз)
-- =====================================================
INSERT INTO users (email, password_hash, name, role, about, city, fitness_level) VALUES
('user@demo.local',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Алексей Петров', 'USER',
 'Люблю футбол и утренние пробежки', 'Красноярск', 'AMATEUR'),

('user2@demo.local',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Мария Иванова', 'USER',
 'Шахматы и йога — мой выбор', 'Красноярск', 'BEGINNER'),

('org@demo.local',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Дмитрий Соколов', 'USER',
 'Представитель спортивных клубов', 'Красноярск', 'PRO'),

('org2@demo.local',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Елена Козлова', 'USER',
 'Организатор шахматного кружка', 'Красноярск', 'AMATEUR'),

('admin@demo.local',
 '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Администратор', 'ADMIN',
 NULL, 'Красноярск', 'BEGINNER');

-- =====================================================
-- ВИДЫ СПОРТА
-- =====================================================
INSERT INTO sports (name, normalized_name, is_custom) VALUES
('Футбол',      'футбол',      FALSE),
('Шахматы',     'шахматы',     FALSE),
('Бег',         'бег',         FALSE),
('Йога',        'йога',        FALSE),
('Баскетбол',   'баскетбол',   FALSE),
('Зарядка',     'зарядка',     FALSE),
-- Нишевый вид спорта для демонстрации универсальности
('Спортивное ориентирование', 'спортивное ориентирование', FALSE);

-- =====================================================
-- ОРГАНИЗАЦИИ
-- Три состояния: PENDING, APPROVED, REJECTED
-- =====================================================
INSERT INTO organizations
    (owner_id, name, description, sports_description, city, status, reject_reason, decided_by, decided_at)
VALUES
-- APPROVED: может публиковать события
((SELECT id FROM users WHERE email='org@demo.local'),
 'ФК «Дворовые легенды»',
 'Любительский футбольный клуб. Играем каждую субботу, берём всех!',
 'Футбол, мини-футбол',
 'Красноярск', 'APPROVED', NULL,
 (SELECT id FROM users WHERE email='admin@demo.local'),
 NOW() - INTERVAL '5 days'),

-- PENDING: ждёт решения администратора (эксперты проверят этот сценарий)
((SELECT id FROM users WHERE email='org2@demo.local'),
 'Шахматный кружок «Гамбит»',
 'Собираемся по средам в кафе. Приходите, научим!',
 'Шахматы, шашки',
 'Красноярск', 'PENDING', NULL, NULL, NULL),

-- REJECTED: отклонена (с причиной)
((SELECT id FROM users WHERE email='org2@demo.local'),
 'Беговой клуб «Ультра»',
 'Профессиональные забеги',
 'Бег',
 'Красноярск', 'REJECTED',
 'Недостаточно информации о деятельности организации. Уточните программу мероприятий.',
 (SELECT id FROM users WHERE email='admin@demo.local'),
 NOW() - INTERVAL '2 days');

-- =====================================================
-- СОБЫТИЯ
-- =====================================================
INSERT INTO events
    (organizer_user_id, organizer_org_id, title, description,
     sport_id, starts_at, address, join_mode, capacity, requirements, status)
VALUES

-- 1. Открытое событие от пользователя (свободная запись, нет лимита)
((SELECT id FROM users WHERE email='user@demo.local'), NULL,
 'Утренняя пробежка в парке',
 'Бегаем в лёгком темпе, 5 км. Подходит для новичков!',
 (SELECT id FROM sports WHERE normalized_name='бег'),
 NOW() + INTERVAL '2 days' + TIME '08:00:00',
 'Красноярск, Центральный парк, главный вход',
 'OPEN', NULL, 'Удобная спортивная обувь', 'PUBLISHED'),

-- 2. Событие с заявкой от организации
(NULL, (SELECT id FROM organizations WHERE name='ФК «Дворовые легенды»'),
 'Товарищеский матч по футболу',
 'Ищем команду соперника. 11 на 11, 2 тайма по 45 минут.',
 (SELECT id FROM sports WHERE normalized_name='футбол'),
 NOW() + INTERVAL '3 days' + TIME '19:00:00',
 'Красноярск, стадион «Север», ул. Лесная 20',
 'APPROVAL', 22,
 'Нужны бутсы или кроссовки. Форма предоставляется.',
 'PUBLISHED'),

-- 3. Йога для всех (открытая запись, лимит мест)
((SELECT id FROM users WHERE email='user2@demo.local'), NULL,
 'Утренняя йога на свежем воздухе',
 'Занятие для начинающих. Берите коврик и хорошее настроение!',
 (SELECT id FROM sports WHERE normalized_name='йога'),
 NOW() + INTERVAL '1 day' + TIME '07:30:00',
 'Красноярск, Набережная, рядом с фонтаном',
 'OPEN', 15, NULL, 'PUBLISHED'),

-- 4. Событие СНЯТОЕ С ПУБЛИКАЦИИ (для проверки сценария 6)
((SELECT id FROM users WHERE email='user@demo.local'), NULL,
 'Баскетбол 3×3',
 'Играем во дворе, нужны ещё двое.',
 (SELECT id FROM sports WHERE normalized_name='баскетбол'),
 NOW() + INTERVAL '4 days' + TIME '18:00:00',
 'Красноярск, ул. Мира 10, дворовая площадка',
 'OPEN', 6, NULL,
 'UNPUBLISHED'),

-- 5. Нишевый вид спорта (демонстрация универсальности)
((SELECT id FROM users WHERE email='user2@demo.local'), NULL,
 'Ночное ориентирование в лесу',
 'Для опытных участников. Карты и компас предоставляются.',
 (SELECT id FROM sports WHERE normalized_name='спортивное ориентирование'),
 NOW() + INTERVAL '5 days' + TIME '21:00:00',
 'Красноярск, Торгашинский хребет, поляна старт',
 'APPROVAL', 20,
 'Тёплая одежда, фонарик, физическая подготовка.',
 'PUBLISHED');

-- Сохраняем ID снятого события и причину модерации
UPDATE events
SET    moderation_reason = 'Нарушение правил размещения: отсутствует контактная информация',
       moderated_by      = (SELECT id FROM users WHERE email='admin@demo.local'),
       moderated_at      = NOW() - INTERVAL '1 hour'
WHERE  title = 'Баскетбол 3×3';

-- =====================================================
-- ЗАПИСИ НА СОБЫТИЯ
-- Все три статуса для демонстрации экспертам
-- =====================================================

-- user2 записался на пробежку (OPEN → сразу CONFIRMED)
INSERT INTO registrations (event_id, user_id, status) VALUES
((SELECT id FROM events  WHERE title='Утренняя пробежка в парке'),
 (SELECT id FROM users   WHERE email='user2@demo.local'),
 'CONFIRMED');

-- user подал заявку на футбол (APPROVAL → PENDING)
INSERT INTO registrations (event_id, user_id, status, message) VALUES
((SELECT id FROM events WHERE title='Товарищеский матч по футболу'),
 (SELECT id FROM users  WHERE email='user@demo.local'),
 'PENDING',
 'Играю в полузащите, опыт 3 года');

-- user2 подал заявку на футбол → REJECTED (для проверки отклонения)
INSERT INTO registrations (event_id, user_id, status, message, decided_at) VALUES
((SELECT id FROM events WHERE title='Товарищеский матч по футболу'),
 (SELECT id FROM users  WHERE email='user2@demo.local'),
 'REJECTED',
 'Хочу попробовать, я новичок',
 NOW() - INTERVAL '30 minutes');

-- =====================================================
-- КОММЕНТАРИИ
-- =====================================================
INSERT INTO comments (event_id, author_id, text) VALUES
((SELECT id FROM events WHERE title='Утренняя пробежка в парке'),
 (SELECT id FROM users  WHERE email='user2@demo.local'),
 'Можно прийти с собакой?'),

((SELECT id FROM events WHERE title='Утренняя пробежка в парке'),
 (SELECT id FROM users  WHERE email='user@demo.local'),
 'Конечно! Главное чтобы собака не сильно обгоняла 😄');

INSERT INTO comments (event_id, author_id, text) VALUES
((SELECT id FROM events WHERE title='Товарищеский матч по футболу'),
 (SELECT id FROM users  WHERE email='user@demo.local'),
 'Будут ли запасные игроки на скамейке?');

-- =====================================================
-- УВЕДОМЛЕНИЯ (демо)
-- =====================================================
INSERT INTO notifications (recipient_id, type, title, body, event_id) VALUES
((SELECT id FROM users WHERE email='user2@demo.local'),
 'REGISTRATION_REJECTED',
 'Заявка отклонена',
 'Ваша заявка на «Товарищеский матч по футболу» отклонена организатором.',
 (SELECT id FROM events WHERE title='Товарищеский матч по футболу')),

((SELECT id FROM users WHERE email='user@demo.local'),
 'EVENT_UNPUBLISHED',
 'Событие снято с публикации',
 'Ваше событие «Баскетбол 3×3» снято с публикации. Причина: нарушение правил.',
 (SELECT id FROM events WHERE title='Баскетбол 3×3'));