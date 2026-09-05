# URent test assessment

[Исходное задание](docs/TASK.md) · [Спецификация](docs/PROJECT_SPEC.md)

## Окружение

JDK 25.

## Convention plugins

Общие настройки находятся в `build-logic`:

- `urent.quality` подключает ktlint, Detekt и строгую конфигурацию Lint для Android-модулей.
- `urent.testing` подключает JUnit 4, `kotlinx-coroutines-test`, `orbit-test`, Ktor `MockEngine`
  и JSON content negotiation в `testImplementation`; настраивает JUnit и HTML/XML-отчёты.

Подключение в Android-модуле после Android-плагина:

```kotlin
plugins {
    id("urent.quality")
    id("urent.testing")
}
```

Оба плагина также поддерживают Kotlin JVM-модули. Задача `:<module>:test` добавляется
в команду CI при подключении JVM-модуля.

## GitHub Actions

[Workflow](.github/workflows/ci.yml) запускается для pull request, push в `main` и вручную.
Повторный запуск для той же ветки отменяет устаревший. Actions закреплены по SHA.
PR могут читать Gradle cache; запись разрешена из `main`.
