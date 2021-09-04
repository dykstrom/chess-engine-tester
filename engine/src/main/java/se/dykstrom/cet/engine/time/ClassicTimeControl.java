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

import se.dykstrom.cet.engine.util.XboardCommand;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static se.dykstrom.cet.engine.util.Args.ensure;
import static se.dykstrom.cet.engine.util.XboardCommand.LEVEL;

public record ClassicTimeControl(int moves, int minutes, int seconds) implements TimeControl {

    public ClassicTimeControl {
        ensure(moves > 0, "moves must be > 0");
        ensure(minutes >= 0, "minutes must be >= 0");
        ensure(seconds >= 0 && seconds < 60, "seconds must be in [0, 59]");
    }

    public ClassicTimeControl(final int moves, final int seconds) {
        this(moves, seconds / 60, seconds % 60);
    }

    @Override
    public XboardCommand xboardCommand() {
        return LEVEL;
    }

    @Override
    public Object[] parameters() {
        return new Object[]{moves, TimeControl.format(minutes, seconds), 0};
    }

    @Override
    public String toPgn() {
        return moves + "/" + (MINUTES.toSeconds(minutes) + seconds);
    }

    @Override
    public long initialTimeInMillis() {
        return MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds);
    }

    @Override
    public long incrementInMillis() {
        return initialTimeInMillis();
    }

    @Override
    public int movesInOnePeriod() {
        return moves;
    }
}
