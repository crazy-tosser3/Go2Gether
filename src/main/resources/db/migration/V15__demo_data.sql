CREATE FUNCTION pg_temp.krs(days INT, at_time TIME) RETURNS TIMESTAMPTZ
LANGUAGE sql AS $$
    SELECT (date_trunc('day', NOW() AT TIME ZONE 'Asia/Krasnoyarsk')
            + days * INTERVAL '1 day' + at_time) AT TIME ZONE 'Asia/Krasnoyarsk'
$$;


INSERT INTO sports (name, normalized_name, category, icon, is_custom) VALUES
('Волейбол',          'волейбол',          'TEAM',       '🏐', FALSE),
('Настольный теннис', 'настольный теннис', 'INDIVIDUAL', '🏓', FALSE),
('Городки',           'городки',           'OTHER',      '🪵', FALSE)
ON CONFLICT (normalized_name) DO NOTHING;


UPDATE events SET latitude = 56.011500, longitude = 92.864000 WHERE title = 'Утренняя пробежка в парке';
UPDATE events SET latitude = 56.058000, longitude = 92.902000 WHERE title = 'Товарищеский матч по футболу';
UPDATE events SET latitude = 56.009000, longitude = 92.880000 WHERE title = 'Утренняя йога на свежем воздухе';
UPDATE events SET latitude = 56.011000, longitude = 92.895000 WHERE title = 'Баскетбол 3×3';
UPDATE events SET latitude = 55.975000, longitude = 92.960000 WHERE title = 'Ночное ориентирование в лесу';


INSERT INTO events
    (organizer_user_id, organizer_org_id, title, description, sport_id,
     starts_at, ends_at, address, latitude, longitude, city, level,
     join_mode, capacity, requirements, equipment, status)
VALUES

((SELECT id FROM users WHERE email = 'org2@demo.local'), NULL,
 'Шахматный блиц в сквере',
 'Партии по 5 минут, играем по швейцарской системе. Приходите с друзьями, доски на всех хватит.',
 (SELECT id FROM sports WHERE normalized_name = 'шахматы'),
 pg_temp.krs(1, '18:00'), pg_temp.krs(1, '20:30'),
 'Красноярск, сквер Сурикова', 56.012500, 92.874500, 'Красноярск', 'BEGINNER',
 'OPEN', 16, NULL, 'Доски и часы предоставим', 'PUBLISHED'),

((SELECT id FROM users WHERE email = 'user@demo.local'), NULL,
 'Настольный теннис на Татышеве',
 'Играем на уличных столах, на вылет. Нужен средний уровень — умеете подавать и держать розыгрыш.',
 (SELECT id FROM sports WHERE normalized_name = 'настольный теннис'),
 pg_temp.krs(2, '17:00'), pg_temp.krs(2, '19:00'),
 'Красноярск, остров Татышев, теннисные столы у центрального входа', 56.031000, 92.939000, 'Красноярск', 'AMATEUR',
 'APPROVAL', 8, 'Своя ракетка', 'Мячи есть у организатора', 'PUBLISHED'),

((SELECT id FROM users WHERE email = 'user2@demo.local'), NULL,
 'Зарядка во дворе',
 'Полчаса лёгкой разминки перед работой: суставная гимнастика, растяжка, немного кардио. Подходит всем.',
 (SELECT id FROM sports WHERE normalized_name = 'зарядка'),
 pg_temp.krs(1, '07:30'), pg_temp.krs(1, '08:00'),
 'Красноярск, ул. Ленина 113, двор', 56.014000, 92.865000, 'Красноярск', 'BEGINNER',
 'OPEN', NULL, NULL, 'Коврик по желанию', 'PUBLISHED'),

((SELECT id FROM users WHERE email = 'org@demo.local'), NULL,
 'Пляжный волейбол на Острове Отдыха',
 'Собираем две команды 6×6. Играем до двух побед, потом меняемся составами.',
 (SELECT id FROM sports WHERE normalized_name = 'волейбол'),
 pg_temp.krs(3, '16:00'), pg_temp.krs(3, '18:30'),
 'Красноярск, Остров Отдыха, пляжные площадки', 56.001000, 92.910000, 'Красноярск', 'AMATEUR',
 'APPROVAL', 12, 'Умение принимать подачу', 'Мяч и сетка есть', 'PUBLISHED'),

((SELECT id FROM users WHERE email = 'org2@demo.local'), NULL,
 'Городки: турнир для новичков',
 'Старинная русская игра: выбиваем фигуры битой. Объясним правила с нуля и сыграем небольшой турнир.',
 (SELECT id FROM sports WHERE normalized_name = 'городки'),
 pg_temp.krs(4, '12:00'), pg_temp.krs(4, '15:00'),
 'Красноярск, остров Татышев, городошная площадка', 56.029000, 92.930000, 'Красноярск', 'BEGINNER',
 'APPROVAL', 10, NULL, 'Биты и фигуры предоставим', 'PUBLISHED'),

(NULL, (SELECT id FROM organizations WHERE name = 'ФК «Дворовые легенды»'),
 'Мини-футбол 5×5 вечером',
 'Тренировочная игра клуба, открыта для всех желающих. Делимся на команды на месте.',
 (SELECT id FROM sports WHERE normalized_name = 'футбол'),
 pg_temp.krs(2, '20:00'), pg_temp.krs(2, '21:30'),
 'Красноярск, ул. Лесная 20, малое поле', 56.057000, 92.900000, 'Красноярск', 'AMATEUR',
 'OPEN', 10, 'Сменная обувь', 'Манишки предоставим', 'PUBLISHED'),

((SELECT id FROM users WHERE email = 'user@demo.local'), NULL,
 'Баскетбол 3×3 на набережной',
 'Стритбол до 21 очка. Собираем 4 команды по 3 человека.',
 (SELECT id FROM sports WHERE normalized_name = 'баскетбол'),
 pg_temp.krs(5, '18:00'), pg_temp.krs(5, '20:00'),
 'Красноярск, набережная Енисея, баскетбольная площадка', 56.008000, 92.873000, 'Красноярск', 'AMATEUR',
 'OPEN', 12, NULL, 'Мяч принесу', 'PUBLISHED');


INSERT INTO registrations (event_id, user_id, status, message, decided_at) VALUES
((SELECT id FROM events WHERE title = 'Шахматный блиц в сквере'),
 (SELECT id FROM users  WHERE email = 'user2@demo.local'), 'CONFIRMED', NULL, NULL),
((SELECT id FROM events WHERE title = 'Шахматный блиц в сквере'),
 (SELECT id FROM users  WHERE email = 'user@demo.local'),  'CONFIRMED', NULL, NULL),
((SELECT id FROM events WHERE title = 'Настольный теннис на Татышеве'),
 (SELECT id FROM users  WHERE email = 'org@demo.local'),   'CONFIRMED', 'Играю пару раз в неделю', NOW() - INTERVAL '3 hours'),
((SELECT id FROM events WHERE title = 'Настольный теннис на Татышеве'),
 (SELECT id FROM users  WHERE email = 'user2@demo.local'), 'PENDING',   'Играла в университете, хочу вспомнить', NULL),
((SELECT id FROM events WHERE title = 'Зарядка во дворе'),
 (SELECT id FROM users  WHERE email = 'org2@demo.local'),  'CONFIRMED', NULL, NULL),
((SELECT id FROM events WHERE title = 'Пляжный волейбол на Острове Отдыха'),
 (SELECT id FROM users  WHERE email = 'org2@demo.local'),  'CONFIRMED', 'Играю в любительской лиге', NOW() - INTERVAL '1 day'),
((SELECT id FROM events WHERE title = 'Пляжный волейбол на Острове Отдыха'),
 (SELECT id FROM users  WHERE email = 'user@demo.local'),  'PENDING',   'Играл в школьной команде, могу на любую позицию', NULL),
((SELECT id FROM events WHERE title = 'Мини-футбол 5×5 вечером'),
 (SELECT id FROM users  WHERE email = 'user@demo.local'),  'CONFIRMED', NULL, NULL),
((SELECT id FROM events WHERE title = 'Мини-футбол 5×5 вечером'),
 (SELECT id FROM users  WHERE email = 'user2@demo.local'), 'CONFIRMED', NULL, NULL)
ON CONFLICT (event_id, user_id) DO NOTHING;


INSERT INTO comments (event_id, author_id, text) VALUES
((SELECT id FROM events WHERE title = 'Городки: турнир для новичков'),
 (SELECT id FROM users  WHERE email = 'user2@demo.local'),
 'Никогда не играла в городки — возьмёте новичка? Нужно что-то своё?');

INSERT INTO comments (event_id, author_id, parent_id, text) VALUES
((SELECT id FROM events   WHERE title = 'Городки: турнир для новичков'),
 (SELECT id FROM users    WHERE email = 'org2@demo.local'),
 (SELECT id FROM comments WHERE text LIKE 'Никогда не играла в городки%'),
 'Конечно! Турнир как раз для новичков. Биты и фигуры дадим, правила объясним на месте — нужна только удобная обувь.');

INSERT INTO comments (event_id, author_id, text) VALUES
((SELECT id FROM events WHERE title = 'Мини-футбол 5×5 вечером'),
 (SELECT id FROM users  WHERE email = 'user@demo.local'),
 'Если пойдёт дождь, игра состоится?');

INSERT INTO comments (event_id, author_id, as_organization_id, parent_id, text) VALUES
((SELECT id FROM events        WHERE title = 'Мини-футбол 5×5 вечером'),
 (SELECT id FROM users