# Подписанный релиз

Версия берётся из единственного тега `vMAJOR.MINOR.PATCH` на текущем `HEAD`.
Теги предков не используются. `versionName` — версия без `v`, `versionCode` —
`major * 1_000_000 + minor * 1_000 + patch`: `v1.0.0` → `1.0.0` / `1000000`.
Minor и patch находятся в диапазоне 0–999, итоговый код — 1–2100000000
(ограничение Android). Суффиксы prerelease, build metadata и ведущие нули не принимаются.
Без тега и без `candidateReleaseTag` сборки получают тестовую версию
`0.0.0-release-test` / `1`, включая release с R8 и локальной подписью.
Отсутствие тега не блокирует сборку; некорректный тег или явно заданный кандидат
по-прежнему считается ошибкой.

Release-сборка оптимизируется R8; состав правил и проверка минифицированного APK
описаны в [R8.md](R8.md). Сохраняйте `mapping.txt` вместе с APK каждого выпуска.

Release APK автоматически получает имя `urent_jukov_v<versionName>.apk`:
`urent_jukov_v1.0.0.apk` для `v1.0.0` или
`urent_jukov_v0.0.0-release-test.apk` без тега/кандидата.

## Локальная подпись

Ключ и пароли хранятся только локально в `keys/` основного worktree; каталог
исключён из Git. Для последующих обновлений приложения нужен тот же ключ.
Сохраните резервную копию каталога в защищённом хранилище.

Если properties отсутствует, заполнен не полностью или файл keystore не найден,
release автоматически подписывается стандартным debug-ключом Android. R8 остаётся
включённым, APK не становится debuggable. Неверный пароль или повреждённый найденный
keystore остаются ошибкой подписи. Для опубликованного выпуска передавайте основные
ключи и проверяйте сертификат: APK с debug-подписью нельзя установить как обновление
поверх APK с другой подписью.

Файл `keys/release.properties`:

```properties
storeFile=release.jks
storePassword=<пароль хранилища>
keyAlias=urent
keyPassword=<пароль ключа>
```

`storeFile` разрешается относительно каталога файла properties. В другом worktree
передавайте `-PsigningProperties=/absolute/path/to/main-worktree/keys/release.properties`.
Не передавайте пароли в командной строке и не добавляйте их в репозиторий.

## Подготовка до мерджа

```shell
./gradlew :app:assembleRelease :app:lintRelease checkBuildLogic \
  -PcandidateReleaseTag=v1.0.0 \
  -PsigningProperties=/absolute/path/to/main-worktree/keys/release.properties
```

Для обычной тестовой release-сборки оба параметра можно опустить:
`./gradlew :app:assembleRelease`. Получится тестовая версия с debug-подписью,
если в текущем worktree нет основных ключей.

`candidateReleaseTag` — явная версия только для локальной предварительной проверки
до создания тега. Если тег на HEAD уже есть, кандидат обязан совпадать с ним.
Публикуемый APK собирается без этого параметра, из итогового коммита с тегом.

Изменения версии, подписи и README проходят проверки и независимое ревью,
затем PR вливается в `main`. Все остальные PR, которые должны войти в выпуск,
также должны быть влиты до следующего шага.

## Выпуск после мерджа

Работайте с чистым worktree на актуальном `origin/main`. До создания тега проверьте,
что `v1.0.0` ещё не существует локально и в origin. Не переносите опубликованный тег.

1. Выполните `git fetch origin --tags`, перейдите на итоговый коммит `origin/main`
   и проверьте `git status --porcelain` — вывод должен быть пустым.
2. Выполните проверки CI и предварительную release-сборку командой выше.
3. Создайте локальный тег: `git tag -a v1.0.0 -m 'Release 1.0.0'`.
4. Соберите `./gradlew :app:assembleRelease :app:lintRelease` с путём к
   `signingProperties`, **без** `candidateReleaseTag`. Никаких новых коммитов
   между созданием тега, сборкой и публикацией делать не нужно.
5. Проверьте APK через Android SDK `apksigner verify --verbose --print-certs`
   и `apkanalyzer manifest version-name` / `version-code`: ожидаются
   `1.0.0` / `1000000`. Убедитесь, что `apkanalyzer manifest debuggable` выводит `false`.
6. Скопируйте `app/build/outputs/apk/release/urent_jukov_v1.0.0.apk` в
   `.context/release/urent_jukov_v1.0.0.apk` и сохраните соответствующий `mapping.txt`.
   README ведёт на страницу latest-релиза, где доступен APK с именем и версией.
7. Только когда APK проверен, выполните `git push origin refs/tags/v1.0.0`.
8. Опубликуйте GitHub Release с APK:

   ```shell
   gh release create v1.0.0 .context/release/urent_jukov_v1.0.0.apk \
     --verify-tag --title '1.0.0' --generate-notes --latest
   ```

Проверьте загрузку APK на странице
https://github.com/jukov/urent-test-assestment/releases/latest.
Если публикация Release не удалась после push, повторите публикацию для того же
тега и проверенного APK; не создавайте новый коммит и не переносите тег.
