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
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineProcessImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_NAME;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_2_NAME;

class IdlingEngineIT {

    private static final int ID = 17;
    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 1, 0);
    private static final GameConfig GAME_CONFIG = new GameConfig(ENGINE_1_NAME, ENGINE_2_NAME, TIME_CONTROL);

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldStartGameOnLinux() throws Exception {
        shouldStartGameCommon(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldStartGameOnWindows() throws Exception {
        shouldStartGameCommon(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldStartGameCommon(final String command) throws Exception {
        // Given
        final var configuredEngine = new ConfiguredEngine(new EngineConfig(ID, command, ENGINE_1_DIRECTORY), new EngineProcessImpl());
        final var idlingEngine = configuredEngine.load();

        // When
        final var forcedEngine = idlingEngine.start(GAME_CONFIG);

        // Stop and unload again
        final var process = forcedEngine.process().process();
        final var onceAgainIdlingEngine = forcedEngine.stop();
        final var onceAgainConfiguredEngine = onceAgainIdlingEngine.unload();
        process.waitFor(5, TimeUnit.SECONDS);

        // Then
        assertFalse(process.isAlive());
        assertEquals(idlingEngine, onceAgainIdlingEngine);
        assertEquals(configuredEngine, onceAgainConfiguredEngine);
    }
}
