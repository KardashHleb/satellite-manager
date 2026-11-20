Gitleaks	Secret Detection	github.com/gitleaks/gitleaks 
Semgrep CLI	SAST-анализ кода	semgrep.dev или pipx install semgrep
Syft	Генерация SBOM-файла	github.com/anchore/syft 
Grype	SCA: сканирование SBOM на CVE	github.com/anchore/grype 
OWASP ZAP	DAST: динамическое тестирование	Запускается через Docker

Gitleaks
Инструмент для поиска «секретов» — случайно забытых в коде паролей, токенов, API-ключей и приватных ключей шифрования.Как работает: Сканирует всю историю коммитов Git с помощью регулярных выражений (regex) и встроенных правил для популярных сервисов (AWS, GitHub, Stripe и др.).Главный плюс: Находит секреты, даже если вы их удалили из кода в последней версии, но они остались в старых коммитах (истории Git).Когда использовать: На этапе коммита (через pre-commit хуки) или как первый шаг в CI/CD пайплайне, чтобы опасные данные вообще не попали в облако.

Semgrep CLI (SAST — Статический анализ)
Сканер исходного кода на наличие ошибок программирования, логических уязвимостей и нарушений стандартов безопасности.Как работает: Анализирует сам текст кода без его компиляции или запуска (как очень продвинутый линтер). Использует понятные правила, похожие на сам исходный код, а не сложные регулярные выражения.Главный плюс: Невероятно быстрый. Поддерживает более 30 языков программирования и имеет огромную бесплатную базу готовых правил от сообщества.Когда использовать: Во время написания кода (как плагин для VS Code) или при каждом пуше/пулл-реквесте в репозиторий.

Syft (Генерация SBOM)Утилита для создания SBOM (Software Bill of Materials) — подробного «паспорта» или состава вашего приложения.Как работает: Сканирует директорию с проектом или готовый Docker-образ и составляет полный каталог всех использованных библиотек, пакетов и зависимостей (из файлов вроде package.json, requirements.txt, go.mod).Главный плюс: Очень точно определяет структуру слоев в Docker-образах и выдает структурированный отчет в форматах JSON, CycloneDX или SPDX.Когда использовать: Сразу после сборки приложения или Docker-образа, перед отправкой на проверку уязвимостей.

Grype (SCA — Анализ зависимостей)Сканер уязвимостей, который работает в связке с Syft. Он проверяет состав вашего приложения на наличие известных уязвимостей (CVE).Как работает: Берет список зависимостей (или напрямую Docker-образ), сверяет его со свежими базами данных уязвимостей (NVD, GitHub Security Advisories) и находит устаревшие библиотеки, в которых есть дыры.Главный плюс: Идеально интегрирован с Syft. Работает молниеносно и четко подсвечивает, какая именно версия библиотеки безопасна и на что нужно обновиться.Когда использовать: В CI/CD пайплайне перед деплоем на сервер, чтобы не выкатить в продакшн код со старыми и уязвимыми библиотеками.

OWASP ZAP (Zaproxy) — это один из самых известных и авторитетных в мире инструментов с открытым исходным кодом для поиска уязвимостей в веб-приложениях (сканер безопасности)

Как они работают вместе (Идеальный пайплайн)Если объединить их по цепочке, получится полноценная защита:Gitleaks проверяет, что вы не залили пароли.Semgrep проверяет, что в самом коде нет уязвимостей (например, SQL-инъекций).Syft сканирует готовый проект и делает список всех библиотек.Grype проверяет этот список и ругается, если библиотеки устарели.OWASP ZAP тестирует уже запущенный сайт, имитируя действия хакера.

Перед установкой любых программ на Linux выполните:
sudo apt update

Gitleaks
macOS / Linux (через Homebrew):
brew install gitleaks
Linux (прямая загрузка бинарника):
curl -sSL https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_linux_x64.tar.gz | tar -xz
sudo mv gitleaks /usr/local/bin/
Windows: скачайте gitleaks.exe со страницы Releases на GitHub и добавьте в PATH.
Проверка установки:
gitleaks version

Semgrep
Через pip (требуется Python 3.8+):
sudo apt install pipx
pipx install semgrep
pipx ensurepath
source ~/.bashrc
macOS / Linux (через Homebrew):
brew install semgrep
Проверка установки:
semgrep --version

Syft и Grype
macOS / Linux:
curl -sSfL https://raw.githubusercontent.com/anchore/syft/main/install.sh | sh -s -- -b /usr/local/bin
curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin
Альтернативные пути установки можно найти здесь: https://oss.anchore.com/docs/installation/syft/ 
Windows: 
перейдите на github.com/anchore/syft/releases, скачайте syft_windows_amd64.zip, распакуйте и добавьте syft.exe в PATH. Либо через Scoop:
scoop install syft

Проверка установки:
syft version
grype version

OWASP ZAP (через Docker)
Убедитесь, что Docker Desktop запущен, затем скачайте образ:
docker pull ghcr.io/zaproxy/zaproxy:stable
Проверка:
docker run --rm ghcr.io/zaproxy/zaproxy:stable zap.sh -version


Secret Detection (Gitleaks)
Запуск сканирования
Перейдите в корневую директорию вашего проекта:
cd /путь/к/вашему/проекту
Запустите полное сканирование директории:
gitleaks detect --source . --report-format json --report-path gitleaks-report.json
Если проект под управлением Git, можно также проверить всю историю коммитов:
gitleaks git --report-format json --report-path gitleaks-git-report.json .
Просмотр результатов в терминале (без сохранения в файл):
gitleaks detect --source . -v


Gitleaks выводит каждую находку в следующем формате:
Finding:     ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
Secret:      ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
RuleID:      github-pat
Entropy:     3.87
File:        src/main/resources/application.properties
Line:        12
Commit:      a1b2c3d4...

Энтропия строки — высокое значение (>3.5) указывает на случайный токен


SAST: статический анализ кода (Semgrep)
Запуск сканирования
Базовое сканирование с набором правил для Java (OWASP Top 10):
semgrep --config=p/java --json --output semgrep-report.json .
Расширенное сканирование с несколькими наборами правил:
semgrep --config=p/java --config=p/owasp-top-ten --config=p/secrets --json --output semgrep-report.json .
Просмотр результатов прямо в терминале:
semgrep --config=p/java .

Semgrep выводит каждую находку в следующем виде:
./src/main/java/com/example/UserService.java
  20|    String query = "SELECT * FROM users WHERE id = " + userId;
  [HIGH] java.lang.sql-injection
  Potential SQL injection. Use parameterized queries.
  Details: https://semgrep.dev/r/java.lang.sql-injection



SCA: анализ зависимостей (Syft + Grype)

Шаг 1 — Генерация SBOM с помощью Syft
Из исходников (Maven / Gradle — Syft определяет автоматически):
syft dir:. -o cyclonedx-json=sbom.json
Альтернатива — сгенерировать SBOM из собранного JAR или WAR:
mvn package -DskipTests
syft target/your-app.jar -o cyclonedx-json=sbom.json
Посмотреть список компонентов в удобном виде:
syft dir:. -o table
SBOM-файл в формате CycloneDX содержит все зависимости: прямые и транзитивные. Именно транзитивные зависимости часто оказываются уязвимыми, так как разработчики не контролируют их напрямую.

5.2 Шаг 2 — Сканирование SBOM с помощью Grype
Сканирование по сгенерированному SBOM-файлу:
grype sbom:sbom.json
С выводом в JSON для подробного анализа:
grype sbom:sbom.json -o json > grype-report.json
Сканирование прямо из директории (без предварительной генерации SBOM):
grype dir:.
Показать только HIGH и CRITICAL уязвимости:
grype sbom:sbom.json | grep -E 'High|Critical'

5.3 Понимание вывода Grype
NAME              INSTALLED  FIXED-IN   TYPE  VULNERABILITY   SEVERITY
log4j-core        2.14.1     2.17.0     java  CVE-2021-44228  Critical
spring-webmvc     5.3.9      5.3.20     java  CVE-2022-22965  High
jackson-databind  2.12.3     2.12.7.1   java  CVE-2022-42003  Medium


docker save satellite-mission-service:1.0.0 -o mission.tar
grype docker-archive:mission.tar

(сканирование архива с помощью grype)