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

package se.dykstrom.cet.engine.state;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineProcessImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_NAME;

class ConfiguredEngineIT {

    private static final int ID = 17;
    private static final boolean USER_MOVE = true;

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldLoadEngineOnLinux() throws Exception {
        shouldLoadEngineCommon(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldLoadEngineOnWindows() throws Exception {
        shouldLoadEngineCommon(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldLoadEngineCommon(final String command) throws Exception {
        // Given
        final var configuredEngine = new ConfiguredEngine(new EngineConfig(ID, command, ENGINE_1_DIRECTORY), new EngineProcessImpl());

        // When
        final var idlingEngine = configuredEngine.load();
        final var name = idlingEngine.features().myName();
        final var userMove = idlingEngine.features().userMove();
        final var process = idlingEngine.process().process();

        // Unload again
        final var onceAgainConfiguredEngine = idlingEngine.unload();
        process.waitFor(5, TimeUnit.SECONDS);

        // Then
        assertEquals(ENGINE_1_NAME, name);
        assertEquals(USER_MOVE, userMove);
        assertFalse(process.isAlive());
        assertEquals(configuredEngine, onceAgainConfiguredEngine);
    }
}
