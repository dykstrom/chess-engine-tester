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

public interface TimeControl {

    /**
     * Returns the XBoard command used to set this time control.
     */
    @SuppressWarnings("SameReturnValue")
    XboardCommand xboardCommand();

    /**
     * Returns the XBoard command parameters needed to set this time control.
     */
    Object[] parameters();

    /**
     * Returns the PGN representation of this time control.
     */
    String toPgn();

    /**
     * Returns the initial time in millis.
     */
    long initialTimeInMillis();

    /**
     * Returns the time increment in millis. For classical time control,
     * this is the same as {@link #initialTimeInMillis()}.
     */
    long incrementInMillis();

    /**
     * Returns the number of moves in one time period. For incremental time control,
     * this is always 1.
     */
    int movesInOnePeriod();

    static String format(int minutes, int seconds) {
        return minutes + ((seconds > 0) ? (":" + seconds) : "");
    }
}
