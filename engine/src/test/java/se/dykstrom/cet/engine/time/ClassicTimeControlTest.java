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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static se.dykstrom.cet.engine.util.XboardCommand.LEVEL;

class ClassicTimeControlTest {

    @Test
    void shouldFormatWithoutSeconds() {
        // Given
        final var timeControl = new ClassicTimeControl(40, 60, 0);

        // When & Then
        assertEquals(LEVEL, timeControl.xboardCommand());
        assertArrayEquals(new Object[]{40, "60", 0}, timeControl.parameters());
    }

    @Test
    void shouldFormatWithoutMinutes() {
        // Given
        final var timeControl = new ClassicTimeControl(100, 0, 15);

        // When & Then
        assertEquals(LEVEL, timeControl.xboardCommand());
        assertArrayEquals(new Object[]{100, "0:15", 0}, timeControl.parameters());
    }

    @Test
    void shouldFormatWithBoth() {
        // Given
        final var timeControl = new ClassicTimeControl(20, 1, 30);

        // When & Then
        assertEquals(LEVEL, timeControl.xboardCommand());
        assertArrayEquals(new Object[]{20, "1:30", 0}, timeControl.parameters());
    }

    @Test
    void shouldCreateWithOnlySeconds() {
        // When
        final var timeControl = new ClassicTimeControl(20, 60);

        // Then
        assertEquals(20, timeControl.moves());
        assertEquals(1, timeControl.minutes());
        assertEquals(0, timeControl.seconds());
    }

    @Test
    void shouldNotAllowInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new ClassicTimeControl(0, 5, 0));
        assertThrows(IllegalArgumentException.class, () -> new ClassicTimeControl(40, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ClassicTimeControl(40, 5, -1));
        assertThrows(IllegalArgumentException.class, () -> new ClassicTimeControl(40, 5, 60));
    }
}
