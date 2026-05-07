/*
 * Copyright 2021 Johan Dykström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.dykstrom.cet.services.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.exception.UnexpectedException;
import se.dykstrom.cet.engine.parser.Result;
import se.dykstrom.cet.engine.state.ActiveEngine;
import se.dykstrom.cet.engine.state.ForcedEngine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.services.exception.ChessLibDrawException;
import se.dykstrom.cet.services.exception.ChessLibIllegalException;
import se.dykstrom.cet.services.exception.TimeoutException;
import se.dykstrom.cet.services.time.StoppedChessClock;
import se.dykstrom.cet.services.util.ThreadUtils;

import static com.github.bhlangonijr.chesslib.Side.WHITE;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static se.dykstrom.cet.engine.util.Args.ensure;
import static se.dykstrom.cet.services.util.BoardUtils.isDrawBy50thMoveRule;
import static se.dykstrom.cet.services.util.ResultUtils.createDrawResult;
import static se.dykstrom.cet.services.util.ResultUtils.createEngineResult;
import static se.dykstrom.cet.services.util.ResultUtils.createIllegalMoveResult;
import static se.dykstrom.cet.services.util.ResultUtils.createTimeoutResult;

public class GameServiceImpl implements GameService {

    private static final System.Logger LOGGER = System.getLogger(GameServiceImpl.class.getName());

    private static final String EXTRA_ENGINE = "EXTRA";

    private final AtomicBoolean playing = new AtomicBoolean(false);

    private record SideState(String move, StoppedChessClock clock, ActiveEngine engine) {}

    @Override
    public PlayedGame playGame(final GameConfig gameConfig,
                               final IdlingEngine whiteEngine,
                               final IdlingEngine blackEngine) {
        if (gameConfig.fen() != null) {
            ensure(whiteEngine.features().setboard(), "Engine '%s' does not support setboard command", whiteEngine.myName());
            ensure(blackEngine.features().setboard(), "Engine '%s' does not support setboard command", blackEngine.myName());
        }

        LOGGER.log(INFO, "Starting new game with ''{0}'' as white and ''{1}'' as black.",
                whiteEngine.myName(), blackEngine.myName());
        playing.set(true);
        var finalResult = new Result("*", "Stopped");

        // Game state
        final var board = new Board();
        if (gameConfig.fen() != null) {
            board.loadFromFen(gameConfig.fen());
        }
        final var moves = gameConfig.fen() == null ? new MoveList() : new MoveList(gameConfig.fen());

        // Initial engine states
        final var forcedWhiteEngine = whiteEngine.start(gameConfig);
        final var forcedBlackEngine = blackEngine.start(gameConfig);

        // Initial chess clocks
        final var initialWhiteClock = new StoppedChessClock(gameConfig.timeControl());
        final var initialBlackClock = new StoppedChessClock(gameConfig.timeControl());

        // Per-side state (carry clock, active engine, and last move across iterations)
        SideState ws = null;
        SideState bs = null;

        // Give engines some time to start
        ThreadUtils.sleepSilently(100);

        try {
            if (board.getSideToMove() == WHITE) {
                // White opens
                ws = makeFirstMove(forcedWhiteEngine, initialWhiteClock, initialBlackClock, null);
                logMove(ws.move(), board);
                updateGameState(ws.move(), board, moves);

                // Black responds
                logMove(ws.move(), board, false);
                bs = makeFirstMove(forcedBlackEngine, initialBlackClock, ws.clock(), ws.move());
                logMove(bs.move(), board);
                updateGameState(bs.move(), board, moves);
            } else {
                // Black opens
                bs = makeFirstMove(forcedBlackEngine, initialBlackClock, initialWhiteClock, null);
                logMove(bs.move(), board);
                updateGameState(bs.move(), board, moves);

                // White responds
                logMove(bs.move(), board, true);
                ws = makeFirstMove(forcedWhiteEngine, initialWhiteClock, bs.clock(), bs.move());
                logMove(ws.move(), board);
                updateGameState(ws.move(), board, moves);

                // Black responds to white (sets up bs for the loop)
                logMove(ws.move(), board, false);
                bs = makeNextMove(bs.engine(), bs.clock(), ws.clock(), ws.move());
                logMove(bs.move(), board);
                updateGameState(bs.move(), board, moves);
            }

            while (playing.get()) {
                logMove(bs.move(), board, true);
                ws = makeNextMove(ws.engine(), ws.clock(), bs.clock(), bs.move());
                logMove(ws.move(), board);
                updateGameState(ws.move(), board, moves);

                logMove(ws.move(), board, false);
                bs = makeNextMove(bs.engine(), bs.clock(), ws.clock(), ws.move());
                logMove(bs.move(), board);
                updateGameState(bs.move(), board, moves);
            }
        } catch (UnexpectedException e) {
            LOGGER.log(INFO, "Unexpected response from " + board.getSideToMove() + " engine on move " + board.getMoveCounter() + ": " + e.response());
            finalResult = createEngineResult(board, e.response());
        } catch (ChessLibIllegalException e) {
            LOGGER.log(INFO, "Illegal move " + e.move() + " detected on move " + board.getMoveCounter() + ": " + e.getMessage());
            finalResult = createIllegalMoveResult(board, e.getMessage(), e.move());
        } catch (ChessLibDrawException e) {
            LOGGER.log(INFO, "Draw detected on move " + board.getMoveCounter() + ": " + e.getMessage());
            finalResult = createDrawResult(board, e.getMessage());
        } catch (TimeoutException e) {
            LOGGER.log(INFO, "Timeout from " + board.getSideToMove() + " engine on move " + board.getMoveCounter() + ": " + e.getMessage());
            finalResult = createTimeoutResult(board);
        } finally {
            postFinalResult(finalResult,
                    forcedWhiteEngine, ws != null ? ws.engine() : null,
                    forcedBlackEngine, bs != null ? bs.engine() : null,
                    null, null);
        }

        return new PlayedGame(
                gameConfig,
                stopEngine(ws != null ? ws.engine() : null, forcedWhiteEngine),
                stopEngine(bs != null ? bs.engine() : null, forcedBlackEngine),
                null,
                GameResult.fromNotation(finalResult.code()),
                finalResult.text(),
                moves,
                null);
    }

    @Override
    public PlayedGame playGameWithExtraEngine(final GameConfig gameConfig,
                                              final IdlingEngine whiteEngine,
                                              final IdlingEngine blackEngine,
                                              final IdlingEngine extraEngine) {
        if (gameConfig.fen() != null) {
            ensure(whiteEngine.features().setboard(), "Engine '%s' does not support setboard command", whiteEngine.myName());
            ensure(blackEngine.features().setboard(), "Engine '%s' does not support setboard command", blackEngine.myName());
            ensure(extraEngine.features().setboard(), "Engine '%s' does not support setboard command", extraEngine.myName());
        }
        ensure(extraEngine.features().playOther(), "Extra engine '%s' does not support playother command", extraEngine.myName());

        LOGGER.log(INFO, "Starting new game with ''{0}'' as white and ''{1}'' as black. Using ''{2}'' as extra engine.",
                whiteEngine.myName(), blackEngine.myName(), extraEngine.myName());
        playing.set(true);
        var finalResult = new Result("*", "Stopped");

        // Game state
        final var board = new Board();
        if (gameConfig.fen() != null) {
            board.loadFromFen(gameConfig.fen());
        }
        final var moves = gameConfig.fen() == null ? new MoveList() : new MoveList(gameConfig.fen());
        final var extraMoves = new HashMap<Integer, String>();

        // Initial engine states
        final var forcedWhiteEngine = whiteEngine.start(gameConfig);
        final var forcedBlackEngine = blackEngine.start(gameConfig);
        final var forcedExtraEngine = extraEngine.start(gameConfig.withBlack(extraEngine.myName()));
        ActiveEngine activeExtraEngine = null;

        // Initial chess clocks
        final var initialWhiteClock = new StoppedChessClock(gameConfig.timeControl());
        final var initialBlackClock = new StoppedChessClock(gameConfig.timeControl());

        // Per-side state (carry clock, active engine, and last move across iterations)
        SideState ws = null;
        SideState bs = null;

        // Give engines some time to start
        ThreadUtils.sleepSilently(100);

        try {
            if (board.getSideToMove() == WHITE) {
                // White opens
                ws = makeFirstMove(forcedWhiteEngine, initialWhiteClock, initialBlackClock, null);
                logMove(ws.move(), board);
                updateGameState(ws.move(), board, moves);

                // Extra engine + Black respond to white's move
                logMove(ws.move(), board, false);
                logMove(EXTRA_ENGINE, ws.move(), board, false);
                forcedExtraEngine.postTime(initialBlackClock.timeLeft(), ws.clock().timeLeft());
                forcedExtraEngine.clear();
                forcedExtraEngine.makeMove(ws.move());
                activeExtraEngine = forcedExtraEngine.go();
                bs = makeFirstMove(forcedBlackEngine, initialBlackClock, ws.clock(), ws.move());
                logMove(bs.move(), board);
            } else {
                // Extra engine + Black opens
                forcedExtraEngine.postTime(initialBlackClock.timeLeft(), initialWhiteClock.timeLeft());
                forcedExtraEngine.clear();
                activeExtraEngine = forcedExtraEngine.go();
                bs = makeFirstMove(forcedBlackEngine, initialBlackClock, initialWhiteClock, null);
                logMove(bs.move(), board);

                // Extra engine comparison (board still in pre-blackMove state)
                var extraMove = activeExtraEngine.readMove();
                logMove(EXTRA_ENGINE, extraMove, board);
                compareAndLog(bs.move(), extraMove).ifPresent(move -> updateExtraMoves(move, board, moves, extraMoves));
                activeExtraEngine = resetExtraEngineAndForceBlackMove(activeExtraEngine, gameConfig, bs.move());
                updateGameState(bs.move(), board, moves);

                // White responds
                logMove(bs.move(), board, true);
                ws = makeFirstMove(forcedWhiteEngine, initialWhiteClock, bs.clock(), bs.move());
                logMove(ws.move(), board);
                updateGameState(ws.move(), board, moves);

                // Extra engine + Black respond to white's move (sets up bs for the loop)
                logMove(ws.move(), board, false);
                logMove(EXTRA_ENGINE, ws.move(), board, false);
                activeExtraEngine.postTime(bs.clock().timeLeft(), ws.clock().timeLeft());
                activeExtraEngine.makeMove(ws.move());
                bs = makeNextMove(bs.engine(), bs.clock(), ws.clock(), ws.move());
                logMove(bs.move(), board);
            }

            // Extra engine comparison (board still in pre-blackMove state)
            var extraMove = activeExtraEngine.readMove();
            logMove(EXTRA_ENGINE, extraMove, board);
            compareAndLog(bs.move(), extraMove).ifPresent(move -> updateExtraMoves(move, board, moves, extraMoves));
            activeExtraEngine = takeBackExtraMoveAndForceBlackMove(activeExtraEngine, ws.move(), bs.move());
            updateGameState(bs.move(), board, moves);

            while (playing.get()) {
                // White move
                logMove(bs.move(), board, true);
                ws = makeNextMove(ws.engine(), ws.clock(), bs.clock(), bs.move());
                logMove(ws.move(), board);
                updateGameState(ws.move(), board, moves);

                // Extra engine + Black respond to white's move
                logMove(ws.move(), board, false);
                logMove(EXTRA_ENGINE, ws.move(), board, false);
                activeExtraEngine.postTime(bs.clock().timeLeft(), ws.clock().timeLeft());
                activeExtraEngine.makeMove(ws.move());
                bs = makeNextMove(bs.engine(), bs.clock(), ws.clock(), ws.move());
                logMove(bs.move(), board);

                // Extra engine comparison (board still in pre-blackMove state)
                extraMove = activeExtraEngine.readMove();
                logMove(EXTRA_ENGINE, extraMove, board);
                compareAndLog(bs.move(), extraMove).ifPresent(move -> updateExtraMoves(move, board, moves, extraMoves));
                activeExtraEngine = takeBackExtraMoveAndForceBlackMove(activeExtraEngine, ws.move(), bs.move());
                updateGameState(bs.move(), board, moves);
            }
        } catch (UnexpectedException e) {
            LOGGER.log(INFO, "Unexpected response from " + board.getSideToMove() + " engine on move " + board.getMoveCounter() + ": " + e.response());
            finalResult = createEngineResult(board, e.response());
        } catch (ChessLibIllegalException e) {
            LOGGER.log(INFO, "Illegal move detected on move " + board.getMoveCounter() + ": " + e.getMessage());
            finalResult = createIllegalMoveResult(board, e.getMessage(), e.move());
        } catch (ChessLibDrawException e) {
            LOGGER.log(INFO, "Draw detected on move " + board.getMoveCounter() + ": " + e.getMessage());
            finalResult = createDrawResult(board, e.getMessage());
        } catch (TimeoutException e) {
            LOGGER.log(INFO, "Timeout from " + board.getSideToMove() + " engine on move " + board.getMoveCounter() + ": " + e.getMessage());
            finalResult = createTimeoutResult(board);
        } finally {
            postFinalResult(finalResult,
                    forcedWhiteEngine, ws != null ? ws.engine() : null,
                    forcedBlackEngine, bs != null ? bs.engine() : null,
                    forcedExtraEngine, activeExtraEngine);
        }

        return new PlayedGame(
                gameConfig,
                stopEngine(ws != null ? ws.engine() : null, forcedWhiteEngine),
                stopEngine(bs != null ? bs.engine() : null, forcedBlackEngine),
                stopEngine(activeExtraEngine, forcedExtraEngine),
                GameResult.fromNotation(finalResult.code()),
                finalResult.text(),
                moves,
                extraMoves);
    }

    /**
     * Makes the first move for an engine, transitioning it from forced to active state.
     * If {@code incomingMove} is non-null, the engine receives that move before thinking.
     */
    private SideState makeFirstMove(final ForcedEngine forcedEngine,
                                    final StoppedChessClock myClock,
                                    final StoppedChessClock theirClock,
                                    final String incomingMove) {
        forcedEngine.postTime(myClock.timeLeft(), theirClock.timeLeft());
        forcedEngine.clear();
        if (incomingMove != null) {
            forcedEngine.makeMove(incomingMove);
        }
        final var runningClock = myClock.start();
        final var activeEngine = forcedEngine.go();
        final var move = activeEngine.readMove();
        final var stoppedClock = runningClock.stop();
        return new SideState(move, stoppedClock, activeEngine);
    }

    /**
     * Makes the next move for an already-active engine.
     */
    private SideState makeNextMove(final ActiveEngine activeEngine,
                                   final StoppedChessClock myClock,
                                   final StoppedChessClock theirClock,
                                   final String incomingMove) {
        activeEngine.postTime(myClock.timeLeft(), theirClock.timeLeft());
        final var runningClock = myClock.start();
        final var move = activeEngine.makeAndReadMove(incomingMove);
        final var stoppedClock = runningClock.stop();
        return new SideState(move, stoppedClock, activeEngine);
    }

    /**
     * Resynchronizes the extra engine after its predicted move has been compared to black's actual
     * move. Forces the engine, takes back its predicted move, then replays {@code whiteMove} and
     * {@code blackMove} so the extra engine reflects the real game state before the next white move.
     */
    private ActiveEngine takeBackExtraMoveAndForceBlackMove(final ActiveEngine activeExtraEngine,
                                                            final String whiteMove,
                                                            final String blackMove) {
        try {
            final ForcedEngine forcedExtraEngine = activeExtraEngine.force();
            forcedExtraEngine.takeBack();
            forcedExtraEngine.makeMove(whiteMove);
            forcedExtraEngine.makeMove(blackMove);
            return forcedExtraEngine.playOther();
        } catch (UnexpectedException e) {
            LOGGER.log(WARNING, "Ignoring unexpected response from extra engine: {0}", e.response());
            return activeExtraEngine;
        }
    }

    /**
     * Resynchronizes the extra engine when black opens. Because there is no preceding white move to
     * take back to, the engine is fully stopped and restarted, then {@code blackMove} is replayed so
     * the extra engine reflects the real game state before white's first move.
     */
    private ActiveEngine resetExtraEngineAndForceBlackMove(final ActiveEngine activeExtraEngine,
                                                           final GameConfig gameConfig,
                                                           final String blackMove) {
        try {
            ForcedEngine forcedExtraEngine = activeExtraEngine.force();
            IdlingEngine idlingExtraEngine = forcedExtraEngine.stop();
            forcedExtraEngine = idlingExtraEngine.start(gameConfig.withBlack(idlingExtraEngine.myName()));
            forcedExtraEngine.makeMove(blackMove);
            return forcedExtraEngine.playOther();
        } catch (UnexpectedException e) {
            LOGGER.log(WARNING, "Ignoring unexpected response from extra engine: {0}", e.response());
            return activeExtraEngine;
        }
    }

    /**
     * Compares the move made by the extra engine to the move made by the black engine,
     * and logs any differences. Returns {@code extraMove} if the moves differ. Otherwise,
     * returns an empty optional.
     */
    private Optional<String> compareAndLog(final String blackMove, final String extraMove) {
        if (!extraMove.equals(blackMove)) {
            LOGGER.log(INFO, "Black engine returned move {0} but extra engine returned move {1}", blackMove, extraMove);
            return Optional.of(extraMove);
        } else {
            return Optional.empty();
        }
    }

    private void updateExtraMoves(final String canMove,
                                  final Board board,
                                  final MoveList moves,
                                  final Map<Integer, String> extraMoves) {
        // Convert move to SAN using the move list
        moves.add(new Move(canMove, board.getSideToMove()));
        final var array = moves.toSanArray();
        moves.removeLast();
        extraMoves.put(board.getMoveCounter(), array[array.length - 1]);
    }

    private void updateGameState(final String canMove, final Board board, final MoveList moves) {
        try {
            final var move = new Move(canMove, board.getSideToMove());
            try {
                boolean isValid = board.doMove(move, true);
                if (!isValid) {
                    final String reason;
                    if (board.isMated()) {
                        reason = "checkmate";
                    } else {
                        reason = null;
                    }
                    throw new ChessLibIllegalException(reason, canMove);
                }
            } catch (RuntimeException e) {
                // ChessLib sometimes throws RuntimeException on illegal moves
                throw new ChessLibIllegalException(e.getMessage(), canMove);
            }
            moves.add(move);
            if (board.isDraw()) {
                final String reason;
                if (board.isRepetition()) {
                    reason = "Draw by repetition";
                } else if (board.isInsufficientMaterial()) {
                    reason = "Draw by insufficient material";
                } else if (isDrawBy50thMoveRule(board)) {
                    reason = "Draw by 50th move rule";
                } else if (board.isStaleMate()) {
                    reason = "Draw by stalemate";
                } else {
                    reason = "Unknown, the triggering move was " + canMove;
                }
                throw new ChessLibDrawException(reason);
            }
        } catch (IllegalArgumentException e) {
            throw new ChessLibIllegalException("cannot parse move", canMove);
        }
    }

    /**
     * Logs an incoming (from engine to tester) move.
     */
    private void logMove(final String move, final Board board) {
        logMove(board.getSideToMove().value(), move, board);
    }

    /**
     * Logs an incoming (from engine to tester) move.
     */
    private void logMove(final String source, final String move, final Board board) {
        final var side = board.getSideToMove();
        final var number = board.getMoveCounter();
        final var dots = side == WHITE ? "." : "...";
        LOGGER.log(DEBUG, "{0} -> {1}{2} {3}", source, number, dots, move);
    }

    /**
     * Logs an outgoing (from tester to engine) move.
     */
    private void logMove(final String move, final Board board, final boolean haveAlreadyIncrementedMoveNumber) {
        logMove(board.getSideToMove().value(), move, board, haveAlreadyIncrementedMoveNumber);
    }

    /**
     * Logs an outgoing (from tester to engine) move.
     */
    private void logMove(final String destination, final String move, final Board board, final boolean haveAlreadyIncrementedMoveNumber) {
        final var side = board.getSideToMove();
        final var number = board.getMoveCounter() - (haveAlreadyIncrementedMoveNumber ? 1 : 0);
        final var dots = side == WHITE ? "..." : ".";
        LOGGER.log(DEBUG, "{0} <- {1}{2} {3}", destination, number, dots, move);
    }

    private void postFinalResult(final Result finalResult,
                                 final ForcedEngine forcedWhiteEngine,
                                 final ActiveEngine activeWhiteEngine,
                                 final ForcedEngine forcedBlackEngine,
                                 final ActiveEngine activeBlackEngine,
                                 final ForcedEngine forcedExtraEngine,
                                 final ActiveEngine activeExtraEngine) {
        LOGGER.log(INFO, "Final result: {0} ({1})", finalResult.code(), finalResult.text());
        postResult(finalResult, activeWhiteEngine, forcedWhiteEngine);
        postResult(finalResult, activeBlackEngine, forcedBlackEngine);
        postResult(finalResult, activeExtraEngine, forcedExtraEngine);
    }

    private void postResult(final Result finalResult, final ActiveEngine activeEngine, final ForcedEngine forcedEngine) {
        if (activeEngine != null) {
            activeEngine.postResult(finalResult.code(), finalResult.text());
        } else if (forcedEngine != null) {
            forcedEngine.postResult(finalResult.code(), finalResult.text());
        }
    }

    private IdlingEngine stopEngine(final ActiveEngine activeEngine, final ForcedEngine forcedEngine) {
        if (activeEngine != null) {
            return activeEngine.force().stop();
        } else {
            return forcedEngine.stop();
        }
    }
}
