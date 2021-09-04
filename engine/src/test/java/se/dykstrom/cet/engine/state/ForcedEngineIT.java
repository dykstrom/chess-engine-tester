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
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.exception.UnexpectedException;
import se.dykstrom.cet.engine.parser.IllegalMove;
import se.dykstrom.cet.engine.parser.InvalidCommand;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineProcessImpl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_NAME;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_2_NAME;

class ForcedEngineIT {

    private static final int ID = 17;
    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 0, 20);
    private static final GameConfig GAME_CONFIG = new GameConfig(ENGINE_1_NAME, ENGINE_2_NAME, TIME_CONTROL);

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldPlayIllegalMoveOnLinux() throws Exception {
        shouldPlayIllegalMove(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldPlayIllegalMoveOnWindows() throws Exception {
        shouldPlayIllegalMove(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldPlayIllegalMove(final String command) throws Exception {
        withForcedEngine(command, forcedEngine -> {
            forcedEngine.clear();
            forcedEngine.makeMove("e2e8");
            final UnexpectedException exception = assertThrows(UnexpectedException.class, forcedEngine::go);
            assertTrue(exception.response() instanceof IllegalMove);
        });
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldSendInvalidCommandOnLinux() throws Exception {
        shouldSendInvalidCommand(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldSendInvalidCommandOnWindows() throws Exception {
        shouldSendInvalidCommand(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldSendInvalidCommand(final String command) throws Exception {
        withForcedEngine(command, forcedEngine -> {
            forcedEngine.clear();
            forcedEngine.process().sendCommand("foo");
            final UnexpectedException exception = assertThrows(UnexpectedException.class, forcedEngine::go);
            // GNU Chess reports IllegalMove instead of InvalidCommand
            assertTrue(exception.response() instanceof InvalidCommand || exception.response() instanceof IllegalMove);
        });
    }

    /**
     * Executes some code in the context of a {@link ForcedEngine}. This method uses the given command
     * to configure, load, and start an engine, and passes the started engine to the given consumer.
     * After the consumer has executed, the engine is stopped and unloaded, regardless of if the consumer
     * succeeded or not.
     */
    public static void withForcedEngine(final String command,
                                        final Consumer<ForcedEngine> consumer) throws Exception {
        final var createdEngine = new CreatedEngine(new EngineProcessImpl());
        final var configuredEngine = createdEngine.configure(ID, command, ENGINE_1_DIRECTORY);
        final var idlingEngine = configuredEngine.load();
        final var forcedEngine = idlingEngine.start(GAME_CONFIG);
        try {
            consumer.accept(forcedEngine);
        } finally {
            final var process = forcedEngine.process().process();
            forcedEngine.stop().unload();
            process.waitFor(5, TimeUnit.SECONDS);
        }
    }
}
