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

import static java.util.Objects.requireNonNull;
import static se.dykstrom.cet.engine.util.Args.ensure;

public abstract class AbstractChessClock implements ChessClock {

    private final TimeControl timeControl;
    private final int moveNumber;
    private final long timeLeft;

    protected AbstractChessClock(final TimeControl timeControl, final int moveNumber, final long timeLeft) {
        this.timeControl = requireNonNull(timeControl);
        this.moveNumber = ensure(moveNumber, m -> m >= 0);
        this.timeLeft = ensure(timeLeft, t -> t >= 0);
    }

    @Override
    public TimeControl timeControl() {
        return timeControl;
    }

    @Override
    public int moveNumber() {
        return moveNumber;
    }

    @Override
    public long timeLeft() {
        return timeLeft;
    }
}
