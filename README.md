# URent test assessment

Android-приложение для поиска городов и просмотра их на карте.
[Исходное задание](docs/TASK.md) · [Спецификация проекта](docs/PROJECT_SPEC.md)

## Возможности

- Список городов с поиском и пагинацией.
- Переключение между списком и картой с сохранением состояния экранов.
- Отображение городов в выбранной области карты и управление масштабом.
- Информация о городе из списка и выбранного пина: название, страна, население.
- Поиск информации о городе в браузере с сообщением, если браузер недоступен.

## Стек

Kotlin, Jetpack Compose, Orbit MVI, Koin, Ktor и AndroidX Navigation Compose.
Проект разделён на `app`, общие модули `core` и функциональные модули `feature`.
Общие настройки сборки и проверок вынесены в convention plugins в `build-logic`.
Логирование построено на Timber и включено только в debug-сборках.

## Карта

Карта работает на MapLibre Native Android 13.4.1 с подложкой OpenFreeMap Liberty
на основе данных OpenStreetMap. Регистрация и API-ключ не требуются.
Города загружаются из API задания по координатам и радиусу видимой области.

Для production-проекта предпочтительным вариантом считаем 2ГИС Mobile SDK.
В тестовом задании выбран MapLibre, поскольку промышленное использование 2ГИС
[требует подписки и ключа доступа](https://docs.2gis.com/android/sdk/start).

## Сборка и проверки

Нужны JDK 25 и Android SDK; путь к SDK задаётся через `ANDROID_HOME` или `local.properties`.

```shell
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug ktlintCheck detekt checkBuildLogic
```

[GitHub Actions](.github/workflows/ci.yml) выполняет проверки для PR и ветки `main`.
