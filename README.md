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

## Логирование

Модуль `:core:logging` предоставляет единую точку входа `AppLogger` и Timber backend.
`URentApplication` включает `DebugTree` только для debug-сборок; при инициализации release-сборки
все Timber trees удаляются, поэтому сообщения, query-параметры и тела ответов не записываются.

```kotlin
AppLogger.withTag("Cities").debug("Loaded page %d", page)
AppLogger.error(error, "Failed to load cities")
```

Feature- и network-код не должен импортировать Timber или `android.util.Log`. Прямой импорт
`android.util.Log` дополнительно запрещён правилом Detekt. Подробное сетевое логирование следует
включать только когда `Logging.isDetailedLoggingEnabled` равно `true`.

## Отображение стран

API возвращает двухбуквенный код страны. Экран городов форматирует его в активной системной
локали, а неизвестный код показывает без изменений, чтобы не терять данные. Для кода `XK`,
который фактически встречается в API, явно используются названия `Косово` и `Kosovo`.

## GitHub Actions

[Workflow](.github/workflows/ci.yml) запускается для pull request, push в `main` и вручную.
Повторный запуск для той же ветки отменяет устаревший. Actions закреплены по SHA.
PR могут читать Gradle cache; запись разрешена из `main`.

## Карта и нижняя навигация

Нижняя панель переключает список и карту с сохранением поиска, прокрутки и позиции камеры.
Вкладки используют type-safe AndroidX Navigation Compose. Модуль `:feature:map` независим
от `:feature:cities` и подключается вместе с ним в composition root приложения.

Карта использует MapLibre Native Android 13.4.1 (OpenGL) и OpenFreeMap Liberty.
Регистрация и API-ключ не нужны; атрибуция OpenStreetMap/OpenMapTiles доступна на карте.
Для production предпочтителен 2ГИС Mobile SDK; для тестового задания выбран MapLibre
из-за платного доступа к 2ГИС. OpenFreeMap не предоставляет SLA. Геолокация не запрашивается.

Начальная область — Москва. После остановки камеры и debounce 300 мс выполняется
`GET /api/cities/map`: центр берётся из камеры, радиус в метрах — максимальное
расстояние до четырёх углов видимой области с округлением вверх. Одинаковые параметры
не загружаются повторно; устаревшие запросы отменяются и их ответы игнорируются.
Имена городов отображаются из API одним GeoJSON source и SymbolLayer. Кнопки справа
меняют масштаб. Города накапливаются в памяти ViewModel по ID: при перемещении карты
уже загруженные пины остаются видимыми, а новые ответы дополняют и обновляют их.
Возврат в предыдущую область выполняет свежий запрос без предварительного удаления пинов.
Кэш не сохраняется после завершения ViewModel или процесса.
Текст пинов — Roboto Bold, 12px с межстрочным интервалом 18px и без разрядки.
Roboto загружается отдельно из закреплённой [сборки glyphs](https://github.com/korywka/fonts.pbf/tree/2d1fd482a5afa0a856277148ee0af3cc6a9db0ff/Roboto%20Bold)
на основе fontnik/OpenMapTiles, поскольку OpenFreeMap его не предоставляет. Для загрузки
этого шрифта нужен доступ к `raw.githubusercontent.com`; шрифты подложки остаются на OpenFreeMap. Неизменившиеся данные
не отправляются в GeoJSON source повторно. Ошибка загрузки сохраняет карту и маркеры,
доступен retry.

JVM-тесты карты проверяют геометрию viewport, debounce, устаревшие ответы, retry
и сохранение камеры. Ktor MockEngine проверяет контракт API, mapping и ошибки
без реальной сети.
