package com.crzto3.go2gether.data

import kotlinx.serialization.Serializable

// Модели повторяют контракт бэка (frontend/openapi/backend.json). Поля — snake_case через JsonNamingStrategy,
// перечисления держим строками: неизвестное значение от бэка не должно ронять разбор ответа.

typealias Id = Long

@Serializable
data class Page<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 0,
    val totalPages: Int = 0,
)

@Serializable
data class ApiErrorBody(val error: ApiErrorDetails? = null)

@Serializable
data class ApiErrorDetails(
    val code: String? = null,
    val message: String? = null,
    val fields: Map<String, String>? = null,
)

@Serializable
data class User(
    val id: Id,
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val avatarUrl: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val level: String? = null,
    val sportIds: List<Id> = emptyList(),
    val isDemo: Boolean = false,
    val createdAt: String? = null,
    val birthYear: Int? = null,
    val radiusKm: Int? = null,
) {
    val isAdmin get() = role == "admin"
}

@Serializable
data class UserShort(val id: Id, val name: String = "", val avatarUrl: String? = null)

@Serializable
data class PublicUser(
    val id: Id,
    val name: String = "",
    val avatarUrl: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val level: String? = null,
    val sports: List<Sport> = emptyList(),
    val createdEvents: List<EventSummary> = emptyList(),
)

@Serializable
data class OrganizationShort(
    val id: Id,
    val name: String = "",
    val type: String? = null,
    val city: String? = null,
    val status: String? = null,
)

@Serializable
data class Me(val user: User, val organizations: List<OrganizationShort> = emptyList())

@Serializable
data class AuthResponse(val tokenType: String? = null, val expiresIn: Long? = null, val user: User)

@Serializable
data class DemoAccount(
    val id: Id,
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val description: String? = null,
)

@Serializable
data class Sport(
    val id: Id,
    val name: String = "",
    val category: String? = null,
    val icon: String? = null,
    val isCustom: Boolean = false,
)

@Serializable
data class Organizer(
    val type: String = "user",
    val id: Id,
    val name: String = "",
    val avatarUrl: String? = null,
)

@Serializable
data class EventSummary(
    val id: Id,
    val title: String = "",
    val sport: Sport,
    val organizer: Organizer,
    val startsAt: String,
    val endsAt: String? = null,
    val city: String? = null,
    val address: String? = null,
    val level: String? = null,
    val joinMode: String = "open",
    val capacity: Int? = null,
    val confirmedCount: Int = 0,
    val status: String = "published",
    val unavailableReason: String? = null,
    val isPast: Boolean = false,
    val isEdited: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class Participation(
    val id: Id,
    val eventId: Id = 0,
    val userId: Id = 0,
    val status: String,
    val message: String? = null,
    val decisionReason: String? = null,
    val decidedAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class EventDetails(
    val id: Id,
    val title: String = "",
    val description: String? = null,
    val sport: Sport,
    val organizer: Organizer,
    val organizerContacts: String? = null,
    val startsAt: String,
    val endsAt: String? = null,
    val city: String? = null,
    val address: String? = null,
    val locationNote: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val level: String? = null,
    val requirements: String? = null,
    val equipment: String? = null,
    val joinMode: String = "open",
    val capacity: Int? = null,
    val confirmedCount: Int = 0,
    val participants: List<UserShort> = emptyList(),
    val status: String = "published",
    val unavailableReason: String? = null,
    val isPast: Boolean = false,
    val isEdited: Boolean = false,
    val isOrganizer: Boolean = false,
    val myParticipation: Participation? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class JoinResult(val message: String? = null, val participation: Participation, val event: EventDetails)

@Serializable
data class MyParticipation(val participation: Participation, val event: EventSummary)

@Serializable
data class OrganizerEvent(val event: EventSummary, val pendingCount: Int = 0, val waitlistedCount: Int = 0)

@Serializable
data class Participant(
    val participationId: Id,
    val user: UserShort,
    val email: String? = null,
    val status: String,
    val message: String? = null,
    val decisionReason: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class ParticipantsList(
    val count: Int = 0,
    val capacity: Int? = null,
    val participants: List<Participant> = emptyList(),
)

@Serializable
data class Comment(
    val id: Id,
    val eventId: Id = 0,
    val parentId: Id? = null,
    val author: UserShort,
    val asOrganization: OrganizationShort? = null,
    val isOrganizer: Boolean = false,
    val text: String? = null,
    val isHidden: Boolean = false,
    val hiddenReason: String? = null,
    val isEdited: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
data class Organization(
    val id: Id,
    val name: String = "",
    val description: String? = null,
    val type: String = "club",
    val sportsDescription: String? = null,
    val city: String? = null,
    val websiteUrl: String? = null,
    val avatarUrl: String? = null,
    val contacts: String? = null,
    val status: String = "approved",
    val rejectReason: String? = null,
    val owner: UserShort? = null,
    val createdAt: String? = null,
)

@Serializable
data class Recommendation(val event: EventSummary, val score: Double = 0.0, val reasons: List<String> = emptyList())

@Serializable
data class Notification(
    val id: Id,
    val type: String = "",
    val title: String = "",
    val body: String? = null,
    val eventId: Id? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
data class Report(
    val id: Id,
    val event: EventSummary,
    val reporter: UserShort,
    val reason: String = "other",
    val comment: String? = null,
    val status: String = "pending",
    val resolutionNote: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class AdminDashboard(
    val pendingOrganizations: Int = 0,
    val approvedOrganizations: Int = 0,
    val publishedEvents: Int = 0,
    val unpublishedEvents: Int = 0,
    val deletedEvents: Int = 0,
    val cancelledEvents: Int = 0,
    val hiddenComments: Int = 0,
    val pendingReports: Int = 0,
    val users: Int = 0,
)

// --- Запросы ---

@Serializable
data class LoginRequest(val email: String? = null, val password: String? = null, val demoAccountId: Id? = null)

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String, val consent: Boolean)

@Serializable
data class MessageRequest(val message: String? = null)

@Serializable
data class ReasonRequest(val reason: String? = null)

@Serializable
data class CommentRequest(val text: String, val parentId: Id? = null, val asOrganizationId: Id? = null)

@Serializable
data class TextRequest(val text: String)

@Serializable
data class ReportRequest(val reason: String, val comment: String? = null)

@Serializable
data class CreateEventRequest(
    val title: String,
    val description: String? = null,
    val sportId: Id? = null,
    val sportName: String? = null,
    val startsAt: String,
    val endsAt: String? = null,
    val city: String? = null,
    val address: String,
    val locationNote: String? = null,
    val level: String? = null,
    val requirements: String? = null,
    val equipment: String? = null,
    val joinMode: String,
    val capacity: Int? = null,
    val organizationId: Id? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val level: String? = null,
    val sportIds: List<Id>? = null,
)

@Serializable
data class CreateOrganizationRequest(
    val name: String,
    val type: String,
    val contacts: String,
    val description: String? = null,
    val sportsDescription: String? = null,
    val city: String? = null,
    val websiteUrl: String? = null,
)

/** PATCH /events/{id}: только изменённые поля, null — «не менять». */
@Serializable
data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val sportId: Id? = null,
    val sportName: String? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val city: String? = null,
    val address: String? = null,
    val locationNote: String? = null,
    val level: String? = null,
    val requirements: String? = null,
    val equipment: String? = null,
    val joinMode: String? = null,
    val capacity: Int? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class UpdateOrganizationRequest(
    val name: String? = null,
    val type: String? = null,
    val contacts: String? = null,
    val description: String? = null,
    val sportsDescription: String? = null,
    val city: String? = null,
    val websiteUrl: String? = null,
)
