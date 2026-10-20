WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sort_order)
    VALUES ('Ушибы и растяжения', 'Что делать в первые минуты', 'FIRST_AID', '#B3261E', 1)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, 'Остановите нагрузку', 'Сразу прекратите тренировку. Продолжать «через боль» — частая причина серьёзной травмы.'),
    (2, 'Холод', 'Приложите холод через ткань на 15–20 минут. Не кладите лёд прямо на кожу.'),
    (3, 'Покой и возвышение', 'Обеспечьте покой и держите повреждённую конечность приподнятой. В первые сутки не грейте и не растирайте место травмы.'),
    (4, 'Когда к врачу', 'Сильная боль, деформация, отёк, невозможность опереться на ногу — признаки возможного перелома. Не вправляйте, обездвижьте конечность и обратитесь за медпомощью.')
) AS s(position, title, body);

WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sort_order)
    VALUES ('Остановка кровотечения', 'Давление, повязка, вызов помощи', 'FIRST_AID', '#8C1D18', 2)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, 'Прямое давление', 'Плотно прижмите рану чистой салфеткой, бинтом или тканью и удерживайте.'),
    (2, 'Давящая повязка', 'Наложите тугую повязку поверх салфетки. Если промокла — не снимайте, бинтуйте сверху.'),
    (3, 'Сильное кровотечение', 'Если кровь бьёт струёй и давление не помогает — вызывайте 112. Жгут накладывают выше раны; обязательно запишите время наложения.')
) AS s(position, title, body);

WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sort_order)
    VALUES ('Сердечно-лёгочная реанимация', 'Если человек не дышит', 'FIRST_AID', '#601410', 3)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, 'Безопасность и сознание', 'Убедитесь, что вам ничего не угрожает. Окликните человека, аккуратно потормошите за плечи.'),
    (2, 'Дыхание', 'Запрокиньте голову, приподняв подбородок, и в течение 10 секунд проверьте дыхание: слушайте, смотрите на грудь.'),
    (3, 'Вызовите 112', 'Если дыхания нет — звоните 112 или попросите кого-то позвонить и найти дефибриллятор.'),
    (4, 'Компрессии', '30 надавливаний на центр грудной клетки на глубину 5–6 см с частотой 100–120 в минуту, затем 2 вдоха, если умеете. Продолжайте до приезда бригады.')
) AS s(position, title, body);

WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sort_order)
    VALUES ('Разминка перед тренировкой', 'Как не травмироваться', 'SAFETY_RULES', '#1F6F43', 4)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, '10–15 минут', 'Начните с лёгкого кардио: ходьба, бег трусцой, прыжки — чтобы разогреть мышцы.'),
    (2, 'Суставы', 'Вращения шеей, плечами, тазом, коленями и стопами в обе стороны.'),
    (3, 'Динамическая растяжка', 'Выпады, махи ногами и руками. Статичную растяжку лучше оставить на заминку.'),
    (4, 'Самочувствие', 'Плохо себя чувствуете, кружится голова, болит сердце — тренировку лучше отменить.')
) AS s(position, title, body);

WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sport_id, sort_order)
    VALUES ('Безопасная пробежка', 'Город, темнота, дорога', 'SAFETY_RULES', '#2B4C7E',
            (SELECT id FROM sports WHERE normalized_name = 'бег'), 5)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, 'Будьте заметны', 'В темноте — светоотражающие элементы на одежде и обуви.'),
    (2, 'Дорога', 'Где нет тротуара, двигайтесь по обочине навстречу транспорту.'),
    (3, 'Наушники', 'Сделайте музыку тише или используйте один наушник — нужно слышать машины и людей.'),
    (4, 'Маршрут', 'Сообщите близким, где бежите и когда вернётесь.')
) AS s(position, title, body);

WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sport_id, sort_order)
    VALUES ('Что взять с собой', 'Минимальный набор на выезд', 'EQUIPMENT', '#5B4B8A',
            (SELECT id FROM sports WHERE normalized_name = 'спортивное ориентирование'), 6)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, 'Вода', 'Возьмите воду, даже если тренировка короткая.'),
    (2, 'Аптечка', 'Бинт, пластырь, антисептик, стерильные салфетки, холодовой пакет.'),
    (3, 'Телефон', 'Заряженный телефон, по возможности — пауэрбанк.'),
    (4, 'Ориентирование', 'Свисток, карта, компас. Договоритесь о контрольном времени возвращения.')
) AS s(position, title, body);

WITH b AS (
    INSERT INTO safety_banners (title, subtitle, category, accent_color, sort_order)
    VALUES ('Экстренные номера', '112 — единый номер', 'EMERGENCY', '#C2410C', 7)
    RETURNING id
)
INSERT INTO safety_slides (banner_id, position, title, body)
SELECT b.id, s.position, s.title, s.body FROM b, (VALUES
    (1, 'Номера', '112 — единый номер экстренных служб. 103 — скорая, 101 — пожарные, 102 — полиция.'),
    (2, 'Что сказать', 'Адрес или ориентиры, что случилось, сколько пострадавших, их состояние.'),
    (3, 'Не кладите трубку', 'Дождитесь, пока диспетчер закончит разговор, и выполняйте его указания.')
) AS s(position, title, body);