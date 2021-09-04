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

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.dykstrom.cet.engine.exception.EngineException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.engine.util.XboardCommand.QUIT;

class EngineProcessImplIT {

    @Test
    void shouldFailRunningCommand() {
        // Given
        final var unloadedProcess = new EngineProcessImpl();

        // When
        final var exception = assertThrows(
                EngineException.class, () -> unloadedProcess.startUp(0, "does-not-exist", null
        ));

        // Then
        assertTrue(exception.getCause().getMessage().contains("does-not-exist"));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldLoadAndQuitEngineOnLinux() throws Exception {
        shouldLoadAndQuitEngineCommon(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldLoadAndQuitEngineOnWindows() throws Exception {
        shouldLoadAndQuitEngineCommon(ENGINE_1_COMMAND_WINDOWS);
    }

    private void shouldLoadAndQuitEngineCommon(final String command) throws Exception {
        // Given
        final var unloadedProcess = new EngineProcessImpl();

        // When
        final var startedProcess = unloadedProcess.startUp(0, command, ENGINE_1_DIRECTORY);
        startedProcess.sendCommand(QUIT);
        startedProcess.process().waitFor(5, TimeUnit.SECONDS);
        final var output = startedProcess.readAllLines();
        startedProcess.shutDown();

        // Then
        assertTrue(output.get(0).startsWith("# Ronja"));
    }
}
