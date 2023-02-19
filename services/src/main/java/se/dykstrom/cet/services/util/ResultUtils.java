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

package se.dykstrom.cet.services.util;

import com.github.bhlangonijr.chesslib.Board;
import se.dykstrom.cet.engine.parser.IllegalMove;
import se.dykstrom.cet.engine.parser.InvalidCommand;
import se.dykstrom.cet.engine.parser.Response;
import se.dykstrom.cet.engine.parser.Result;

import static com.github.bhlangonijr.chesslib.Side.WHITE;

public final class ResultUtils {

    private ResultUtils() { }

    /**
     * This method is called when an engine has received an illegal move or invalid command,
     * or when an engine has detected checkmate or draw, which means the move has already been made,
     * and the side to move is not the one to blame.
     * <p>
     * Note that it is uncertain if we ever end up here, because ChessLib will have detected
     * the same problem before the move was actually made.
     */
    public static Result createEngineResult(final Board board, final Response response) {
        final var side = board.getSideToMove();
        if (response instanceof Result result) {
            return createNormalResult(board, result);
        } else if (response instanceof IllegalMove illegalMove) {
            return createIllegalMoveResult(board, illegalMove);
        } else if (response instanceof InvalidCommand invalidCommand) {
            return new Result("*", side + " received an invalid command: " + invalidCommand.text());
        } else {
            throw new IllegalArgumentException("unsupported response: " + response);
        }
    }

    private static Result createNormalResult(final Board board, final Result result) {
        if (result.isMate() && board.isMated()) {
            return result;
        } else if (result.isDraw() && board.isDraw()) {
            return result;
        } else {
            final var claim = result.code();
            final var side = board.getSideToMove();
            final var code = side == WHITE ? "0-1" : "1-0";
            return new Result(code, side + " claimed invalid game over: " + claim);
        }
    }

    private static Result createIllegalMoveResult(final Board board, final IllegalMove illegalMove) {
        final var side = board.getSideToMove();
        final var code = side == WHITE ? "1-0" : "0-1";
        final var builder = new StringBuilder();
        builder.append("Illegal move");
        if (!illegalMove.text().isBlank()) {
            builder.append(" (").append(illegalMove.text()).append(")");
        }
        builder.append(": ").append(illegalMove.move());
        return new Result(code, builder.toString());
    }

    /**
     * This method is called when ChessLib has found an illegal move,
     * which means the move has not yet been made, and the side to move
     * is the one that returned the illegal move.
     */
    public static Result createIllegalMoveResult(final Board board, final String message, final String move) {
        final var side = board.getSideToMove();
        final var code = side == WHITE ? "0-1" : "1-0";
        final var builder = new StringBuilder();
        builder.append("Illegal move");
        if (message != null) {
            builder.append(" (").append(message).append(")");
        }
        builder.append(": ").append(move);
        return new Result(code, builder.toString());
    }

    public static Result createDrawResult(final Board board, final String message) {
        return new Result("1/2-1/2", "Adjudication: " + message);
    }

    /**
     * This method is called when the Tester has detected a timeout,
     * which means the move has not yet been made, and the side to move
     * is the one that timed out.
     */
    public static Result createTimeoutResult(final Board board) {
        final var side = board.getSideToMove();
        final var code = side == WHITE ? "0-1" : "1-0";
        return new Result(code, "Time forfeit");
    }
}
