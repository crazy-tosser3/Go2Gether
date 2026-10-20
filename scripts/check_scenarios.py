"""Проверка шести базовых сценариев через REST API.

Запуск: python3 scripts/check_scenarios.py [BASE_URL]   (по умолчанию http://localhost:8080)

Скрипт входит под демо-аккаунтами (demo_account_id) и работает только с данными, которые
создаёт сам: мероприятия, организация, заявки, комментарии; затем снимает и удаляет своё
мероприятие. Сидовые записи не меняет. Данные остаются в базе — запускайте на локальной/тестовой БД. Делает ~60 изменяющих запросов: повторный
запуск в течение минуты с того же IP упрётся в лимит RateLimitFilter (60 записей/мин).
Код выхода 1, если хотя бы одна проверка не прошла.
"""
import json, sys, urllib.request, urllib.error
from datetime import datetime, timedelta, timezone

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
API = BASE + "/api/v1"


def call(method, path, token=None, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(API + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read()
            return r.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read()
        return e.code, json.loads(raw) if raw else None


def check(name, cond, extra=""):
    print(("OK   " if cond else "FAIL ") + name + (" | " + str(extra) if extra else ""))
    if not cond:
        global failed
        failed += 1


failed = 0
st, demo = call("GET", "/auth/demo-accounts")
check("demo-accounts 200", st == 200, [(d["id"], d["email"], d["role"]) for d in demo])
ids = {d["email"]: d["id"] for d in demo}


def login(email):
    req = urllib.request.Request(API + "/auth/login", data=json.dumps({"demo_account_id": ids[email]}).encode(),
                                 method="POST", headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req) as r:
        cookie = r.headers["Set-Cookie"]
    return cookie.split(";")[0].split("=", 1)[1]


def login_pwd(email):
    st, r = call("POST", "/auth/login", body={"email": email, "password": "Demo1234!"})
    return st


for e in ids:
    check("password login " + e, login_pwd(e) == 200)

U1, U2, ORG, ORG2, ADM = (login(e) for e in
                          ["user@demo.local", "user2@demo.local", "org@demo.local", "org2@demo.local", "admin@demo.local"])
start = (datetime.now(timezone.utc) + timedelta(days=3)).replace(microsecond=0).isoformat().replace("+00:00", "Z")

# ---------- Scenario 1
st, sports = call("GET", "/sports")
football = next(s for s in sports if s["name"] == "Футбол")
st, ev = call("POST", "/events", U1, {"title": "S1 сценарий", "sport_id": football["id"], "starts_at": start,
                                     "address": "Красноярск, ул. Тест 1", "city": "Красноярск",
                                     "join_mode": "open", "capacity": 5, "requirements": "Кроссовки"})
check("S1 create event 201", st == 201, (st, ev.get("organizer") if ev else None))
s1 = ev["id"]
st, r = call("GET", f"/events/{s1}", U2)
check("S1 other user sees event", st == 200 and r["title"] == "S1 сценарий")
st, r = call("GET", "/events?q=S1%20%D1%81%D1%86%D0%B5%D0%BD%D0%B0%D1%80%D0%B8%D0%B9")
check("S1 in feed search", st == 200 and any(x["id"] == s1 for x in r["items"]) if st == 200 else False,
      list(r.keys()) if isinstance(r, dict) else r)
st, ev2 = call("POST", "/events", U1, {"title": "S1 новый вид", "sport_name": "  Падел  ", "starts_at": start,
                                      "address": "Красноярск", "join_mode": "approval"})
check("S1 new sport via sport_name", st == 201 and ev2["sport"]["name"] == "Падел" and ev2["sport"]["is_custom"],
      ev2.get("sport") if ev2 else st)
st, ev3 = call("POST", "/events", U2, {"title": "S1 дубль", "sport_name": "падел", "starts_at": start,
                                      "address": "Красноярск", "join_mode": "open"})
check("S1 duplicate sport reused", st == 201 and ev3["sport"]["id"] == ev2["sport"]["id"])
st, r = call("POST", "/events", U1, {"title": "past", "sport_id": football["id"],
                                    "starts_at": "2020-01-01T10:00:00Z", "address": "x", "join_mode": "open"})
check("S1 past date -> 422", st == 422, r)
st, r = call("POST", "/events", U1, {"title": "", "starts_at": start, "address": "x", "join_mode": "open"})
check("S1 empty title, no sport -> 422", st == 422, r)
st, r = call("POST", "/events", None, {"title": "x"})
check("S1 anon -> 401", st == 401)

# ---------- Scenario 2
st, org = call("POST", "/organizations", U1, {"name": "S2 Клуб", "type": "club", "contacts": "+7 900 000-00-00",
                                             "city": "Красноярск", "sports_description": "Футбол"})
check("S2 create org pending", st == 201 and org["status"] == "pending", (st, org))
oid = org["id"]
st, r = call("GET", f"/organizations/{oid}", U2)
check("S2 pending org hidden from others (404)", st == 404)
st, r = call("POST", "/events", U1, {"title": "S2 early", "sport_id": football["id"], "starts_at": start,
                                    "address": "x", "join_mode": "open", "organization_id": oid})
check("S2 pending org cannot publish (403)", st == 403, r)
st, r = call("GET", "/admin/organizations?status=pending", ADM)
check("S2 admin sees pending list", st == 200 and any(o["id"] == oid for o in r["items"]))
st, r = call("POST", f"/admin/organizations/{oid}/reject", ADM, {"reason": "Нет описания"})
check("S2 reject", st == 200 and r["status"] == "rejected")
st, r = call("GET", "/organizations/my", U1)
mine = next(o for o in r if o["id"] == oid)
check("S2 owner sees reject reason", mine["reject_reason"] == "Нет описания", mine.get("reject_reason"))
st, n = call("GET", "/notifications", U1)
check("S2 ORG_REJECTED notification", any(x["type"] == "org_rejected" for x in n), [x["type"] for x in n][:5])
st, r = call("POST", f"/organizations/{oid}/resubmit", U1)
check("S2 resubmit -> pending", st == 200 and r["status"] == "pending")
st, r = call("POST", f"/admin/organizations/{oid}/approve", ADM)
check("S2 approve", st == 200 and r["status"] == "approved")
st, r = call("POST", f"/admin/organizations/{oid}/approve", ADM)
check("S2 double approve -> 409", st == 409)
st, oev = call("POST", "/events", U1, {"title": "S2 от клуба", "sport_id": football["id"], "starts_at": start,
                                      "address": "x", "join_mode": "open", "organization_id": oid})
check("S2 approved org publishes", st == 201 and oev["organizer"]["type"] == "organization", oev.get("organizer"))
st, r = call("GET", f"/organizations/{oid}", U2)
check("S2 public org page", st == 200)
st, r = call("GET", f"/organizations/{oid}/events", None)
check("S2 org events public", st == 200 and any(e["id"] == oev["id"] for e in r))
st, r = call("POST", "/events", U2, {"title": "x", "sport_id": football["id"], "starts_at": start,
                                    "address": "x", "join_mode": "open", "organization_id": oid})
check("S2 non-owner cannot publish as org (403)", st == 403)

# ---------- Scenario 3
st, r = call("POST", f"/events/{s1}/join", U2, {})
check("S3 open join -> confirmed", st in (200, 201) and r["participation"]["status"] == "confirmed", (st, r))
st, r = call("GET", f"/events/{s1}/my-participation", U2)
check("S3 participant status", r["status"] == "confirmed")
st, r = call("GET", f"/events/{s1}/participants", U1)
check("S3 organizer sees participant with email", any(p["user"]["name"] == "Мария Иванова" and p.get("email")
                                                     for p in r["participants"]), r)
st, r = call("GET", f"/events/{s1}/participants", None)
check("S3 public participants without email", all(not p.get("email") for p in r["participants"]) and r["count"] == 1)
st, r = call("POST", f"/events/{s1}/join", U2, {})
check("S3 double join -> 409 ALREADY_JOINED", st == 409 and r["error"]["code"] == "ALREADY_JOINED")
st, r = call("POST", f"/events/{s1}/join", U1, {})
check("S3 own event -> 409 OWN_EVENT", st == 409 and r["error"]["code"] == "OWN_EVENT")

# ---------- Scenario 4
aev = ev2["id"]  # approval event by U1
st, r = call("POST", f"/events/{aev}/join", U2, {"message": "Хочу"})
check("S4 apply -> pending", r["participation"]["status"] == "pending", (st, r))
pid = r["participation"]["id"]
st, n = call("GET", "/notifications", U1)
check("S4 organizer NEW_REGISTRATION", any(x["type"] == "new_registration" and x["event_id"] == aev for x in n))
st, r = call("GET", f"/events/{aev}/requests", U1)
check("S4 organizer sees request", any(p["participation_id"] == pid for p in r))
st, r = call("GET", f"/events/{aev}/requests", U2)
check("S4 non-organizer requests -> 403", st == 403)
st, r = call("POST", f"/participations/{pid}/reject", U2, {"reason": "x"})
check("S4 non-organizer reject -> 403", st == 403)
st, r = call("POST", f"/participations/{pid}/reject", U1, {"reason": "Нужен опыт"})
check("S4 reject", st == 200 and r["status"] == "rejected" and r["decision_reason"] == "Нужен опыт", r)
st, r = call("GET", f"/events/{aev}/my-participation", U2)
check("S4 participant sees rejected + reason", r["status"] == "rejected" and r["decision_reason"] == "Нужен опыт")
st, r = call("POST", f"/events/{aev}/join", U2, {})
check("S4 reapply after reject -> 409 REJECTED", st == 409 and r["error"]["code"] == "REJECTED", r)
# approve branch: org2 creates an approval event, user1 applies, org2 approves
st, gor = call("POST", "/events", ORG2, {"title": "S4 по заявке", "sport_id": football["id"], "starts_at": start,
                                        "address": "Красноярск", "join_mode": "approval", "capacity": 3})
check("S4 approval event created", st == 201, st)
st, r = call("POST", f"/events/{gor['id']}/join", U1, {"message": "Возьмите"})
check("S4 apply (approve branch) -> pending", st == 201 and r["participation"]["status"] == "pending", (st, r))
pid2 = r["participation"]["id"]
st, r = call("POST", f"/participations/{pid2}/approve", ORG2)
check("S4 approve", st == 200 and r["status"] == "confirmed", r)
st, r = call("GET", f"/events/{gor['id']}/participants", ORG2)
check("S4 approved in participants", any(p["participation_id"] == pid2 for p in r["participants"]))
st, r = call("GET", f"/events/{gor['id']}/my-participation", U1)
check("S4 participant sees confirmed", r["status"] == "confirmed")
st, n = call("GET", "/notifications", U1)
check("S4 REGISTRATION_CONFIRMED", any(x["type"] == "registration_confirmed" and x["event_id"] == gor["id"] for x in n))
st, r = call("POST", f"/participations/{pid2}/approve", ORG2)
check("S4 re-approve -> 409", st == 409)

# ---------- Scenario 5
st, q = call("POST", f"/events/{s1}/comments", U2, {"text": "Есть душ?"})
check("S5 question", st == 201 and q["author"]["name"] == "Мария Иванова" and not q["is_organizer"], (st, q))
st, n = call("GET", "/notifications", U1)
check("S5 organizer NEW_COMMENT", any(x["type"] == "new_comment" and x["event_id"] == s1 for x in n))
st, a = call("POST", f"/events/{s1}/comments", U1, {"text": "Да", "parent_id": q["id"]})
check("S5 answer by organizer", st == 201 and a["is_organizer"] and a["parent_id"] == q["id"], a)
st, n = call("GET", "/notifications", U2)
check("S5 COMMENT_REPLY", any(x["type"] == "comment_reply" and x["event_id"] == s1 for x in n))
st, r = call("GET", f"/events/{s1}/comments", None)
check("S5 thread visible", st == 200 and [c["id"] for c in r["items"]] == [q["id"], a["id"]], r)
st, oc = call("POST", f"/events/{oev['id']}/comments", U1, {"text": "От клуба", "as_organization_id": oid})
check("S5 comment as organization", st == 201 and oc["as_organization"]["id"] == oid, oc)
st, r = call("PATCH", f"/comments/{q['id']}", U1, {"text": "hack"})
check("S5 edit foreign comment -> 403", st == 403)
st, r = call("POST", f"/events/{s1}/comments", U2, {"text": ""})
check("S5 empty -> 422", st == 422)

# ---------- Scenario 6
st, r = call("POST", f"/admin/events/{s1}/unpublish", ADM, {"reason": "Спам"})
check("S6 unpublish", st == 200 and r["status"] == "unpublished", r.get("status") if r else st)
st, r = call("GET", "/events?size=100")
check("S6 gone from feed", all(e["id"] != s1 for e in r["items"]))
st, r = call("GET", f"/events/{s1}", None)
check("S6 anon direct link -> 410", st == 410, r)
st, r = call("GET", f"/events/{s1}", U2)
check("S6 participant still sees card", st == 200 and r["status"] == "unpublished", (st, r.get("unavailable_reason") if r else None))
st, r = call("GET", f"/events/{s1}/my-participation", U2)
check("S6 participation remains confirmed", r["status"] == "confirmed", r)
st, n = call("GET", "/notifications", U2)
check("S6 participant notified EVENT_UNPUBLISHED", any(x["type"] == "event_unpublished" and x["event_id"] == s1 for x in n))
st, n = call("GET", "/notifications", U1)
check("S6 organizer notified", any(x["type"] == "event_unpublished" and x["event_id"] == s1 for x in n))
st, r = call("POST", f"/events/{s1}/join", ORG, {})
check("S6 join unpublished -> 410", st == 410, r)
st, r = call("POST", f"/events/{s1}/comments", U2, {"text": "?"})
check("S6 comment unpublished -> 410", st == 410)
st, r = call("GET", "/admin/audit-log", ADM)
check("S6 audit log entry", any(x["target_id"] == s1 for x in r["items"]), r["items"][:1])
st, r = call("POST", f"/admin/events/{s1}/restore", ADM)
check("S6 restore", st == 200 and r["status"] == "published")
st, r = call("DELETE", f"/admin/events/{s1}", ADM, {"reason": "Удаляем"})
check("S6 delete", st == 200 and r["status"] == "deleted")
st, r = call("GET", f"/events/{s1}", None)
check("S6 deleted -> 410", st == 410)
st, r = call("POST", f"/admin/events/{s1}/unpublish", U1, {"reason": "x"})
check("S6 user cannot unpublish -> 403", st == 403)

# ---------- extra
st, r = call("POST", f"/events/{oev['id']}/cancel", U1, {"reason": "Дождь"})
check("X cancel by organizer", st == 200 and r["status"] == "cancelled")
st, r = call("GET", "/recommendations/events", U2)
check("X recommendations", st == 200 and isinstance(r, list), st)
st, r = call("GET", "/safety/banners")
check("X safety banners", st == 200 and len(r) >= 7, st)
st, r = call("GET", "/admin/dashboard", ADM)
check("X dashboard", st == 200, r)
print("FAILED:", failed)
sys.exit(1 if failed else 0)
