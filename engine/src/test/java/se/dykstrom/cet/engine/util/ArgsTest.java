/*
 * Copyright 2026 Johan Dykström
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.dykstrom.cet.engine.util.Args.ensure;

class ArgsTest {

    @Test
    void ensureShouldPassWhenConditionIsTrue() {
        assertDoesNotThrow(() -> ensure(true, "message"));
    }

    @Test
    void ensureShouldThrowWhenConditionIsFalse() {
        assertThrows(IllegalArgumentException.class, () -> ensure(false, "message"));
    }

    @Test
    void ensureShouldFormatMessageWithArgs() {
        var e = assertThrows(IllegalArgumentException.class, () -> ensure(false, "value is '%s' and '%s'", "foo", "bar"));
        assertEquals("value is 'foo' and 'bar'", e.getMessage());
    }

    @Test
    void ensureShouldPassWithNoFormatArgs() {
        assertDoesNotThrow(() -> ensure(true, "plain message"));
    }

    @Test
    void ensureShouldThrowWithNoFormatArgs() {
        var e = assertThrows(IllegalArgumentException.class, () -> ensure(false, "plain message"));
        assertEquals("plain message", e.getMessage());
    }

    @Test
    void ensureValueShouldReturnValueWhenConditionIsTrue() {
        assertEquals(42, ensure(42, true, "message"));
    }

    @Test
    void ensureValueShouldThrowWhenConditionIsFalse() {
        var e = assertThrows(IllegalArgumentException.class, () -> ensure(42, false, "value is invalid"));
        assertEquals("value is invalid", e.getMessage());
    }
}
