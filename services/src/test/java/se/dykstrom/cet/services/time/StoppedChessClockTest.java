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

package se.dykstrom.cet.services.time;

import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.time.ClassicTimeControl;

import static org.junit.jupiter.api.Assertions.*;

class StoppedChessClockTest {

    @Test
    void shouldStartClock() {
        // Given
        final var timeControl = new ClassicTimeControl(40, 60, 0);

        // When
        final var stoppedClock = new StoppedChessClock(timeControl);
        final var runningChessClock = stoppedClock.start();

        // Then
        assertEquals(timeControl, runningChessClock.timeControl());
        assertEquals(60 * 60 * 1000, runningChessClock.timeLeft());
        assertEquals(1, runningChessClock.moveNumber());
    }
}
