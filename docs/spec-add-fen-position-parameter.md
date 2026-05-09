# Spec: Add FEN position parameter and support

## Problem

<!--
What is the current situation, and why is it a problem?
One or two sentences. Avoid proposing solutions here.
-->

There is no way to specify a custom starting position for the chess games. Games can only start
from the starting position, which limits the possibility to test the engines.

## Goal

<!--
What does success look like? What should be true after this work that isn't true now?
-->

The goal of this task is to enable the user to specify a starting position, and make the
chess engines use this position in all games.

* The starting position can be specified as a command line argument.
* The position is validated before starting any game.
* The starting position can allow either WHITE or BLACK to make the first move.
* The position is sent to all chess engines when starting a new game.
* It is actually used by the engines when playing the games.
* The starting position is written to the log file and if possible to the PGN file.
* The new behavior is documented in the README.md including an example.

## Non-goals

<!--
What is explicitly out of scope for this change? Helps prevent scope creep.
-->

## Background

<!--
Any context needed to understand the problem — relevant code, prior decisions, links.
Delete this section if not needed.
-->

The Chess Engine Communication Protocol is specified at https://www.gnu.org/software/xboard/engine-intf.html.

The "setboard" feature is described in sections "setboard (boolean, default 0, recommended 1)"
and "setboard FEN" of the above page.

The FEN format is described at https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation.

The PGN format is described at https://en.wikipedia.org/wiki/Portable_Game_Notation.

## Design

<!--
How will you solve the problem? Describe the approach at a level where a reviewer
could spot flaws without reading code. Include:
- Data structures or records added/changed
- State machine changes (if touching Engine or ChessClock states)
- New classes or interfaces and their responsibilities
- Changes to existing APIs or CLI options
-->

* The starting position should be specified as a command line argument using FEN format.
* The specified position should be validated and an error message printed if invalid
  (see validation of numberOfGames). Possibly the bhlangonijr chess library can be used.
* The starting position should be passed on to the match service when starting the match.
  The class MatchConfig can be used to couple the starting position with other match
  specific data such as the time control.
* The match service methods pass the position on to the game service as part of the
  GameConfig class.
* The game service passes the game config on to the idling engines when starting the game
  where it is finally used when the engine class sends a "setboard" XBoard command to the
  actual engine process.
* The engine configuration (see ConfiguredEngine) must be extended with the "setboard"
  feature that has a default value of 0. If one or both of the engines report that they
  do not support the setboard feature, an error should be printed and no game should be
  started. Compare with how the "playother" feature is checked in GameServiceImpl.
* If the specified FEN position dictates that BLACK is on the move, the game service should
  let BLACK make the first move. The current implementation assumes that WHITE always
  makes the first move.
* The starting position should be included in the PGN file in attribute "FEN".

## Acceptance criteria

<!--
Concrete, testable conditions that define "done". Use checkboxes.
-->

- [ ] All new behaviour is covered by unit tests
- [ ] Units tests should cover the case when BLACK starts the game
- [ ] New behaviour should also be covered by integration tests to prove that the
      application can actually send the FEN string and that the engines receive it
- [ ] Integration tests pass (`mvn clean verify`)
