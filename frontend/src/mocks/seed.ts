import type {
  CommentRow,
  ReportRow,
  EventRow,
  OrganizationRow,
  ParticipationRow,
  SafetyBannerRow,
  SportRow,
  UserRow,
} from './db'

/** Идентификаторы демо-данных. Демо-аккаунты 1–5 совпадают с демо-аккаунтами бэка. */
export const U = { anna: 1, petr: 2, igor: 3, olga: 4, admin: 5, maria: 6, sergey: 7, nina: 8 }
export const O = { enisey: 1, ladya: 2, pulse: 3 }
export const SP = {
  football: 1,
  volleyball: 2,
  running: 3,
  cycling: 4,
  tabletennis: 5,
  chess: 6,
  workout: 7,
  yoga: 8,
  gorodki: 9,
}
export const E = {
  runMorning: 1,
  football: 2,
  chessPark: 3,
  workout: 4,
  volleyball: 5,
  runInterval: 6,
  tabletennis: 7,
  yoga: 8,
  cycling: 9,
  gorodki: 10,
}

// Демо-данные по сценариям кейса: разные виды спорта, оба режима записи,
// организации на рассмотрении, отклонённая и допущенная, заявки, снятое событие.
export function createSeed(now: Date) {
  const iso = (d: Date) => d.toISOString()
  const created = iso(new Date(now.getTime() - 7 * 24 * 3600 * 1000))

  // Время относительно «сейчас», чтобы события всегда были в будущем.
  const at = (days: number, hours: number, minutes = 0) => {
    const d = new Date(now)
    d.setDate(d.getDate() + days)
    d.setHours(hours, minutes, 0, 0)
    return iso(d)
  }

  const user = (
    id: number,
    name: string,
    email: string,
    extra: Partial<UserRow> = {},
  ): UserRow => ({
    id,
    name,
    role: 'user',
    email,
    avatar_url: null,
    city: 'Красноярск',
    bio: null,
    level: null,
    sport_ids: [],
    birth_year: null,
    radius_km: null,
    preferred_age_from: null,
    preferred_age_to: null,
    preferred_times: [],
    sport_levels: {},
    is_demo: false,
    created_at: created,
    password: 'Demo1234!',
    contacts: null,
    demo_description: null,
    ...extra,
  })

  const users: UserRow[] = [
    user(U.anna, 'Анна Смирнова', 'user@demo.local', {
      is_demo: true,
      demo_description: 'Ищет компанию и организует свои встречи',
      level: 'amateur',
      bio: 'Бегаю по утрам, ищу компанию для йоги и футбола.',
      sport_ids: [SP.running, SP.yoga],
      contacts: 'Телеграм: @anna_runs',
    }),
    user(U.petr, 'Пётр Иванов', 'user2@demo.local', {
      is_demo: true,
      demo_description: 'Записывается на чужие события',
      level: 'beginner',
      bio: 'Недавно переехал, хочу найти, с кем играть в волейбол.',
      sport_ids: [SP.volleyball, SP.football],
      contacts: '+7 900 000-00-02',
    }),
    user(U.igor, 'Игорь Белов', 'org@demo.local', {
      is_demo: true,
      demo_description: 'Владелец допущенной организации',
      level: 'pro',
      sport_ids: [SP.running],
      contacts: '+7 900 000-00-03',
    }),
    user(U.olga, 'Ольга Ким', 'org2@demo.local', {
      is_demo: true,
      demo_description: 'Владелец организаций на модерации и отклонённой',
      sport_ids: [SP.chess],
      contacts: '+7 900 000-00-04',
    }),
    user(U.admin, 'Администратор', 'admin@demo.local', {
      is_demo: true,
      demo_description: 'Допуск организаций и модерация',
      role: 'admin',
    }),
    user(U.maria, 'Мария Лебедева', 'maria@example.ru', { sport_ids: [SP.yoga] }),
    user(U.sergey, 'Сергей Орлов', 'sergey@example.ru', {
      level: 'amateur',
      sport_ids: [SP.chess, SP.tabletennis],
      contacts: '+7 900 000-00-07',
    }),
    user(U.nina, 'Нина Петровна', 'nina@example.ru', {
      level: 'beginner',
      bio: 'На пенсии, веду зарядку во дворе для соседей.',
      sport_ids: [SP.workout],
      contacts: '+7 900 000-00-08',
    }),
  ]

  const sport = (
    id: number,
    name: string,
    category: SportRow['category'],
    icon: string,
    extra: Partial<SportRow> = {},
  ): SportRow => ({
    id,
    name,
    category,
    icon,
    is_custom: false,
    normalized_name: name.toLowerCase().replace(/ё/g, 'е'),
    created_by: null,
    ...extra,
  })

  const sports: SportRow[] = [
    sport(SP.football, 'Футбол', 'team', '⚽'),
    sport(SP.volleyball, 'Волейбол', 'team', '🏐'),
    sport(SP.running, 'Бег', 'individual', '🏃'),
    sport(SP.cycling, 'Велоспорт', 'individual', '🚴'),
    sport(SP.tabletennis, 'Настольный теннис', 'individual', '🏓'),
    sport(SP.chess, 'Шахматы', 'mind', '♟️'),
    sport(SP.workout, 'Зарядка', 'fitness', '🤸'),
    sport(SP.yoga, 'Йога', 'fitness', '🧘'),
    sport(SP.gorodki, 'Городки', 'other', '🎯', { is_custom: true, created_by: U.sergey }),
  ]

  const org = (
    id: number,
    data: Pick<OrganizationRow, 'owner_id' | 'name' | 'type' | 'status'> & Partial<OrganizationRow>,
  ): OrganizationRow => ({
    description: null,
    sports_description: null,
    city: 'Красноярск',
    website_url: null,
    avatar_url: null,
    contacts: null,
    reject_reason: null,
    decided_at: data.status === 'pending' ? null : created,
    created_at: created,
    updated_at: created,
    id,
    ...data,
  })

  const organizations: OrganizationRow[] = [
    org(O.enisey, {
      owner_id: U.igor,
      name: 'Беговой клуб «Енисей»',
      description:
        'Любительский беговой клуб. Тренируемся на набережной и стадионе, готовим к первым 5 и 10 км.',
      type: 'club',
      sports_description: 'Бег, интервальные тренировки',
      contacts: 'Телеграм-канал @enisey_run',
      status: 'approved',
    }),
    org(O.ladya, {
      owner_id: U.olga,
      name: 'Шахматный клуб «Ладья»',
      description: 'Кружок для взрослых и школьников: еженедельные встречи и блиц-турниры.',
      type: 'community',
      sports_description: 'Шахматы',
      contacts: '+7 900 000-00-04',
      status: 'pending',
    }),
    org(O.pulse, {
      owner_id: U.olga,
      name: 'Фитнес-студия «Пульс»',
      description: 'Групповые занятия.',
      type: 'company',
      sports_description: 'Йога',
      city: null,
      contacts: 'pulse@example.ru',
      status: 'rejected',
      reject_reason: 'Добавьте описание занятий и контакты для связи.',
    }),
  ]

  const event = (
    id: number,
    data: Pick<
      EventRow,
      | 'title'
      | 'description'
      | 'sport_id'
      | 'organizer_user_id'
      | 'starts_at'
      | 'address'
      | 'join_mode'
    > &
      Partial<EventRow>,
  ): EventRow => ({
    organizer_type: 'user',
    organizer_org_id: null,
    ends_at: null,
    city: 'Красноярск',
    lat: null,
    lng: null,
    location_note: null,
    level: null,
    requirements: null,
    equipment: null,
    capacity: null,
    status: 'published',
    moderation_reason: null,
    created_at: created,
    updated_at: created,
    id,
    ...data,
  })

  const events: EventRow[] = [
    event(E.runMorning, {
      title: 'Утренняя пробежка по набережной',
      description:
        'Спокойный темп 6:30–7:00 на километр, 5 км. Подходит новичкам, никого не бросаем.',
      sport_id: SP.running,
      organizer_type: 'organization',
      organizer_user_id: U.igor,
      organizer_org_id: O.enisey,
      starts_at: at(1, 7, 30),
      ends_at: at(1, 8, 30),
      address: 'Красноярск, Центральная набережная, у Коммунального моста',
      lat: 56.0089,
      lng: 92.8705,
      location_note: 'Собираемся у часов, ищите оранжевый флажок клуба.',
      level: 'beginner',
      equipment: 'Беговая обувь, вода',
      join_mode: 'open',
    }),
    event(E.football, {
      title: 'Дворовый футбол 5×5',
      description: 'Играем на освещённой площадке, команды делим на месте. Нужны ещё 4 человека.',
      sport_id: SP.football,
      organizer_user_id: U.anna,
      starts_at: at(2, 19),
      ends_at: at(2, 21),
      address: 'Красноярск, ул. Ленина, 120, школьный стадион',
      lat: 56.0153,
      lng: 92.8622,
      level: 'amateur',
      requirements: 'Играли раньше хотя бы во дворе',
      equipment: 'Сороконожки или кеды, светлая и тёмная футболки',
      join_mode: 'approval',
      capacity: 10,
    }),
    event(E.chessPark, {
      title: 'Шахматы в парке',
      description:
        'Приносите доски, кто может. Играем партии по 15 минут, разбираем ошибки вместе.',
      sport_id: SP.chess,
      organizer_user_id: U.sergey,
      starts_at: at(3, 12),
      address: 'Красноярск, Центральный парк, беседка у фонтана',
      lat: 56.0105,
      lng: 92.878,
      join_mode: 'open',
    }),
    event(E.workout, {
      title: 'Зарядка во дворе для всех возрастов',
      description: 'Лёгкая разминка 30 минут под музыку. Можно приходить с детьми и внуками.',
      sport_id: SP.workout,
      organizer_user_id: U.nina,
      starts_at: at(1, 9),
      ends_at: at(1, 9, 30),
      address: 'Красноярск, ул. Весны, 7, детская площадка',
      lat: 56.0446,
      lng: 92.9013,
      level: 'beginner',
      equipment: 'Удобная одежда',
      join_mode: 'open',
    }),
    event(E.volleyball, {
      title: 'Пляжный волейбол',
      description: 'Ищу 3–5 человек для регулярных игр. Играем на песке, сетка есть.',
      sport_id: SP.volleyball,
      organizer_user_id: U.petr,
      starts_at: at(4, 18),
      ends_at: at(4, 20),
      address: 'Красноярск, остров Татышев, волейбольные площадки',
      lat: 56.0247,
      lng: 92.9301,
      level: 'amateur',
      requirements: 'Умеете принимать подачу',
      join_mode: 'approval',
      capacity: 6,
    }),
    event(E.runInterval, {
      title: 'Интервальная тренировка на стадионе',
      description: 'Отрезки 400 м с тренером клуба. Перед тренировкой обязательна разминка.',
      sport_id: SP.running,
      organizer_type: 'organization',
      organizer_user_id: U.igor,
      organizer_org_id: O.enisey,
      starts_at: at(5, 19),
      address: 'Красноярск, стадион «Локомотив»',
      lat: 56.0291,
      lng: 92.905,
      level: 'amateur',
      requirements: 'Пробегаете 5 км без остановки',
      join_mode: 'approval',
      capacity: 15,
    }),
    event(E.tabletennis, {
      title: 'Настольный теннис после работы',
      description: 'Два стола в зале, играем на вылет.',
      sport_id: SP.tabletennis,
      organizer_user_id: U.sergey,
      starts_at: at(2, 18, 30),
      address: 'Красноярск, пр. Мира, 50, спортзал',
      lat: 56.0118,
      lng: 92.8691,
      equipment: 'Своя ракетка — по желанию, есть запасные',
      join_mode: 'open',
      capacity: 4,
    }),
    event(E.yoga, {
      title: 'Йога на траве',
      description: 'Мягкая практика на рассвете. Подойдёт тем, кто никогда не занимался.',
      sport_id: SP.yoga,
      organizer_user_id: U.anna,
      starts_at: at(6, 8),
      address: 'Красноярск, парк «Роев ручей», большая поляна',
      lat: 55.9726,
      lng: 92.7638,
      level: 'beginner',
      equipment: 'Коврик, плед',
      join_mode: 'open',
    }),
    event(E.cycling, {
      title: 'Велопрогулка вдоль Енисея',
      description: 'Неспешно 20 км с остановками.',
      sport_id: SP.cycling,
      organizer_user_id: U.petr,
      starts_at: at(3, 10),
      address: 'Где-то в центре',
      join_mode: 'open',
      status: 'unpublished',
      moderation_reason: 'Не указано место встречи. Уточните адрес и опубликуйте событие заново.',
    }),
    event(E.gorodki, {
      title: 'Городки: учимся с нуля',
      description: 'Старинная русская игра. Покажу правила и дам биты, нужно только желание.',
      sport_id: SP.gorodki,
      organizer_user_id: U.sergey,
      starts_at: at(7, 11),
      address: 'Красноярск, Татышев парк, площадка у проката',
      lat: 56.0262,
      lng: 92.9203,
      level: 'beginner',
      join_mode: 'open',
    }),
  ]

  const part = (
    id: number,
    event_id: number,
    user_id: number,
    status: ParticipationRow['status'],
    extra: Partial<ParticipationRow> = {},
  ): ParticipationRow => ({
    id,
    event_id,
    user_id,
    status,
    message: null,
    decision_reason: null,
    decided_at: status === 'confirmed' || status === 'rejected' ? created : null,
    created_at: created,
    ...extra,
  })

  const participations: ParticipationRow[] = [
    part(1, E.runMorning, U.anna, 'confirmed'),
    part(2, E.runMorning, U.sergey, 'confirmed'),
    part(3, E.football, U.sergey, 'confirmed'),
    part(4, E.football, U.petr, 'pending', {
      message: 'Играл в школе, давно не практиковался — возьмёте?',
    }),
    part(5, E.volleyball, U.anna, 'pending', { message: 'Играю второй сезон.' }),
    part(6, E.runInterval, U.sergey, 'pending'),
    part(7, E.tabletennis, U.nina, 'confirmed'),
    part(8, E.cycling, U.anna, 'confirmed'),
    part(9, E.workout, U.petr, 'confirmed'),
  ]

  const comment = (
    id: number,
    event_id: number,
    author_id: number,
    text: string,
    extra: Partial<CommentRow> = {},
  ): CommentRow => ({
    id,
    event_id,
    author_id,
    as_organization_id: null,
    parent_id: null,
    text,
    is_hidden: false,
    hidden_reason: null,
    created_at: created,
    updated_at: created,
    ...extra,
  })

  const comments: CommentRow[] = [
    comment(1, E.football, U.petr, 'Площадка с искусственным газоном или асфальт?'),
    comment(2, E.football, U.anna, 'Искусственный газон, бутсы с шипами не нужны.', {
      parent_id: 1,
    }),
    comment(3, E.runMorning, U.sergey, 'Если пойдёт дождь, пробежка будет?'),
    comment(4, E.runMorning, U.igor, 'Да, бегаем в любую погоду, кроме грозы.', {
      parent_id: 3,
      as_organization_id: O.enisey,
    }),
    comment(5, E.chessPark, U.petr, 'Продаю абонементы в спортзал со скидкой, пишите в личку!!!', {
      is_hidden: true,
      hidden_reason: 'Реклама',
    }),
  ]

  // Памятки по безопасности — как на бэке: общие и привязанные к виду спорта.
  const slide = (title: string | null, body: string) => ({ title, body, image_url: null })
  const safetyBanners: SafetyBannerRow[] = [
    {
      id: 1,
      title: 'Ушибы и растяжения',
      subtitle: 'Что делать в первые минуты',
      category: 'first_aid',
      cover_url: null,
      accent_color: '#B3261E',
      sport_id: null,
      sort_order: 1,
      published: true,
      slides: [
        slide(
          'Остановите нагрузку',
          'Сразу прекратите тренировку — «через боль» травма усиливается.',
        ),
        slide('Холод', 'Приложите холод через ткань на 15–20 минут.'),
        slide('Когда к врачу', 'Сильная боль, отёк, нельзя опереться — нужен осмотр врача.'),
      ],
    },
    {
      id: 2,
      title: 'Разминка перед тренировкой',
      subtitle: 'Как не травмироваться',
      category: 'safety_rules',
      cover_url: null,
      accent_color: '#1F6F43',
      sport_id: null,
      sort_order: 2,
      published: true,
      slides: [
        slide('5–10 минут', 'Лёгкий бег или ходьба, чтобы разогреть мышцы.'),
        slide('Суставы', 'Вращения шеей, плечами, тазом, коленями и стопами.'),
      ],
    },
    {
      id: 3,
      title: 'Безопасная пробежка',
      subtitle: 'Город, темнота, дорога',
      category: 'safety_rules',
      cover_url: null,
      accent_color: '#2B4C7E',
      sport_id: SP.running,
      sort_order: 3,
      published: true,
      slides: [
        slide('Будьте заметны', 'В темноте — светоотражатели и яркая одежда.'),
        slide('Дорога', 'Бегите навстречу движению и не надевайте оба наушника.'),
      ],
    },
    {
      id: 4,
      title: 'Экстренные номера',
      subtitle: '112 — единый номер',
      category: 'emergency',
      cover_url: null,
      accent_color: '#C2410C',
      sport_id: null,
      sort_order: 4,
      published: true,
      slides: [slide('112', 'Звонок бесплатный, с любого телефона, даже без денег на счёте.')],
    },
    {
      id: 5,
      title: 'Черновик: жара',
      subtitle: 'Пока не опубликован',
      category: 'safety_rules',
      cover_url: null,
      accent_color: '#C2410C',
      sport_id: null,
      sort_order: 5,
      published: false,
      slides: [slide(null, 'Пейте воду каждые 15–20 минут.')],
    },
  ]

  // Как в демо-данных бэка: одна нерассмотренная жалоба от первого пользователя.
  const reports: ReportRow[] = [
    {
      id: 1,
      event_id: E.runMorning,
      reporter_id: U.anna,
      reason: 'misleading',
      comment: 'На набережной ремонт, место встречи неактуально',
      status: 'pending',
      resolution_note: null,
      resolved_at: null,
      created_at: created,
    },
  ]

  return { users, organizations, sports, events, participations, comments, safetyBanners, reports }
}
