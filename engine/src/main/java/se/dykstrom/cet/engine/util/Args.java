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

import java.util.function.Predicate;

public final class Args {

    private Args() { }

    /**
     * Ensures that the given condition is true, and throws an exception with the given message if not.
     */
    public static void ensure(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures that the given predicate is true when tested on the given value, and returns the value
     * if that is the case. If the predicate is false, this method throws an exception.
     */
    public static <T> T ensure(final T value, final Predicate<T> predicate) {
        if (predicate.test(value)) {
            return value;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
