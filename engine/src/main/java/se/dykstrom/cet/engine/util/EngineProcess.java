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

import java.io.File;
import java.util.List;

import se.dykstrom.cet.engine.parser.Response;

public interface EngineProcess {

    EngineProcess startUp(final int id, final String osCommand, final File directory);

    EngineProcess shutDown();

    /**
     * Returns a reference to the underlying OS {@link Process}.
     */
    Process process();

    /**
     * Sends the given XBoard command, followed by any optional parameters, to the engine.
     */
    void sendCommand(final XboardCommand xboardCommand, final Object... params);

    /**
     * Sends a command that only consists of the given parameters to the engine.
     * This is typically used to send moves without the "usermove" command.
     */
    void sendCommand(final Object... params);

    void checkStatus();

    List<String> readAllLines();

    List<String> readUntil(final String regex);

    <T extends Response> T read(final Class<T> clazz);

    void clearOutput();
}
