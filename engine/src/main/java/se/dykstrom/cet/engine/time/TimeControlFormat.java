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

package se.dykstrom.cet.engine.time;

import java.text.ParseException;
import java.util.regex.Pattern;

public final class TimeControlFormat {

    private static final Pattern REGEX_CLASSIC = Pattern.compile("^([0-9]+)/([0-9]+)$");
    private static final Pattern REGEX_INCREMENTAL = Pattern.compile("^([0-9]+)\\+([0-9]+)$");

    private TimeControlFormat() { }

    public static TimeControl parse(final String s) throws ParseException {
        final var classicMatcher = REGEX_CLASSIC.matcher(s);
        final var incrementalMatcher = REGEX_INCREMENTAL.matcher(s);

        if (classicMatcher.matches()) {
            final var moves = Integer.parseInt(classicMatcher.group(1));
            final var seconds = Integer.parseInt(classicMatcher.group(2));
            return new ClassicTimeControl(moves, seconds);
        } else if (incrementalMatcher.matches()) {
            final var seconds = Integer.parseInt(classicMatcher.group(1));
            final var increment = Integer.parseInt(classicMatcher.group(2));
            return new IncrementalTimeControl(seconds, increment);
        } else {
            throw new ParseException(s, 0);
        }
    }
}
