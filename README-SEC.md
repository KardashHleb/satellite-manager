Gitleaks	Secret Detection	github.com/gitleaks/gitleaks 
Semgrep CLI	SAST code analysis	semgrep.dev or pipx install semgrep
Syft	SBOM file generation	github.com/anchore/syft 
Grype	SCA: scanning the SBOM for CVEs	github.com/anchore/grype 
OWASP ZAP	DAST: dynamic testing	Runs via Docker

Gitleaks
A tool for finding "secrets": passwords, tokens, API keys and private encryption keys accidentally left in the code.How it works: Scans the entire Git commit history using regular expressions (regex) and built-in rules for popular services (AWS, GitHub, Stripe, etc.).Main advantage: Finds secrets even if you removed them from the code in the latest version but they remain in older commits (Git history).When to use: At commit time (via pre-commit hooks) or as the first step in a CI/CD pipeline, so that sensitive data never reaches the cloud at all.

Semgrep CLI (SAST — Static analysis)
A source code scanner that looks for programming errors, logic vulnerabilities and violations of security standards.How it works: Analyzes the code text itself without compiling or running it (like a very advanced linter). It uses readable rules that look like the source code itself rather than complex regular expressions.Main advantage: Extremely fast. Supports more than 30 programming languages and has a huge free library of ready-made community rules.When to use: While writing code (as a VS Code plugin) or on every push/pull request to the repository.

Syft (SBOM generation)A utility for creating an SBOM (Software Bill of Materials), a detailed "passport" or composition of your application.How it works: Scans the project directory or a built Docker image and produces a complete catalog of all libraries, packages and dependencies used (from files such as package.json, requirements.txt, go.mod).Main advantage: Determines the layer structure of Docker images very accurately and produces a structured report in JSON, CycloneDX or SPDX formats.When to use: Right after building the application or Docker image, before sending it for vulnerability checks.

Grype (SCA — Dependency analysis)A vulnerability scanner that works together with Syft. It checks your application's composition for known vulnerabilities (CVEs).How it works: Takes the list of dependencies (or a Docker image directly), matches it against up-to-date vulnerability databases (NVD, GitHub Security Advisories) and finds outdated libraries that have security holes.Main advantage: Integrates perfectly with Syft. Works very fast and clearly highlights which library version is safe and what you need to upgrade to.When to use: In the CI/CD pipeline before deploying to a server, so you do not ship code with old and vulnerable libraries to production.

OWASP ZAP (Zaproxy) is one of the best-known and most respected open-source tools in the world for finding vulnerabilities in web applications (a security scanner)

How they work together (The ideal pipeline)Chained together, they provide full protection:Gitleaks checks that you have not pushed any passwords.Semgrep checks that the code itself has no vulnerabilities (for example, SQL injection).Syft scans the finished project and builds a list of all libraries.Grype checks this list and complains if libraries are outdated.OWASP ZAP tests the already running site by imitating the actions of an attacker.

Before installing any programs on Linux, run:
sudo apt update

Gitleaks
macOS / Linux (via Homebrew):
brew install gitleaks
Linux (direct binary download):
curl -sSL https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_linux_x64.tar.gz | tar -xz
sudo mv gitleaks /usr/local/bin/
Windows: download gitleaks.exe from the Releases page on GitHub and add it to PATH.
Verify the installation:
gitleaks version

Semgrep
Via pip (requires Python 3.8+):
sudo apt install pipx
pipx install semgrep
pipx ensurepath
source ~/.bashrc
macOS / Linux (via Homebrew):
brew install semgrep
Verify the installation:
semgrep --version

Syft and Grype
macOS / Linux:
curl -sSfL https://raw.githubusercontent.com/anchore/syft/main/install.sh | sh -s -- -b /usr/local/bin
curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin
Alternative installation methods can be found here: https://oss.anchore.com/docs/installation/syft/ 
Windows: 
go to github.com/anchore/syft/releases, download syft_windows_amd64.zip, unpack it and add syft.exe to PATH. Or via Scoop:
scoop install syft

Verify the installation:
syft version
grype version

OWASP ZAP (via Docker)
Make sure Docker Desktop is running, then pull the image:
docker pull ghcr.io/zaproxy/zaproxy:stable
Verify:
docker run --rm ghcr.io/zaproxy/zaproxy:stable zap.sh -version


Secret Detection (Gitleaks)
Running a scan
Go to the root directory of your project:
cd /path/to/your/project
Run a full scan of the directory:
gitleaks detect --source . --report-format json --report-path gitleaks-report.json
If the project is under Git, you can also check the entire commit history:
gitleaks git --report-format json --report-path gitleaks-git-report.json .
View the results in the terminal (without saving to a file):
gitleaks detect --source . -v


Gitleaks prints each finding in the following format:
Finding:     ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
Secret:      ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
RuleID:      github-pat
Entropy:     3.87
File:        src/main/resources/application.properties
Line:        12
Commit:      a1b2c3d4...

String entropy: a high value (>3.5) indicates a random token


SAST: static code analysis (Semgrep)
Running a scan
Basic scan with the Java rule set (OWASP Top 10):
semgrep --config=p/java --json --output semgrep-report.json .
Extended scan with several rule sets:
semgrep --config=p/java --config=p/owasp-top-ten --config=p/secrets --json --output semgrep-report.json .
View the results directly in the terminal:
semgrep --config=p/java .

Semgrep prints each finding as follows:
./src/main/java/com/example/UserService.java
  20|    String query = "SELECT * FROM users WHERE id = " + userId;
  [HIGH] java.lang.sql-injection
  Potential SQL injection. Use parameterized queries.
  Details: https://semgrep.dev/r/java.lang.sql-injection



SCA: dependency analysis (Syft + Grype)

Step 1 — Generating an SBOM with Syft
From sources (Maven / Gradle — Syft detects them automatically):
syft dir:. -o cyclonedx-json=sbom.json
Alternative: generate the SBOM from a built JAR or WAR:
mvn package -DskipTests
syft target/your-app.jar -o cyclonedx-json=sbom.json
View the list of components in a readable form:
syft dir:. -o table
An SBOM file in CycloneDX format contains all dependencies, both direct and transitive. It is the transitive dependencies that often turn out to be vulnerable, because developers do not control them directly.

5.2 Step 2 — Scanning the SBOM with Grype
Scan using the generated SBOM file:
grype sbom:sbom.json
With JSON output for detailed analysis:
grype sbom:sbom.json -o json > grype-report.json
Scan directly from the directory (without generating an SBOM first):
grype dir:.
Show only HIGH and CRITICAL vulnerabilities:
grype sbom:sbom.json | grep -E 'High|Critical'

5.3 Understanding Grype output
NAME              INSTALLED  FIXED-IN   TYPE  VULNERABILITY   SEVERITY
log4j-core        2.14.1     2.17.0     java  CVE-2021-44228  Critical
spring-webmvc     5.3.9      5.3.20     java  CVE-2022-22965  High
jackson-databind  2.12.3     2.12.7.1   java  CVE-2022-42003  Medium


docker save satellite-mission-service:1.0.0 -o mission.tar
grype docker-archive:mission.tar

(scanning the archive with grype)