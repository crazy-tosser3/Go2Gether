This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
---

## Go2Gether: мобильные приложения

Android и iOS — одно приложение на Kotlin Multiplatform + Compose Multiplatform; визуал повторяет веб-клиент из [`frontend/`](./frontend).

**Адрес API захардкожен** в [`AppConfig.kt`](./shared/src/commonMain/kotlin/com/crzto3/go2gether/config/AppConfig.kt): `https://hack.crzto3.ru` (+ префикс `/api/v1`).

| Путь | Что |
|---|---|
| `shared/.../config/` | конфиг: адрес сервера, таймауты |
| `shared/.../data/` | модели по `frontend/openapi/backend.json`, Ktor-клиент, сессия |
| `shared/.../ui/` | тема (токены из `frontend/src/assets/main.css`), навигация, экраны |

Вход: бэк отдаёт JWT в cookie `jwt`; приложение сохраняет его (SharedPreferences / NSUserDefaults) и шлёт как `Authorization: Bearer` и cookie.
