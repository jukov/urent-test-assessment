# URent test assessment

Android-приложение для просмотра городов. Сейчас в репозитории стартовый Compose-экран
и инфраструктура сборки, статического анализа и тестирования. Продуктовые сценарии ещё
не реализованы. Требования: [исходное задание](docs/TASK.md) и [спецификация](docs/PROJECT_SPEC.md).

## Окружение и запуск

- JDK 25. Версия также зафиксирована в `gradle/gradle-daemon-jvm.properties`.
- Android SDK: `platform-tools`, `platforms;android-37.0`, `build-tools;36.0.0`.
- Указать SDK через `ANDROID_HOME` или `sdk.dir` в локальном `local.properties`.
- Использовать Gradle Wrapper из репозитория; отдельно устанавливать Gradle не нужно.

```sh
sdkmanager 'platform-tools' 'platforms;android-37.0' 'build-tools;36.0.0'
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Для установки на подключённое устройство
или эмулятор с Android 9+ выполнить `./gradlew installDebug`.

## Проверки

Полная команда, которую запускает CI:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug ktlintCheck detekt checkBuildLogic --continue
```

| Команда | Назначение |
| --- | --- |
| `./gradlew testDebugUnitTest` | Все unit- и интеграционные тесты Android-модулей на JVM |
| `./gradlew :app:testDebugUnitTest --tests '*MainDispatcherRuleTest'` | Один тестовый класс |
| `./gradlew :app:testDebugUnitTest --tests '*.integration.*'` | Только интеграционные тесты приложения |
| `./gradlew lintDebug` | Android Lint, включая тестовые исходники |
| `./gradlew ktlintCheck` | Форматирование Kotlin и Gradle Kotlin DSL |
| `./gradlew ktlintFormat :build-logic:ktlintFormat` | Автоматическое исправление форматирования |
| `./gradlew detekt` | Статический анализ всех исходников в `src`, включая `androidTest` |
| `./gradlew checkBuildLogic` | Тесты Gradle TestKit, проверка плагинов, ktlint и Detekt для `build-logic` |

Lint, ktlint и Detekt останавливают сборку при нарушениях. Baseline не используется.
Правила форматирования находятся в `.editorconfig`, Detekt — в `config/detekt/detekt.yml`,
Android Lint — в `config/lint.xml`. Для Compose разрешены имена функций с заглавной буквы
и длинные списки параметров у `@Composable`. Числовые инициализаторы именованных свойств
допускаются для токенов темы. Остальные стандартные правила Detekt сохранены.

Lint считает предупреждения ошибками. Отключены только уведомления о наличии новых версий
зависимостей и Gradle: публикация библиотеки не должна менять результат проверки того же
коммита. Обновления зависимостей выполняются отдельной задачей.

Используется Detekt `2.0.0-alpha.6`, поддерживающий AGP 9.3.1 и JDK 25
([официальная таблица совместимости](https://detekt.dev/docs/introduction/compatibility/)).
Это предварительная версия; её обновления требуют проверки конфигурации и тестов плагинов.
Версии инструментов и тестовых библиотек закреплены в `gradle/libs.versions.toml`.

## Как писать тесты

Общие настройки находятся в convention plugins внутри `build-logic`:

- `urent.quality` подключает ktlint, Detekt и строгую конфигурацию Lint для Android-модулей.
- `urent.testing` подключает JUnit 4, `kotlinx-coroutines-test`, `orbit-test`, Ktor `MockEngine`
  и JSON content negotiation в `testImplementation`; настраивает JUnit и HTML/XML-отчёты.

Для нового Android-модуля добавить после Android-плагина:

```kotlin
plugins {
    // Android application/library plugin
    id("urent.quality")
    id("urent.testing")
}
```

Оба convention plugin также подходят для JVM-модуля с Kotlin JVM plugin. Его тесты нужно
добавить в команду CI как `:<module>:test`, когда такой модуль появится.

Unit- и интеграционные тесты размещаются в стандартном `src/test/kotlin` или `src/test/java`.
Для интеграционных тестов используется пакет `integration`, а не отдельный Android source set:
они запускаются той же задачей `testDebugUnitTest` и не требуют эмулятора.

- Для coroutine-тестов использовать `runTest` и виртуальное время.
- Для ViewModel, работающих с `Dispatchers.Main`, есть пример `MainDispatcherRule` в тестах `:app`.
  Создавать ViewModel после активации правила; правило сбрасывает dispatcher после теста.
- Для Orbit проверять состояния и side effects через `orbit-test`. Рабочий пример:
  `OrbitInfrastructureTest`.
- Для API/repository-тестов использовать `MockEngine` и локальные JSON fixtures, без реального
  API. Пример запроса, десериализации и HTTP-ошибки: `HttpClientInfrastructureTest`.
- Текущие infrastructure-тесты проверяют доступность и совместимость тестового стека;
  они не заменяют будущие тесты бизнес-логики.

`MainDispatcherRule` пока принадлежит тестам `:app`. При появлении нескольких потребителей
общие test utilities можно вынести отдельно, не добавляя тестовые зависимости в runtime приложения.

Instrumentation- и Compose UI-тесты находятся в `src/androidTest`. **В CI они пока не запускаются.**
Локальная команда при запущенном эмуляторе или подключённом устройстве:

```sh
./gradlew connectedDebugAndroidTest
```

## GitHub Actions и отчёты

Workflow `.github/workflows/ci.yml` запускается для pull request, push в `main` и вручную.
Повторный запуск для той же ветки отменяет устаревший. Используются JDK 25, закреплённые
SDK-пакеты, Gradle cache и проверка wrapper через `setup-gradle`. Actions закреплены по SHA.
Права workflow ограничены чтением репозитория; PR могут читать cache, запись разрешена из `main`.

После запуска доступны артефакты `verification-reports` и, при успехе, `debug-apk` (7 дней).
Отчёты загружаются и при ошибке проверок. Локально:

- `<module>/build/reports/tests/testDebugUnitTest/index.html` и `build/test-results/` — JVM-тесты;
- `<module>/build/reports/lint-results-debug.html` — Android Lint;
- `<module>/build/reports/ktlint/` — ktlint;
- `<module>/build/reports/detekt/` — Detekt;
- `build-logic/build/reports/` — проверки convention plugins.

Для запрета слияния неуспешных PR администратор репозитория может сделать проверку
`Build, JVM tests and static analysis` обязательной в правилах ветки `main`.
