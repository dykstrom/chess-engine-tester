# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build and run unit tests
mvn clean test

# Build without tests
mvn clean package -DskipTests

# Run integration tests (requires GNU Chess and Ronja 0.9.0 in ./engines/)
mvn clean verify

# Run integration tests in Docker (no engine dependencies needed)
docker build .

# Run a single test class
mvn test -pl engine -Dtest=ParserTest

# Run a single test method
mvn test -pl services -Dtest=GameServiceImplTest#testPlayGame
```

## Running the Tool

After building, the distribution zip in `cli/target/` contains the executable scripts. From an unpacked distribution:

```bash
./chess-engine-tester -1 engine1.json -2 engine2.json -n 10 -t 40/300
```

Engine config files are JSON with a `command` field and optional `directory`:
```json
{"command": "/usr/bin/gnuchess", "directory": "/tmp"}
```

Time control format: `"40/300"` (40 moves in 300 seconds) or `"5*60+2"` (5 min + 2 sec increment).

## Architecture

**Multi-module Maven project:** `engine` → `services` → `cli`

### Engine Module (XBoard protocol and state machine)

The engine lifecycle is modeled as an **explicit state machine** with immutable record classes:
`CreatedEngine` → `ConfiguredEngine` → `IdlingEngine` → `ForcedEngine` → `ActiveEngine`

Each state class holds a reference to `EngineProcess` (the actual subprocess), which communicates via the XBoard protocol. State transitions return new instances of the next state class. The `Parser` class interprets engine responses into typed objects (`Move`, `Result`, `IllegalMove`, etc.).

Time control is abstracted as `TimeControl` with two implementations: `ClassicTimeControl` (moves/period) and `IncrementalTimeControl` (base + increment).

### Services Module (Game and match coordination)

- **`GameServiceImpl`** drives a single game: alternates engine turns, sends moves via `ForcedEngine`/`ActiveEngine`, manages `ChessClock`, detects game end via chesslib, notifies `GameListener` observers.
- **`MatchServiceImpl`** orchestrates multi-game matches: alternates colors between games, aggregates results into `PlayedMatch`.
- **`EngineServiceImpl`** loads engine configs from JSON files and starts/stops engine processes.
- **`PgnFileWriter`** implements `GameListener` to write PGN output.

### CLI Module (Entry point)

`App.java` uses Picocli for argument parsing. It wires together all services, creates engines, runs the match, and displays a progress bar. The third-engine feature (`-3`) shadows the black engine for logging/comparison purposes without affecting the game outcome.

### Key abstractions

- `Engine` interface — base contract for all engine states
- `EngineProcess` interface — isolates subprocess I/O (enables mocking in tests)
- `GameListener` — observer for game events (used by progress bar and PGN writer)
- `TimeControl` — sealed interface for time management variants
- `ChessClock` — two states: `RunningClock` / `StoppedClock`

## Testing

Unit tests mock `EngineProcess` to avoid needing real chess engines. Integration tests in `*IT.java` files require actual engines in `./engines/`. The Maven Failsafe plugin runs integration tests during `verify`.

## References

- **XBoard Engine Communication Protocol:** https://www.gnu.org/software/xboard/engine-intf.html

## Development Guidelines

- **Unit tests required:** When adding new functionality, always add unit tests to verify it.
- **No new module dependencies:** Do not introduce new dependencies between Maven modules (the existing `engine` → `services` → `cli` order is fixed).
- **Cross-platform:** The application must work on Linux, macOS, and Windows. Avoid OS-specific APIs or path separators.
- **Preserve the state machine:** Do not break the explicit state machine pattern in the Engine implementations (`CreatedEngine` → `ConfiguredEngine` → `IdlingEngine` → `ForcedEngine` → `ActiveEngine`).
- **Plan first:** ALWAYS start in plan mode. Create and display a plan to the user before making any changes.
- **Be concise:** Write concise comments and commit messages. Avoid obvious comments.
