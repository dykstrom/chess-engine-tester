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

package se.dykstrom.cet.engine.util;

import java.util.regex.Pattern;

public final class StringUtils {

    public static final String EOL = System.lineSeparator();

    private static final Pattern REGEX_STRING = Pattern.compile("^\"(.*)\"$");

    private StringUtils() { }

    public static String unstringify(String s) {
        final var matcher = REGEX_STRING.matcher(s);
        return matcher.matches() ? matcher.group(1) : s;
    }

    /**
     * Extracts the chess engine name from the given command.
     */
    public static String getNameFromCommand(final String command) {
        int indexOfLastSlash = command.lastIndexOf("/");
        if (indexOfLastSlash == -1) {
            indexOfLastSlash = command.lastIndexOf("\\");
        }
        int indexOfLastDot = command.lastIndexOf(".");
        if (indexOfLastSlash < indexOfLastDot) {
            return command.substring(indexOfLastSlash + 1, indexOfLastDot);
        } else {
            return command.substring(indexOfLastSlash + 1);
        }
    }
}
