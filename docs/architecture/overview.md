# Architecture overview

chess-engine-tester runs automated matches between chess engines that use the [XBoard protocol](https://www.gnu.org/software/xboard/engine-intf.html). It handles time control, move validation, PGN output, and match results.

## Modules

The project is a multi-module Maven build. The allowed dependencies between modules are rules, listed in [development.md](development.md).

```mermaid
graph TD
    cli["cli<br/>Entry point, argument parsing,<br/>progress display"]
    services["services<br/>Game and match coordination,<br/>chess clock, PGN writing"]
    engine["engine<br/>XBoard protocol, state machine,<br/>time control, subprocess I/O"]

    cli --> services
    cli --> engine
    services --> engine
```

| Module | Responsibility |
|--------|----------------|
| `engine` | Engine lifecycle state machine, XBoard protocol communication, time control definitions |
| `services` | Single games and multi-game matches, chess clock, PGN file output |
| `cli` | Argument parsing with Picocli, wiring of all services, display of progress and results |

## Application flow

`App` loads the engines and registers the game listeners. Then it calls `MatchService` once. For `-n 1`, `App` calls `playSingleGameMatch`, or `playSingleGameMatchWithExtraEngine` when a third engine is given. For more games, `App` calls `playMatch`.

`MatchServiceImpl.playMatch` plays the games in pairs and swaps colors between the two games of a pair. After each game it notifies every `GameListener`. If an engine does not report the XBoard feature `reuse`, `MatchServiceImpl` unloads and loads that engine again before the next game.

```mermaid
sequenceDiagram
    participant User
    participant App
    participant EngineService
    participant MatchService
    participant GameService
    participant Engine
    participant GameListener

    User->>App: cet -1 e1.json -2 e2.json -n 10 -t 40/300
    App->>EngineService: load(e1.json), load(e2.json)
    EngineService-->>App: IdlingEngine × 2
    App->>MatchService: addGameListener(ProgressBarWriter, PgnFileWriter)
    App->>MatchService: playMatch(matchConfig, engine1, engine2)

    loop Each game, colors swapped every other game
        MatchService->>GameService: playGame(gameConfig, white, black)

        loop Each move
            GameService->>Engine: ForcedEngine.go() or makeMove(opponentMove)
            Engine-->>GameService: ActiveEngine.readMove()
        end

        GameService-->>MatchService: PlayedGame
        MatchService->>GameListener: gameOver(round, startTime, playedGame)
        MatchService->>Engine: unload() and load() unless the engine supports reuse
    end

    MatchService-->>App: PlayedMatch
    App-->>User: Match results table
    App->>EngineService: unload(engine1), unload(engine2)
```

## Details

- Engine lifecycle: [../system/engine-state-machine.md](../system/engine-state-machine.md)
- Chess clock: [../system/chess-clock.md](../system/chess-clock.md)
- Key types: [../system/domain-model.md](../system/domain-model.md)
- Third engine: [../system/third-engine.md](../system/third-engine.md)
