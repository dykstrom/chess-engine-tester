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

public final class Args {

    private Args() { }

    /**
     * Ensures that the given condition is true, and throws an exception with the formatted message if not.
     * The message may contain format specifiers as defined by {@link String#format}.
     */
    public static void ensure(final boolean condition, final String message, final Object... args) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    /**
     * Ensures that the given condition is true, and returns the value if that is the case.
     * If the condition is false, this method throws an exception with the given message.
     */
    public static <T> T ensure(final T value, final boolean condition, final String message) {
        if (condition) {
            return value;
        } else {
            throw new IllegalArgumentException(message);
        }
    }
}
