# Third engine

The `-3 engine3.json` option adds a third engine that shadows the engine playing black. The third engine thinks about the same positions as black, but its moves never affect the game.

## How it works

- The third engine only works in a single-game match (`-n 1`). `App` then calls `MatchService.playSingleGameMatchWithExtraEngine`, which calls `GameService.playGameWithExtraEngine`.
- The third engine must support the XBoard command `playother`. `GameServiceImpl` also uses `remove` to take back the third engine's move. With a FEN start position, the third engine must also support `setboard`.
- After each black move, `GameServiceImpl` reads the third engine's move and compares the two. When they differ, it logs the difference at level `INFO`. It then resets the third engine so it continues from black's actual move.
- `PlayedGame.extraMoves` holds the alternative moves. `PgnFileWriter` writes each one as a comment after black's move, for example `1. d4 e6 {1... Nf6}`.
