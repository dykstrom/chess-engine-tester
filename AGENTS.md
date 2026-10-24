# AGENTS.md

## What is this

chess-engine-tester is a command-line tool that tests XBoard chess engines by letting them play matches against each other. It can also run a third engine that shadows the engine playing black. The tool logs and compares that engine's moves but never plays them. The tool is for engine developers who want to compare engines or engine versions without a GUI like XBoard or Arena.

## Stack

| Piece | Choice |
|-------|--------|
| Language / runtime | Java (JDK 25 or later) |
| Build | System Maven (`mvn`), multi-module: `engine`, `services`, `cli` |
| Chess rules | chesslib, for move validation and board state |
| CLI | Picocli |
| Config files | Jackson, to read the JSON engine config files |
| Logging | `java.util.logging`, configured in `logging.properties` |
| Testing | JUnit 5 and Mockito. Failsafe runs the `*IT.java` integration tests |
| Engine protocol | XBoard, with the engines as subprocesses |

## Directory index

| Path | What's there |
|------|-------------|
| `engine/` | XBoard protocol and the engine state machine. Packages: `state`, `parser`, `time`, `config`, `util` (subprocess I/O), `exception` |
| `services/` | Game and match coordination. Packages: `game`, `match`, `time` (chess clock), `engine` (loads configs, starts engines), `io`, `util` (PGN writer) |
| `cli/` | Picocli entry point `se.dykstrom.cet.cli.app.App` |
| `cli/src/main/scripts/` | Launch scripts `cet` and `cet.bat` that go into the distribution zip |
| `cli/src/test/resources/` | Engine config files for tests and local runs. They point into `../engines/` |
| `engines/` | Installed chess engines for integration tests and local matches. Git ignores it. It can be a symlink to engines installed elsewhere, or a real folder. Integration tests need GNU Chess on the `PATH` and Ronja 0.9.0 in `engines/ronja-0.9.0/` |
| `docs/architecture/overview.md` | Architecture overview: modules and application flow, with links to the detail files in `docs/system/` |
| `docs/` | Durable project context. Sub-folder layout below shows where each kind of doc goes. |

```
docs/
├── system/         ← what the code does today (updated as code changes)
├── architecture/   ← what the system must do (updated when rules change)
├── adr/            ← architecture decisions (immutable once shipped)
├── reference/      ← long-form rationale (append-only)
└── working-notes/  ← research; NOT authoritative — rules live in architecture/ + adr/
```

## Commands

| What | Command |
|------|---------|
| Unit tests | `mvn clean test` |
| Build the distribution zip, no tests | `mvn clean package -DskipTests`. The zip lands in `cli/target/chess-engine-tester-<version>.zip` |
| Install modules locally | `mvn clean install -DskipTests`. Run this before a single-module test, so `-pl` uses the current `engine` and `services` instead of stale copies in `~/.m2` |
| Single test class or method | `mvn test -pl services -Dtest=GameServiceImplTest#testPlayGame` |
| Integration tests | `mvn clean verify`. Needs GNU Chess on the `PATH` and Ronja 0.9.0 in `engines/ronja-0.9.0/` |
| Include slow integration tests | `mvn clean verify -P slow-tests`. Without this profile, Failsafe skips tests tagged `@Tag("slow")` |
| Integration tests in Docker | `docker build .`. Needs Docker BuildKit. Always uses the `slow-tests` profile and installs its own engines |

## Gotchas

- The test engine configs and `TestConfig.java` use paths like `../engines/ronja-0.9.0`, relative to each module's folder. Integration tests and local matches fail if `engines/` is missing from the repo root or points to the wrong place.

## Working rules

- Always start in plan mode. Show the developer a plan before you make any changes.
- Ask before you commit. Never commit changes automatically.
- Keep comments and commit messages concise. Leave out obvious comments.
- Before you change code, read the code rules in `docs/architecture/development.md`.
