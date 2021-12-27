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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PgnUtils {

    private PgnUtils() { }

    public static List<String> formatMoveText(final String[] moves,
                                              final Map<Integer, String> extraMoves) {
        final var lines = new ArrayList<String>();
        final var builder = new StringBuilder();

        for (var index = 0; index < moves.length; index += 2) {
            final var moveNumber = index / 2 + 1;

            // White move
            builder.append(moveNumber).append(". ").append(moves[index]).append(" ");

            // Black move
            if (index + 1 < moves.length) {
                builder.append(moves[index + 1]).append(" ");
            }

            // Possible extra move
            if (extraMoves != null && extraMoves.containsKey(moveNumber)) {
                final var extraMove = extraMoves.get(moveNumber);
                builder.append("{").append(moveNumber).append("... ").append(extraMove).append("} ");
            }

            if (builder.length() > 60) {
                lines.add(builder.toString().strip());
                builder.setLength(0);
            }
        }
        lines.add(builder.toString().strip());

        return lines;
    }

    public static String tag(final String name, final int value) {
        return "[" + name + " \"" + value + "\"]";
    }

    public static String tag(final String name, final String value) {
        return "[" + name + " \"" + value + "\"]";
    }
}
