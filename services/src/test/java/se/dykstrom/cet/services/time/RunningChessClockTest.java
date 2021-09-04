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
import se.dykstrom.cet.engine.time.IncrementalTimeControl;
import se.dykstrom.cet.services.exception.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunningChessClockTest {

    private static final int ONE_HOUR = 60 * 60 * 1000;
    private static final int FIVE_MINUTES = 5 * 60 * 1000;

    @Test
    void shouldStopClock() {
        // Given
        final var timeControl = new ClassicTimeControl(40, 60, 0);

        // When
        // Simulate that clock was started 100 ms ago
        final var runningChessClock = new RunningChessClock(timeControl, 1, ONE_HOUR, System.currentTimeMillis() - 100);
        final var stoppedChessClock = runningChessClock.stop();

        // Then
        assertEquals(timeControl, stoppedChessClock.timeControl());
        // Compare using a margin of 5.0 millis
        assertEquals(1.0 * ONE_HOUR - 100, stoppedChessClock.timeLeft(), 5.0);
    }

    @Test
    void shouldThrowIfStoppedAfterTimeout() {
        // Given
        final var timeControl = new ClassicTimeControl(40, 60, 0);

        // When & Then
        // Simulate that clock was started 100 ms ago
        final var runningChessClock = new RunningChessClock(timeControl, 1, 50, System.currentTimeMillis() - 100);
        assertThrows(TimeoutException.class, runningChessClock::stop);
    }

    @Test
    void shouldGetMoreTimeAfterOnePeriodClassic() {
        // Given
        final var timeControl = new ClassicTimeControl(3, 60, 0);

        // When
        final var runningChessClock = new RunningChessClock(timeControl, 1, ONE_HOUR, System.currentTimeMillis());
        // Stop clock 3 times to pass a time control period
        final var stoppedChessClock = runningChessClock.stop().start().stop().start().stop();

        // Then
        assertEquals(timeControl, stoppedChessClock.timeControl());
        // Compare using a margin of 5.0 millis
        assertEquals(2.0 * ONE_HOUR, stoppedChessClock.timeLeft(), 5.0);
    }

    @Test
    void shouldGetMoreTimeAfterOnePeriodIncremental() {
        // Given
        final var timeControl = new IncrementalTimeControl(5, 0, 10);

        // When
        final var runningChessClock = new RunningChessClock(timeControl, 1, FIVE_MINUTES, System.currentTimeMillis());
        // Stop clock 2 times
        final var stoppedChessClock = runningChessClock.stop().start().stop();

        // Then
        assertEquals(timeControl, stoppedChessClock.timeControl());
        // Compare using a margin of 5.0 millis
        assertEquals(1.0 * FIVE_MINUTES + 2 * 10 * 1000, stoppedChessClock.timeLeft(), 5.0);
    }
}
