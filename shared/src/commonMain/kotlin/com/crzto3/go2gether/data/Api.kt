package com.crzto3.go2gether.data

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.serialization.serializer

/** Эндпоинты бэка `/api/v1`, по группам как в frontend/src/api. */
class Api(val client: ApiClient = ApiClient()) {

    /**
     * Координаты мероприятий из списков. Карточка (GET /events/{id}) на текущей сборке бэка отдаёт lat/lng = null,
     * а список — с точкой; как useEventCoords.ts на сайте, берём точку из списка.
     */
    val coords = EventCoords()

    /** Точка мероприятия: из карточки, из уже загруженных списков или поиском в ленте по названию. */
    suspend fun eventPoint(event: EventDetails): Pair<Double, Double>? {
        if (event.lat != null && event.lng != null) return event.lat to event.lng
        coords[event.id]?.let { return it }
        runCatching { events(q = event.title, size = 50) }
        return coords[event.id]
    }

    private suspend inline fun <reified T> call(
        method: HttpMethod,
        path: String,
        query: Map<String, Any?> = emptyMap(),
        body: Any? = null,
    ): T {
        val text = client.send(method, path, query, body).bodyAsText()
        if (T::class == Unit::class) return Unit as T
        return AppJson.decodeFromString(serializer<T>(), text.ifBlank { "null" })
    }

    // --- auth ---
    suspend fun demoAccounts(): List<DemoAccount> = call(HttpMethod.Get, "/auth/demo-accounts")
    suspend fun loginDemo(id: Id): AuthResponse =
        call(HttpMethod.Post, "/auth/login", body = LoginRequest(demoAccountId = id))
    suspend fun login(email: String, password: String): AuthResponse =
        call(HttpMethod.Post, "/auth/login", body = LoginRequest(email = email, password = password))
    suspend fun register(body: RegisterRequest): AuthResponse = call(HttpMethod.Post, "/auth/register", body = body)
    suspend fun logout(): Unit = call(HttpMethod.Post, "/auth/logout")
    suspend fun me(): Me = call(HttpMethod.Get, "/auth/me")

    // --- sports ---
    suspend fun sports(q: String? = null): List<Sport> = call(HttpMethod.Get, "/sports", mapOf("q" to q))

    // --- events ---
    suspend fun events(
        q: String? = null,
        sport: Id? = null,
        city: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        level: String? = null,
        joinMode: String? = null,
        organization: Id? = null,
        organizer: Id? = null,
        page: Int = 0,
        size: Int = 20,
    ): Page<EventSummary> = call<Page<EventSummary>>(
        HttpMethod.Get, "/events",
        mapOf(
            "q" to q, "sport" to sport, "city" to city, "date_from" to dateFrom, "date_to" to dateTo,
            "level" to level, "join_mode" to joinMode, "organization" to organization,
            "organizer" to organizer, "page" to page, "size" to size,
        ),
    ).also { coords.remember(it.items) }

    suspend fun event(id: Id): EventDetails = call(HttpMethod.Get, "/events/$id")
    suspend fun createEvent(body: CreateEventRequest): EventDetails = call(HttpMethod.Post, "/events", body = body)
    suspend fun updateEvent(id: Id, body: UpdateEventRequest): EventDetails =
        call(HttpMethod.Patch, "/events/$id", body = body)
    suspend fun cancelEvent(id: Id, reason: String): EventDetails =
        call(HttpMethod.Post, "/events/$id/cancel", body = ReasonRequest(reason))
    suspend fun join(id: Id, message: String? = null): JoinResult =
        call(HttpMethod.Post, "/events/$id/join", body = MessageRequest(message))
    suspend fun leave(id: Id): Participation = call(HttpMethod.Delete, "/events/$id/join")
    suspend fun participants(id: Id): ParticipantsList = call(HttpMethod.Get, "/events/$id/participants")
    suspend fun requests(id: Id, status: String = "pending"): List<Participant> =
        call(HttpMethod.Get, "/events/$id/requests", mapOf("status" to status))
    suspend fun similar(id: Id, limit: Int = 4): List<Recommendation> =
        call<List<Recommendation>>(HttpMethod.Get, "/events/$id/similar", mapOf("limit" to limit))
            .also { list -> coords.remember(list.map { it.event }) }
    suspend fun report(id: Id, reason: String, comment: String?): Report =
        call(HttpMethod.Post, "/events/$id/reports", body = ReportRequest(reason, comment))

    suspend fun approve(pid: Id): Participation = call(HttpMethod.Post, "/participations/$pid/approve")
    suspend fun reject(pid: Id, reason: String?): Participation =
        call(HttpMethod.Post, "/participations/$pid/reject", body = ReasonRequest(reason))

    suspend fun recommendations(limit: Int = 6): List<Recommendation> =
        call<List<Recommendation>>(HttpMethod.Get, "/recommendations/events", mapOf("limit" to limit))
            .also { list -> coords.remember(list.map { it.event }) }

    // --- comments ---
    suspend fun comments(eventId: Id): Page<Comment> =
        call(HttpMethod.Get, "/events/$eventId/comments", mapOf("size" to 100))
    suspend fun addComment(eventId: Id, text: String, parentId: Id? = null, asOrganizationId: Id? = null): Comment =
        call(HttpMethod.Post, "/events/$eventId/comments", body = CommentRequest(text, parentId, asOrganizationId))
    suspend fun updateComment(id: Id, text: String): Comment = call(HttpMethod.Patch, "/comments/$id", body = TextRequest(text))
    suspend fun deleteComment(id: Id): Unit = call(HttpMethod.Delete, "/comments/$id")

    // --- users ---
    suspend fun user(id: Id): PublicUser = call(HttpMethod.Get, "/users/$id")
    suspend fun updateMe(body: UpdateProfileRequest): User = call(HttpMethod.Patch, "/users/me", body = body)
    suspend fun myEvents(): List<OrganizerEvent> =
        call<List<OrganizerEvent>>(HttpMethod.Get, "/users/me/events").also { list -> coords.remember(list.map { it.event }) }
    suspend fun myParticipations(status: String? = null): List<MyParticipation> =
        call<List<MyParticipation>>(HttpMethod.Get, "/users/me/participations", mapOf("status" to status))
            .also { list -> coords.remember(list.map { it.event }) }
    suspend fun notifications(): List<Notification> = call(HttpMethod.Get, "/notifications")
    suspend fun readNotification(id: Id): Notification = call(HttpMethod.Post, "/notifications/$id/read")

    // --- organizations ---
    suspend fun organizations(q: String? = null, page: Int = 0, size: Int = 20): Page<Organization> =
        call(HttpMethod.Get, "/organizations", mapOf("q" to q, "page" to page, "size" to size))
    suspend fun organization(id: Id): Organization = call(HttpMethod.Get, "/organizations/$id")
    suspend fun organizationEvents(id: Id): List<EventSummary> =
        call<List<EventSummary>>(HttpMethod.Get, "/organizations/$id/events").also { coords.remember(it) }
    suspend fun myOrganizations(): List<Organization> = call(HttpMethod.Get, "/organizations/my")
    suspend fun createOrganization(body: CreateOrganizationRequest): Organization =
        call(HttpMethod.Post, "/organizations", body = body)
    suspend fun updateOrganization(id: Id, body: UpdateOrganizationRequest): Organization =
        call(HttpMethod.Patch, "/organizations/$id", body = body)
    suspend fun resubmitOrganization(id: Id): Organization = call(HttpMethod.Post, "/organizations/$id/resubmit")

    // --- admin ---
    suspend fun adminDashboard(): AdminDashboard = call(HttpMethod.Get, "/admin/dashboard")
    suspend fun adminOrganizations(status: String? = "pending", q: String? = null): Page<Organization> =
        call(HttpMethod.Get, "/admin/organizations", mapOf("status" to status, "q" to q, "size" to 50))
    suspend fun approveOrganization(id: Id): Organization = call(HttpMethod.Post, "/admin/organizations/$id/approve")
    suspend fun rejectOrganization(id: Id, reason: String): Organization =
        call(HttpMethod.Post, "/admin/organizations/$id/reject", body = ReasonRequest(reason))
    suspend fun adminEvents(status: String?, q: String?): Page<EventSummary> =
        call(HttpMethod.Get, "/admin/events", mapOf("status" to status, "q" to q, "size" to 50))
    suspend fun unpublishEvent(id: Id, reason: String): EventDetails =
        call(HttpMethod.Post, "/admin/events/$id/unpublish", body = ReasonRequest(reason))
    suspend fun deleteEvent(id: Id, reason: String): EventDetails =
        call(HttpMethod.Delete, "/admin/events/$id", body = ReasonRequest(reason))
    suspend fun restoreEvent(id: Id): EventDetails = call(HttpMethod.Post, "/admin/events/$id/restore")
    suspend fun hideComment(id: Id, reason: String): Comment =
        call(HttpMethod.Post, "/admin/comments/$id/hide", body = ReasonRequest(reason))
    suspend fun adminReports(status: String? = "pending"): Page<Report> =
        call(HttpMethod.Get, "/admin/reports", mapOf("status" to status, "size" to 50))
    suspend fun acceptReport(id: Id, reason: String): Report =
        call(HttpMethod.Post, "/admin/reports/$id/accept", body = ReasonRequest(reason))
    suspend fun rejectReport(id: Id, reason: String?): Report =
        call(HttpMethod.Post, "/admin/reports/$id/reject", body = ReasonRequest(reason))
}

class EventCoords {
    private val known = mutableMapOf<Id, Pair<Double, Double>>()

    operator fun get(id: Id): Pair<Double, Double>? = known[id]

    fun remember(events: List<EventSummary>) {
        events.forEach { e -> if (e.lat != null && e.lng != null) known[e.id] = e.lat to e.lng }
    }
}
