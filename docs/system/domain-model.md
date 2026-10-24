# Key types

| Type | Module | What it is |
|------|--------|------------|
| `Engine` | `engine` | Interface that all engine state records implement. See [engine-state-machine.md](engine-state-machine.md). |
| `EngineProcess` | `engine` | Interface that hides the subprocess I/O. Unit tests mock it. |
| `TimeControl` | `engine` | Interface with two records: `ClassicTimeControl` (moves per period) and `IncrementalTimeControl` (base time plus increment) |
| `GameConfig` | `engine` | Record: white name, black name, `TimeControl`, and an optional `fen` start position |
| `ChessClock` | `services` | Interface with two states: `StoppedChessClock` and `RunningChessClock`. See [chess-clock.md](chess-clock.md). |
| `GameListener` | `services` | Observer with `gameOver(round, startTime, playedGame)`, called after each game. `ProgressBarWriter` (in `cli`) and `PgnFileWriter` implement it. |
| `MatchConfig` | `services` | Record: number of games, `TimeControl`, and an optional `fen` start position |
| `PlayedGame` | `services` | Record: result of one game, with the engines, result, reason, moves, and the third engine's alternative moves |
| `PlayedMatch` | `services` | Record: results and reasons for all games in a match, with the engines |
