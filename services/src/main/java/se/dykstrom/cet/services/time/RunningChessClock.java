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

import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.services.exception.TimeoutException;

public class RunningChessClock extends AbstractChessClock {

    private final long startTime;

    public RunningChessClock(final TimeControl timeControl, final int moveNumber, final long timeLeft, final long startTime) {
        super(timeControl, moveNumber, timeLeft);
        this.startTime = startTime;
    }

    public StoppedChessClock stop() {
        final long stopTime = System.currentTimeMillis();
        final long elapsedTime = stopTime - startTime;

        long newTimeLeft = timeLeft() - elapsedTime;
        if (newTimeLeft < 0) {
            throw new TimeoutException("Timeout after " + elapsedTime + " ms, time left " + newTimeLeft + " ms");
        }
        if (moveNumber() % timeControl().movesInOnePeriod() == 0) {
            newTimeLeft += timeControl().incrementInMillis();
        }
        return new StoppedChessClock(timeControl(), moveNumber(), newTimeLeft);
    }
}
