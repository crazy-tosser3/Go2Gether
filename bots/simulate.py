#!/usr/bin/env python3
"""
Боты, симулирующие поведение пользователей STOLOTO API.

Только стандартная библиотека Python 3.9+, без зависимостей.

Типы ботов (персоны):
  guest       — гость без входа: листает ленту, фильтрует, смотрит карточки, комментарии, виды спорта, организации.
  participant — регистрируется, заполняет профиль, ищет события, записывается/отменяет запись,
                пишет комментарии, читает уведомления и свои записи.
  organizer   — регистрируется, создаёт события (open/approval), разбирает заявки (approve/reject),
                отвечает в обсуждениях, редактирует и иногда отменяет свои события.

Пример:
  python3 bots/simulate.py --base-url http://localhost:8080 --bots 20 --duration 120
  python3 bots/simulate.py --mix guest=5,participant=10,organizer=2 --duration 300 --cleanup
"""

import argparse
import json
import random
import string
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from collections import defaultdict
from datetime import datetime, timedelta, timezone

API = "/api/v1"

CITIES = ["Москва", "Санкт-Петербург", "Казань", "Новосибирск", "Екатеринбург", "Сочи"]
LEVELS = ["beginner", "amateur", "pro"]
FIRST_NAMES = ["Алексей", "Мария", "Иван", "Ольга", "Дмитрий", "Анна", "Сергей", "Елена", "Павел", "Наталья"]
LAST_NAMES = ["Иванов", "Смирнова", "Кузнецов", "Попова", "Соколов", "Лебедева", "Козлов", "Новикова"]
SEARCH_WORDS = ["футбол", "бег", "йога", "турнир", "тренировка", "забег", "шахматы", "волейбол", ""]
EVENT_TITLES = ["Вечерняя игра", "Утренняя пробежка", "Турнир выходного дня", "Открытая тренировка",
                "Дружеский матч", "Сбор для новичков", "Интенсив", "Командная игра"]
COMMENTS = ["Буду!", "А мяч свой брать?", "Во сколько сбор?", "Отличное место, был там", "Есть парковка?",
            "Можно прийти с другом?", "Подскажите по экипировке", "Спасибо за организацию!", "+1"]
ORGANIZER_REPLIES = ["Да, конечно", "Сбор за 15 минут до начала", "Всё есть на месте", "Ждём всех!"]
JOIN_MESSAGES = [None, "Хочу присоединиться", "Играю пару лет, уровень средний", "Впервые, но очень хочу"]


# ---------------------------------------------------------------- статистика

class Stats:
    def __init__(self):
        self.lock = threading.Lock()
        self.calls = defaultdict(lambda: {"n": 0, "errors": 0, "lat": [], "codes": defaultdict(int)})
        self.actions = defaultdict(int)

    def record(self, name, status, latency):
        with self.lock:
            s = self.calls[name]
            s["n"] += 1
            s["lat"].append(latency)
            s["codes"][status] += 1
            if status == 0 or status >= 500:
                s["errors"] += 1

    def action(self, persona, name):
        with self.lock:
            self.actions[f"{persona}.{name}"] += 1

    def total(self):
        with self.lock:
            return sum(s["n"] for s in self.calls.values()), sum(s["errors"] for s in self.calls.values())

    def report(self, elapsed):
        with self.lock:
            rows = sorted(self.calls.items(), key=lambda kv: -kv[1]["n"])
            total = sum(s["n"] for _, s in rows)
            print()
            print(f"=== Итог: {total} запросов за {elapsed:.0f} с ({total / max(elapsed, 1):.1f} rps)")
            print(f"{'запрос':42} {'кол-во':>7} {'p50 мс':>8} {'p95 мс':>8} {'max мс':>8}  коды")
            for name, s in rows:
                lat = sorted(s["lat"])
                p = lambda q: lat[min(len(lat) - 1, int(q * len(lat)))] * 1000
                codes = " ".join(f"{c}:{k}" for c, k in sorted(s["codes"].items()))
                print(f"{name:42} {s['n']:7} {p(.5):8.0f} {p(.95):8.0f} {lat[-1] * 1000:8.0f}  {codes}")
            print()
            print("Действия ботов:")
            for name, n in sorted(self.actions.items(), key=lambda kv: -kv[1]):
                print(f"  {name:40} {n}")


# ---------------------------------------------------------------- HTTP-клиент

class ApiError(Exception):
    def __init__(self, status, body):
        super().__init__(f"HTTP {status}: {body}")
        self.status = status
        self.body = body


class Client:
    """HTTP-клиент одного бота. JWT берётся из Set-Cookie и отправляется в Authorization: Bearer
    (cookie помечена Secure и не уходит по http://localhost)."""

    def __init__(self, base_url, stats, fake_ip=None, timeout=15):
        self.base = base_url.rstrip("/")
        self.stats = stats
        self.token = None
        self.fake_ip = fake_ip
        self.timeout = timeout

    def call(self, method, path, label, body=None, params=None, ok=(200, 201, 204)):
        url = self.base + API + path
        if params:
            url += "?" + urllib.parse.urlencode({k: v for k, v in params.items() if v is not None})
        data = json.dumps(body).encode() if body is not None else None
        headers = {"Accept": "application/json", "User-Agent": "stoloto-bot/1.0"}
        if data is not None:
            headers["Content-Type"] = "application/json"
        if self.token:
            headers["Authorization"] = "Bearer " + self.token
        if self.fake_ip:
            headers["X-Forwarded-For"] = self.fake_ip

        for attempt in range(3):
            req = urllib.request.Request(url, data=data, method=method, headers=headers)
            started = time.monotonic()
            try:
                with urllib.request.urlopen(req, timeout=self.timeout) as resp:
                    status, raw, hdrs = resp.status, resp.read(), resp.headers
            except urllib.error.HTTPError as e:
                status, raw, hdrs = e.code, e.read(), e.headers
            except (urllib.error.URLError, OSError) as e:
                self.stats.record(label, 0, time.monotonic() - started)
                raise ApiError(0, str(e))
            self.stats.record(label, status, time.monotonic() - started)

            if status == 429 and attempt < 2:
                time.sleep(float(hdrs.get("Retry-After", "5")) + random.random())
                continue
            break

        for cookie in hdrs.get_all("Set-Cookie") or []:
            if cookie.startswith("jwt="):
                value = cookie.split(";", 1)[0][4:]
                self.token = value or None

        payload = None
        if raw:
            try:
                payload = json.loads(raw)
            except ValueError:
                payload = raw.decode(errors="replace")
        if status not in ok:
            raise ApiError(status, payload)
        return payload


# ---------------------------------------------------------------- боты

def rand_suffix(n=6):
    return "".join(random.choices(string.ascii_lowercase + string.digits, k=n))


def iso(dt):
    return dt.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class Bot:
    persona = "bot"
    actions = {}  # имя метода -> вес

    def __init__(self, idx, args, stats, stop, registry):
        self.idx = idx
        self.args = args
        self.stats = stats
        self.stop = stop
        self.registry = registry  # общие данные между ботами (созданные события и т.п.)
        fake_ip = f"10.{random.randint(0, 255)}.{random.randint(0, 255)}.{random.randint(1, 254)}" \
            if args.fake_ips else None
        self.api = Client(args.base_url, stats, fake_ip=fake_ip)
        self.name = f"{self.persona}-{idx}"
        self.email = None
        self.password = None
        self.user = None
        self.sports = []

    def log(self, msg):
        if self.args.verbose:
            print(f"[{time.strftime('%H:%M:%S')}] {self.name}: {msg}", flush=True)

    def think(self):
        lo, hi = self.args.think
        self.stop.wait(random.uniform(lo, hi))

    # --- общее

    def register(self):
        first, last = random.choice(FIRST_NAMES), random.choice(LAST_NAMES)
        self.email = f"bot-{self.args.run_id}-{self.persona}-{self.idx}@bots.test"
        self.password = "Bot-" + rand_suffix(12)
        resp = self.api.call("POST", "/auth/register", "POST /auth/register", {
            "name": f"{first} {last}", "email": self.email, "password": self.password})
        self.user = resp["user"]
        self.log(f"зарегистрирован {self.email} (id={self.user['id']})")

    def load_sports(self):
        self.sports = self.api.call("GET", "/sports", "GET /sports") or []

    def browse_feed(self, **filters):
        params = {"page": random.choice([0, 0, 0, 1]), "size": random.choice([10, 20])}
        params.update(filters)
        return self.api.call("GET", "/events", "GET /events", params=params)["items"]

    def pick_event(self, predicate=lambda e: True):
        items = self.browse_feed()
        items += self.registry.events_snapshot()
        items = [e for e in items if e.get("status") == "published" and not e.get("is_past") and predicate(e)]
        return random.choice(items) if items else None

    def run(self):
        try:
            self.setup()
        except ApiError as e:
            print(f"{self.name}: не удалось стартовать: {e}", file=sys.stderr)
            return
        names, weights = zip(*self.actions.items())
        while not self.stop.is_set():
            action = random.choices(names, weights)[0]
            try:
                getattr(self, action)()
                self.stats.action(self.persona, action)
            except ApiError as e:
                self.log(f"{action}: {e}")
            except Exception as e:  # бот не должен падать из-за неожиданного ответа
                self.log(f"{action}: {type(e).__name__}: {e}")
            self.think()

    def setup(self):
        pass

    def cleanup(self):
        if self.password and self.api.token:
            try:
                self.api.call("DELETE", "/users/me", "DELETE /users/me", {"password": self.password})
                self.log("аккаунт удалён")
            except ApiError as e:
                print(f"{self.name}: не удалось удалить аккаунт: {e}", file=sys.stderr)


class GuestBot(Bot):
    persona = "guest"
    actions = {
        "scroll_feed": 5,
        "filter_feed": 3,
        "view_event": 5,
        "read_comments": 3,
        "view_participants": 1,
        "browse_sports": 1,
        "browse_organizations": 1,
        "view_profile": 1,
    }

    def setup(self):
        self.load_sports()

    def scroll_feed(self):
        for page in range(random.randint(1, 3)):
            items = self.api.call("GET", "/events", "GET /events", params={"page": page, "size": 20})["items"]
            if not items:
                break
            self.stop.wait(random.uniform(0.3, 1.5))

    def filter_feed(self):
        today = datetime.now(timezone.utc).date()
        self.browse_feed(
            q=random.choice(SEARCH_WORDS) or None,
            sport=random.choice(self.sports)["id"] if self.sports and random.random() < .5 else None,
            city=random.choice(CITIES) if random.random() < .5 else None,
            level=random.choice(LEVELS).upper() if random.random() < .3 else None,
            join_mode=random.choice(["OPEN", "APPROVAL"]) if random.random() < .2 else None,
            date_from=today.isoformat() if random.random() < .3 else None,
            date_to=(today + timedelta(days=14)).isoformat() if random.random() < .3 else None,
        )

    def view_event(self):
        e = self.pick_event()
        if e:
            self.api.call("GET", f"/events/{e['id']}", "GET /events/{id}")

    def read_comments(self):
        e = self.pick_event()
        if e:
            self.api.call("GET", f"/events/{e['id']}/comments", "GET /events/{id}/comments")

    def view_participants(self):
        e = self.pick_event()
        if e:
            self.api.call("GET", f"/events/{e['id']}/participants", "GET /events/{id}/participants")

    def browse_sports(self):
        sports = self.api.call("GET", "/sports", "GET /sports",
                               params={"q": random.choice(["", "бол", "бег", "ш"]) or None})
        if sports:
            self.api.call("GET", f"/sports/{random.choice(sports)['id']}", "GET /sports/{id}")

    def browse_organizations(self):
        page = self.api.call("GET", "/organizations", "GET /organizations",
                             params={"city": random.choice(CITIES) if random.random() < .3 else None})
        if page["items"]:
            org = random.choice(page["items"])
            self.api.call("GET", f"/organizations/{org['id']}", "GET /organizations/{id}")
            self.api.call("GET", f"/organizations/{org['id']}/events", "GET /organizations/{id}/events")

    def view_profile(self):
        e = self.pick_event()
        if e and e.get("organizer", {}).get("type") == "user" and e["organizer"].get("id"):
            self.api.call("GET", f"/users/{e['organizer']['id']}", "GET /users/{id}", ok=(200, 404))


class ParticipantBot(GuestBot):
    persona = "participant"
    actions = {
        "scroll_feed": 4,
        "filter_feed": 3,
        "view_event": 4,
        "read_comments": 2,
        "join_event": 4,
        "leave_event": 1,
        "write_comment": 2,
        "edit_comment": 1,
        "check_notifications": 2,
        "my_participations": 2,
        "update_profile": 1,
        "whoami": 1,
    }

    def setup(self):
        self.register()
        self.load_sports()
        self.my_comments = []
        self.update_profile()

    def whoami(self):
        self.api.call("GET", "/auth/me", "GET /auth/me")

    def update_profile(self):
        body = {"city": random.choice(CITIES), "level": random.choice(LEVELS),
                "bio": random.choice(["Люблю спорт", "Ищу компанию для игр", "Бегаю по утрам", None])}
        if self.sports:
            body["sport_ids"] = [s["id"] for s in random.sample(self.sports, min(3, len(self.sports)))]
        self.api.call("PATCH", "/users/me", "PATCH /users/me", body)

    def join_event(self):
        e = self.pick_event(lambda e: e.get("organizer", {}).get("id") != self.user["id"])
        if not e:
            return
        self.api.call("GET", f"/events/{e['id']}", "GET /events/{id}")
        msg = random.choice(JOIN_MESSAGES) if e.get("join_mode") == "approval" else None
        resp = self.api.call("POST", f"/events/{e['id']}/join", "POST /events/{id}/join",
                             {"message": msg} if msg else None, ok=(201, 409, 410))
        if isinstance(resp, dict) and "participation" in resp:
            self.log(f"запись на «{e['title']}»: {resp['message']}")

    def leave_event(self):
        mine = self.api.call("GET", "/users/me/participations", "GET /users/me/participations")
        active = [m for m in mine if m["participation"]["status"] in ("pending", "confirmed", "waitlist")
                  and not m["event"].get("is_past")]
        if active:
            m = random.choice(active)
            self.api.call("DELETE", f"/events/{m['event']['id']}/join", "DELETE /events/{id}/join",
                          ok=(200, 404, 409))
            self.log(f"отменил запись на «{m['event']['title']}»")

    def write_comment(self):
        e = self.pick_event()
        if not e:
            return
        existing = self.api.call("GET", f"/events/{e['id']}/comments", "GET /events/{id}/comments")["items"]
        body = {"text": random.choice(COMMENTS)}
        visible = [c for c in existing if not c.get("is_hidden")]
        if visible and random.random() < .4:
            body["parent_id"] = random.choice(visible)["id"]
        c = self.api.call("POST", f"/events/{e['id']}/comments", "POST /events/{id}/comments", body,
                          ok=(201, 410))
        if isinstance(c, dict) and "id" in c:
            self.my_comments.append(c["id"])

    def edit_comment(self):
        if not self.my_comments:
            return
        cid = random.choice(self.my_comments)
        if random.random() < .3:
            self.api.call("DELETE", f"/comments/{cid}", "DELETE /comments/{id}", ok=(204, 404, 409))
            self.my_comments.remove(cid)
        else:
            self.api.call("PATCH", f"/comments/{cid}", "PATCH /comments/{id}",
                          {"text": random.choice(COMMENTS) + " (upd)"}, ok=(200, 404, 409))

    def check_notifications(self):
        items = self.api.call("GET", "/notifications", "GET /notifications")
        unread = [n for n in items if not n.get("is_read")]
        for n in unread[:random.randint(0, 3)]:
            self.api.call("POST", f"/notifications/{n['id']}/read", "POST /notifications/{id}/read")

    def my_participations(self):
        self.api.call("GET", "/users/me/participations", "GET /users/me/participations",
                      params={"status": random.choice([None, "CONFIRMED", "PENDING"])})


class OrganizerBot(ParticipantBot):
    persona = "organizer"
    actions = {
        "create_event": 2,
        "review_requests": 5,
        "my_events": 2,
        "reply_comment": 2,
        "edit_event": 1,
        "cancel_event": 0.2,
        "check_notifications": 2,
        "scroll_feed": 1,
    }

    def setup(self):
        self.register()
        self.load_sports()
        self.my_comments = []
        self.events = []
        for _ in range(random.randint(1, 2)):
            self.create_event()

    def create_event(self):
        starts = datetime.now(timezone.utc) + timedelta(days=random.randint(1, 21), hours=random.randint(0, 12))
        body = {
            "title": f"{random.choice(EVENT_TITLES)} #{rand_suffix(4)}",
            "description": "Событие создано ботом-симулятором для нагрузочного тестирования.",
            "starts_at": iso(starts),
            "ends_at": iso(starts + timedelta(hours=random.choice([1, 2, 3]))),
            "city": random.choice(CITIES),
            "address": f"ул. Спортивная, д. {random.randint(1, 120)}",
            "level": random.choice(LEVELS),
            "join_mode": random.choice(["open", "approval"]),
            "capacity": random.choice([None, 6, 10, 12, 20, 30]),
            "requirements": random.choice([None, "Хорошее настроение", "Своя форма"]),
        }
        if self.sports:
            body["sport_id"] = random.choice(self.sports)["id"]
        else:
            body["sport_name"] = "Футбол"
        e = self.api.call("POST", "/events", "POST /events", body)
        self.events.append(e["id"])
        self.registry.add_event(e)
        self.log(f"создал событие «{e['title']}» ({e['join_mode']}, мест: {e.get('capacity')})")

    def my_events(self):
        self.api.call("GET", "/users/me/events", "GET /users/me/events")

    def review_requests(self):
        if not self.events:
            return
        eid = random.choice(self.events)
        pending = self.api.call("GET", f"/events/{eid}/requests", "GET /events/{id}/requests",
                                params={"status": "PENDING"}, ok=(200, 403, 404, 410))
        if not isinstance(pending, list):
            return
        for req in pending:
            pid = req["participation_id"]
            if random.random() < .8:
                self.api.call("POST", f"/participations/{pid}/approve", "POST /participations/{id}/approve",
                              ok=(200, 409))
            else:
                self.api.call("POST", f"/participations/{pid}/reject", "POST /participations/{id}/reject",
                              {"reason": "Набор на этот уровень закрыт"}, ok=(200, 409))
            self.stop.wait(random.uniform(0.2, 1.0))

    def reply_comment(self):
        if not self.events:
            return
        eid = random.choice(self.events)
        page = self.api.call("GET", f"/events/{eid}/comments", "GET /events/{id}/comments", ok=(200, 410))
        others = [c for c in (page or {}).get("items", [])
                  if c["author"]["id"] != self.user["id"] and not c.get("is_hidden")]
        if others:
            self.api.call("POST", f"/events/{eid}/comments", "POST /events/{id}/comments",
                          {"text": random.choice(ORGANIZER_REPLIES), "parent_id": random.choice(others)["id"]},
                          ok=(201, 410))

    def edit_event(self):
        if not self.events:
            return
        eid = random.choice(self.events)
        self.api.call("PATCH", f"/events/{eid}", "PATCH /events/{id}",
                      {"location_note": random.choice(["Вход со двора", "Сбор у главного входа", "Зал №2"])},
                      ok=(200, 409, 410))

    def cancel_event(self):
        if len(self.events) < 2:
            return
        eid = self.events.pop(random.randrange(len(self.events)))
        self.api.call("POST", f"/events/{eid}/cancel", "POST /events/{id}/cancel",
                      {"reason": "Не набралось участников"}, ok=(200, 409, 410))
        self.registry.remove_event(eid)
        self.log(f"отменил событие {eid}")
        self.create_event()


PERSONAS = {"guest": GuestBot, "participant": ParticipantBot, "organizer": OrganizerBot}


class Registry:
    """События, созданные ботами-организаторами, — чтобы участники находили их сразу, не только через ленту."""

    def __init__(self):
        self.lock = threading.Lock()
        self.events = {}

    def add_event(self, e):
        with self.lock:
            self.events[e["id"]] = e

    def remove_event(self, eid):
        with self.lock:
            self.events.pop(eid, None)

    def events_snapshot(self):
        with self.lock:
            return list(self.events.values())


# ---------------------------------------------------------------- запуск

def parse_mix(text, total):
    if text:
        mix = {}
        for part in text.split(","):
            name, _, n = part.partition("=")
            if name not in PERSONAS:
                raise SystemExit(f"неизвестная персона: {name} (доступны: {', '.join(PERSONAS)})")
            mix[name] = int(n)
        return mix
    # по умолчанию: ~10% организаторов, 50% участников, 40% гостей
    organizers = max(1, round(total * .1))
    participants = max(1, round(total * .5))
    return {"organizer": organizers, "participant": participants,
            "guest": max(0, total - organizers - participants)}


def main():
    p = argparse.ArgumentParser(description="Симуляция пользователей STOLOTO API")
    p.add_argument("--base-url", default="http://localhost:8080", help="адрес приложения (без /api/v1)")
    p.add_argument("--bots", type=int, default=10, help="всего ботов (если не задан --mix)")
    p.add_argument("--mix", help="состав, напр. guest=5,participant=10,organizer=2")
    p.add_argument("--duration", type=float, default=60, help="длительность, секунд (0 — до Ctrl+C)")
    p.add_argument("--think", type=float, nargs=2, default=[1.0, 4.0], metavar=("MIN", "MAX"),
                   help="пауза между действиями бота, секунд")
    p.add_argument("--ramp-up", type=float, default=10, help="за сколько секунд запустить всех ботов")
    p.add_argument("--no-fake-ips", dest="fake_ips", action="store_false",
                   help="не подставлять X-Forwarded-For (иначе все боты делят rate-limit одного IP)")
    p.add_argument("--cleanup", action="store_true", help="удалить аккаунты ботов в конце")
    p.add_argument("--seed", type=int, help="seed для воспроизводимости")
    p.add_argument("-v", "--verbose", action="store_true", help="подробный лог действий")
    args = p.parse_args()
    args.run_id = time.strftime("%m%d%H%M%S")
    if args.seed is not None:
        random.seed(args.seed)

    mix = parse_mix(args.mix, args.bots)
    stats, stop, registry = Stats(), threading.Event(), Registry()

    bots = []
    for persona, n in mix.items():
        for i in range(n):
            bots.append(PERSONAS[persona](len(bots) + 1, args, stats, stop, registry))
    # организаторы стартуют первыми, чтобы участникам было куда записываться
    bots.sort(key=lambda b: b.persona != "organizer")

    print(f"Запуск {len(bots)} ботов на {args.base_url}: " + ", ".join(f"{k}={v}" for k, v in mix.items()))
    started = time.monotonic()
    threads = []
    for b in bots:
        t = threading.Thread(target=b.run, name=b.name, daemon=True)
        t.start()
        threads.append(t)
        stop.wait(args.ramp_up / max(len(bots), 1))

    try:
        while not stop.is_set():
            elapsed = time.monotonic() - started
            if args.duration and elapsed >= args.duration:
                break
            stop.wait(5)
            n, errors = stats.total()
            print(f"[{elapsed:5.0f} с] запросов: {n}, ошибок (5xx/сеть): {errors}", flush=True)
    except KeyboardInterrupt:
        print("\nОстановка…")
    stop.set()
    for t in threads:
        t.join(timeout=20)

    if args.cleanup:
        print("Удаление аккаунтов ботов…")
        for b in bots:
            b.cleanup()

    stats.report(time.monotonic() - started)
    _, errors = stats.total()
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
