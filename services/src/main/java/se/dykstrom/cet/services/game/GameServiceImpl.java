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
import static se.dykstrom.cet.services.util.BoardUtils.isDrawBy50thMoveRule;
import static se.dykstrom.cet.services.util.ResultUtils.createDrawResult;
import static se.dykstrom.cet.services.util.ResultUtils.createEngineResult;
import static se.dykstrom.cet.services.util.ResultUtils.createIllegalMoveResult;
import static se.dykstrom.cet.services.util.ResultUtils.createTimeoutResult;

public class GameServiceImpl implements GameService {

    private static final System.Logger LOGGER = System.getLogger(GameServiceImpl.class.getName());

    private static final String EXTRA_ENGINE = "EXTRA";

    private final AtomicBoolean playing = new AtomicBoolean(false);

    @Override
    public PlayedGame playGame(final GameConfig gameConfig,
                               final IdlingEngine idlingWhiteEngine,
                               final IdlingEngine idlingBlackEngine) {
        LOGGER.log(INFO, "Starting new game with ''{0}'' as white and ''{1}'' as black.",
                idlingWhiteEngine.myName(), idlingBlackEngine.myName());
        playing.set(true);
        var finalResult = new Result("*", "Stopped");

        // Game state
        final var board = new Board();
        final var moves = new MoveList();

        // Engine states
        final ForcedEngine forcedWhiteEngine = idlingWhiteEngine.start(gameConfig);
        final ForcedEngine forcedBlackEngine = idlingBlackEngine.start(gameConfig);
        ActiveEngine activeWhiteEngine = null;
        ActiveEngine activeBlackEngine = null;
        final IdlingEngine finalIdlingWhiteEngine;
        final IdlingEngine finalIdlingBlackEngine;

        // Chess clocks
        var stoppedWhiteClock = new StoppedChessClock(gameConfig.timeControl());
        var stoppedBlackClock = new StoppedChessClock(gameConfig.timeControl());

        // Give engines some time to start
        ThreadUtils.sleepSilently(100);

        try {
            // First white move
            forcedWhiteEngine.postTime(stoppedWhiteClock.timeLeft(), stoppedBlackClock.timeLeft());
            forcedWhiteEngine.clear();
            var runningWhiteClock = stoppedWhiteClock.start();
            activeWhiteEngine = forcedWhiteEngine.go();
            var whiteMove = activeWhiteEngine.readMove();
            stoppedWhiteClock = runningWhiteClock.stop();
            logMove(whiteMove, board);
            updateGameState(whiteMove, board, moves);

            // First black move
            logMove(whiteMove, board, false);
            forcedBlackEngine.clear();
            forcedBlackEngine.makeMove(whiteMove);
            forcedBlackEngine.postTime(stoppedBlackClock.timeLeft(), stoppedWhiteClock.timeLeft());
            var runningBlackClock = stoppedBlackClock.start();
            activeBlackEngine = forcedBlackEngine.go();
            var blackMove = activeBlackEngine.readMove();
            stoppedBlackClock = runningBlackClock.stop();
            logMove(blackMove, board);
            updateGameState(blackMove, board, moves);

            while (playing.get()) {
                logMove(blackMove, board, true);
                activeWhiteEngine.postTime(stoppedWhiteClock.timeLeft(), stoppedBlackClock.timeLeft());
                runningWhiteClock = stoppedWhiteClock.start();
                whiteMove = activeWhiteEngine.makeAndReadMove(blackMove);
                stoppedWhiteClock = runningWhiteClock.stop();
                logMove(whiteMove, board);
                updateGameState(whiteMove, board, moves);

                logMove(whiteMove, board, false);
                activeBlackEngine.postTime(stoppedBlackClock.timeLeft(), stoppedWhiteClock.timeLeft());
                runningBlackClock = stoppedBlackClock.start();
                blackMove = activeBlackEngine.makeAndReadMove(whiteMove);
                stoppedBlackClock = runningBlackClock.stop();
                logMove(blackMove, board);
                updateGameState(blackMove, board, moves);
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
                    forcedWhiteEngine, activeWhiteEngine,
                    forcedBlackEngine, activeBlackEngine,
                    null, null);
            finalIdlingWhiteEngine = stopEngine(activeWhiteEngine, forcedWhiteEngine);
            finalIdlingBlackEngine = stopEngine(activeBlackEngine, forcedBlackEngine);
        }

        return new PlayedGame(
                gameConfig,
                finalIdlingWhiteEngine,
                finalIdlingBlackEngine,
                GameResult.fromNotation(finalResult.code()),
                finalResult.text(),
                moves,
                null);
    }

    @Override
    public PlayedGame playGameWithExtraEngine(final GameConfig gameConfig,
                                              final IdlingEngine idlingWhiteEngine,
                                              final IdlingEngine idlingBlackEngine,
                                              final IdlingEngine idlingExtraEngine) {
        if (!idlingExtraEngine.features().playOther()) {
            throw new IllegalArgumentException("Extra engine '" + idlingExtraEngine.myName() + "' does not support playother command");
        }

        LOGGER.log(INFO, "Starting new game with ''{0}'' as white and ''{1}'' as black. Using ''{2}'' as extra engine.",
                idlingWhiteEngine.myName(), idlingBlackEngine.myName(), idlingExtraEngine.myName());
        playing.set(true);
        var finalResult = new Result("*", "Stopped");

        // Game state
        final var board = new Board();
        final var moves = new MoveList();
        final var extraMoves = new HashMap<Integer, String>();

        // Engine states
        final ForcedEngine forcedWhiteEngine = idlingWhiteEngine.start(gameConfig);
        final ForcedEngine forcedBlackEngine = idlingBlackEngine.start(gameConfig);
        final ForcedEngine forcedExtraEngine = idlingExtraEngine.start(gameConfig.withBlack(idlingExtraEngine.myName()));
        ActiveEngine activeWhiteEngine = null;
        ActiveEngine activeBlackEngine = null;
        ActiveEngine activeExtraEngine = null;
        final IdlingEngine finalIdlingWhiteEngine;
        final IdlingEngine finalIdlingBlackEngine;

        // Chess clocks
        var stoppedWhiteClock = new StoppedChessClock(gameConfig.timeControl());
        var stoppedBlackClock = new StoppedChessClock(gameConfig.timeControl());

        // Give engines some time to start
        ThreadUtils.sleepSilently(100);

        try {
            // First white move
            forcedWhiteEngine.postTime(stoppedWhiteClock.timeLeft(), stoppedBlackClock.timeLeft());
            forcedWhiteEngine.clear();
            var runningWhiteClock = stoppedWhiteClock.start();
            activeWhiteEngine = forcedWhiteEngine.go();
            var whiteMove = activeWhiteEngine.readMove();
            stoppedWhiteClock = runningWhiteClock.stop();
            logMove(whiteMove, board);
            updateGameState(whiteMove, board, moves);

            // First black move
            logMove(whiteMove, board, false);
            logMove(EXTRA_ENGINE, whiteMove, board, false);
            // Extra engine
            forcedExtraEngine.clear();
            forcedExtraEngine.makeMove(whiteMove);
            forcedExtraEngine.postTime(stoppedBlackClock.timeLeft(), stoppedWhiteClock.timeLeft());
            activeExtraEngine = forcedExtraEngine.go();
            // Black engine
            forcedBlackEngine.clear();
            forcedBlackEngine.makeMove(whiteMove);
            forcedBlackEngine.postTime(stoppedBlackClock.timeLeft(), stoppedWhiteClock.timeLeft());
            var runningBlackClock = stoppedBlackClock.start();
            activeBlackEngine = forcedBlackEngine.go();
            var blackMove = activeBlackEngine.readMove();
            stoppedBlackClock = runningBlackClock.stop();
            logMove(blackMove, board);
            // Extra engine
            var extraMove = activeExtraEngine.readMove();
            logMove(EXTRA_ENGINE, extraMove, board);
            compare(blackMove, extraMove).ifPresent(move -> updateExtraMoves(move, board, moves, extraMoves));
            activeExtraEngine = takeExtraMoveAndForceBlackMove(activeExtraEngine, whiteMove, blackMove);
            // Black engine
            updateGameState(blackMove, board, moves);
            

            while (playing.get()) {
                logMove(blackMove, board, true);
                activeWhiteEngine.postTime(stoppedWhiteClock.timeLeft(), stoppedBlackClock.timeLeft());
                runningWhiteClock = stoppedWhiteClock.start();
                whiteMove = activeWhiteEngine.makeAndReadMove(blackMove);
                stoppedWhiteClock = runningWhiteClock.stop();
                logMove(whiteMove, board);
                updateGameState(whiteMove, board, moves);

                logMove(whiteMove, board, false);
                logMove(EXTRA_ENGINE, whiteMove, board, false);
                // Extra engine
                activeExtraEngine.postTime(stoppedBlackClock.timeLeft(), stoppedWhiteClock.timeLeft());
                activeExtraEngine.makeMove(whiteMove);
                // Black engine
                activeBlackEngine.postTime(stoppedBlackClock.timeLeft(), stoppedWhiteClock.timeLeft());
                runningBlackClock = stoppedBlackClock.start();
                blackMove = activeBlackEngine.makeAndReadMove(whiteMove);
                stoppedBlackClock = runningBlackClock.stop();
                logMove(blackMove, board);
                // Extra engine
                extraMove = activeExtraEngine.readMove();
                logMove(EXTRA_ENGINE, extraMove, board);
                compare(blackMove, extraMove).ifPresent(move -> updateExtraMoves(move, board, moves, extraMoves));
                activeExtraEngine = takeExtraMoveAndForceBlackMove(activeExtraEngine, whiteMove, blackMove);
                // Black engine
                updateGameState(blackMove, board, moves);
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
                    forcedWhiteEngine, activeWhiteEngine,
                    forcedBlackEngine, activeBlackEngine,
                    forcedExtraEngine, activeExtraEngine);

            finalIdlingWhiteEngine = stopEngine(activeWhiteEngine, forcedWhiteEngine);
            finalIdlingBlackEngine = stopEngine(activeBlackEngine, forcedBlackEngine);
        }

        return new PlayedGame(
                gameConfig,
                finalIdlingWhiteEngine,
                finalIdlingBlackEngine,
                GameResult.fromNotation(finalResult.code()),
                finalResult.text(),
                moves,
                extraMoves);
    }

    private ActiveEngine takeExtraMoveAndForceBlackMove(final ActiveEngine activeExtraEngine,
                                                        final String whiteMove,
                                                        final String blackMove) {
        try {
            final ForcedEngine forcedExtraEngine = activeExtraEngine.force();
            forcedExtraEngine.takeBack();
            forcedExtraEngine.makeMove(whiteMove);
            forcedExtraEngine.makeMove(blackMove);
            return forcedExtraEngine.playOther();
        } catch (UnexpectedException e) {
            LOGGER.log(WARNING, "Ignoring unexpected response from extra engine: " + e.response());
            return activeExtraEngine;
        }
    }

    /**
     * Compares the move made by the extra engine to the move made by the black engine,
     * and logs any differences. Returns {@code extraMove} if the moves differ. Otherwise,
     * returns an empty optional.
     */
    private Optional<String> compare(final String blackMove, final String extraMove) {
        if (!extraMove.equals(blackMove)) {
            LOGGER.log(INFO, "Black engine returned move {0} but extra engine returned move {1}", blackMove, extraMove);
            return Optional.of(extraMove);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void stopGame() {
        playing.set(false);
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
            boolean isValid = board.doMove(move, false);
            if (!isValid) {
                final String reason;
                if (board.isMated()) {
                    reason = "checkmate";
                } else {
                    reason = null;
                }
                throw new ChessLibIllegalException(reason, canMove);
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
        LOGGER.log(INFO, "Final result: " + finalResult.code() + " {" + finalResult.text() + "}");
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
