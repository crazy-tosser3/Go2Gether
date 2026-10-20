package com.crzto3.go2gether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crzto3.go2gether.data.ApiException
import com.crzto3.go2gether.data.Id
import com.crzto3.go2gether.data.userMessage
import com.crzto3.go2gether.ui.LocalAppState
import com.crzto3.go2gether.ui.Screen
import com.crzto3.go2gether.ui.ScreenScaffold
import com.crzto3.go2gether.ui.block
import com.crzto3.go2gether.ui.components.AppButton
import com.crzto3.go2gether.ui.components.AppCard
import com.crzto3.go2gether.ui.components.AppTextField
import com.crzto3.go2gether.ui.components.Avatar
import com.crzto3.go2gether.ui.components.ButtonVariant
import com.crzto3.go2gether.ui.components.ErrorBlock
import com.crzto3.go2gether.ui.components.Loading
import com.crzto3.go2gether.ui.components.Notice
import com.crzto3.go2gether.ui.rememberLoad
import com.crzto3.go2gether.ui.theme.AppTheme
import com.crzto3.go2gether.ui.theme.Brand
import com.crzto3.go2gether.ui.theme.Radius
import com.crzto3.go2gether.ui.theme.Type
import kotlinx.coroutines.launch

/** Вход: демо-аккаунты одним нажатием и форма email + пароль. [embedded] — вместо закрытого раздела. */
@Composable
fun LoginScreen(embedded: Boolean = false) {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    val accounts = rememberLoad { state.api.demoAccounts() }
    var pendingDemo by remember { mutableStateOf<Id?>(null) }
    var demoError by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    fun done() {
        if (!embedded && state.nav.canGoBack) state.nav.back()
    }

    ScreenScaffold(title = if (embedded) null else "Вход") {
        block {
            AppCard(color = Brand.Accent) {
                Text("ВХОД", style = Type.display, color = Brand.OnBrand)
                Text(
                    "Войдите, чтобы записываться на мероприятия, создавать свои и видеть точный адрес встречи.",
                    style = Type.body, color = Brand.OnBrand,
                )
            }
        }
        block {
            AppCard {
                Text("Быстрый вход", style = Type.bodyBold, color = AppTheme.colors.ink)
                when {
                    accounts.loading -> Text("Загружаем демо-аккаунты…", style = Type.small, color = AppTheme.colors.ink2)
                    accounts.error != null -> ErrorBlock("Не получилось загрузить демо-аккаунты.") { accounts.reload() }
                    else -> groupDemoAccounts(accounts.data.orEmpty()).forEach { (group, list) ->
                      Text(group, style = Type.smallBold, color = AppTheme.colors.ink2)
                      list.forEach { acc ->
                        Row(
                            Modifier.fillMaxWidth().clip(Radius.md).background(AppTheme.colors.field)
                                .clickable(enabled = pendingDemo == null) {
                                    pendingDemo = acc.id
                                    demoError = null
                                    scope.launch {
                                        try {
                                            state.session.loginDemo(acc.id)
                                            done()
                                        } catch (e: Exception) {
                                            demoError = e.userMessage()
                                        } finally {
                                            pendingDemo = null
                                        }
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Avatar(acc.name, acc.id)
                            Column(Modifier.weight(1f)) {
                                Text(acc.name, style = Type.bodyBold, color = AppTheme.colors.ink)
                                Text(
                                    acc.description ?: if (acc.role == "admin") "Администратор" else "Пользователь",
                                    style = Type.small, color = AppTheme.colors.ink2,
                                )
                                DEMO_SCENARIOS[acc.email.lowercase()]?.let {
                                    Text("Проверить: $it", style = Type.small, color = AppTheme.colors.accentInk)
                                }
                            }
                            Text(if (pendingDemo == acc.id) "…" else "Войти", style = Type.smallBold, color = AppTheme.colors.ink)
                        }
                      }
                    }
                }
                demoError?.let { Notice(it, ok = false) }
            }
        }
        block {
            AppCard {
                Text("или по почте", style = Type.bodyBold, color = AppTheme.colors.ink)
                AppTextField(email, { email = it }, "Почта", keyboardType = KeyboardType.Email)
                AppTextField(password, { password = it }, "Пароль", password = true)
                formError?.let { Notice(it, ok = false) }
                AppButton("Войти", loading = submitting, onClick = {
                    formError = null
                    if (email.isBlank() || password.isEmpty()) {
                        formError = "Введите почту и пароль."
                        return@AppButton
                    }
                    submitting = true
                    scope.launch {
                        try {
                            state.session.login(email.trim(), password)
                            done()
                        } catch (e: Exception) {
                            formError = if ((e as? ApiException)?.status == 401) "Неверная почта или пароль." else e.userMessage()
                        } finally {
                            submitting = false
                        }
                    }
                })
                AppButton("Зарегистрироваться", variant = ButtonVariant.Ghost, onClick = { state.nav.push(Screen.Register) })
            }
        }
    }
}

@Composable
fun RegisterScreen() {
    val state = LocalAppState.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    var fields by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Регистрация") {
        block {
            AppCard {
                AppTextField(name, { name = it }, "Имя", error = fields["name"])
                AppTextField(email, { email = it }, "Электронная почта", error = fields["email"], keyboardType = KeyboardType.Email)
                AppTextField(password, { password = it }, "Пароль (не короче 8 символов)", error = fields["password"], password = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        consent, { consent = it },
                        colors = CheckboxDefaults.colors(checkedColor = AppTheme.colors.ink, checkmarkColor = AppTheme.colors.onInk),
                    )
                    Text("Согласен на обработку персональных данных", style = Type.small, color = AppTheme.colors.ink)
                }
                error?.let { Notice(it, ok = false) }
                AppButton("Создать аккаунт", loading = submitting, enabled = consent, onClick = {
                    val local = buildMap {
                        if (name.isBlank()) put("name", "Введите имя")
                        if (!email.contains('@')) put("email", "Проверьте адрес почты")
                        if (password.length < 8) put("password", "Не короче 8 символов")
                    }
                    fields = local
                    error = null
                    if (local.isNotEmpty()) return@AppButton
                    submitting = true
                    scope.launch {
                        try {
                            state.session.register(name.trim(), email.trim(), password)
                            state.nav.select(com.crzto3.go2gether.ui.Tab.Home)
                        } catch (e: Exception) {
                            fields = (e as? ApiException)?.fields.orEmpty()
                            error = e.userMessage()
                        } finally {
                            submitting = false
                        }
                    }
                })
            }
        }
    }
}

// Роль организации API не отдаёт — группу и подсказку берём по почте, как features/auth/demoRoles.ts.
private val DEMO_GROUPS = mapOf(
    "user@demo.local" to "Пользователи", "user2@demo.local" to "Пользователи",
    "org@demo.local" to "Организации", "org2@demo.local" to "Организации",
    "admin@demo.local" to "Администратор",
)

private val DEMO_SCENARIOS = mapOf(
    "user@demo.local" to "С1, С4, С5 — создать мероприятие, принять заявку",
    "user2@demo.local" to "С3, С4, С5 — записаться, задать вопрос",
    "org@demo.local" to "С2 — опубликовать от организации",
    "org2@demo.local" to "С2 — заявка на допуске, публиковать нельзя",
    "admin@demo.local" to "С2, С6 — допуск и модерация",
)

private fun groupDemoAccounts(accounts: List<com.crzto3.go2gether.data.DemoAccount>): List<Pair<String, List<com.crzto3.go2gether.data.DemoAccount>>> {
    val order = listOf("Пользователи", "Организации", "Администратор")
    return accounts
        .groupBy { DEMO_GROUPS[it.email.lowercase()] ?: if (it.role == "admin") "Администратор" else "Пользователи" }
        .toList()
        .sortedBy { order.indexOf(it.first) }
}
