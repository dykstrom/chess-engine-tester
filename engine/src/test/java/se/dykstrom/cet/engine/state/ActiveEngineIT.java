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
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.dykstrom.cet.engine.parser.Result;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineProcessImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_1_NAME;
import static se.dykstrom.cet.engine.util.TestConfig.ENGINE_2_NAME;

class ActiveEngineIT {

    private static final int ID = 17;
    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 0, 20);
    private static final GameConfig GAME_CONFIG = new GameConfig(ENGINE_1_NAME, ENGINE_2_NAME, TIME_CONTROL);

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldReadMoveEngineIsWhiteOnLinux() throws Exception {
        shouldReadMoveEngineIsWhite(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldReadMoveEngineIsWhiteOnWindows() throws Exception {
        shouldReadMoveEngineIsWhite(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldReadMoveEngineIsWhite(final String command) throws Exception {
        withForcedEngine(command, forcedEngine -> {
            // When
            forcedEngine.clear();
            final var activeEngine = forcedEngine.go();
            final var move = activeEngine.readMove();
            activeEngine.force();

            // Then
            assertRegexMatches("^[a-h][12][a-h][34]$", move); // A first white move
        });
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldReadMoveEngineIsBlackOnLinux() throws Exception {
        shouldReadMoveEngineIsBlack(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldReadMoveEngineIsBlackOnWindows() throws Exception {
        shouldReadMoveEngineIsBlack(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldReadMoveEngineIsBlack(final String command) throws Exception {
        withForcedEngine(command, forcedEngine -> {
            // When
            forcedEngine.clear();
            forcedEngine.makeMove("e2e4");
            final var activeEngine = forcedEngine.go();
            final var move = activeEngine.readMove();
            activeEngine.force();

            // Then
            assertRegexMatches("^[a-h][78][a-h][56]$", move); // A first black move
        });
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldPlayUntilCheckMateOnLinux() throws Exception {
        shouldPlayUntilCheckMate(ENGINE_1_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldPlayUntilCheckMateOnWindows() throws Exception {
        shouldPlayUntilCheckMate(ENGINE_1_COMMAND_WINDOWS);
    }

    void shouldPlayUntilCheckMate(final String command) throws Exception {
        withForcedEngine(command, forcedEngine -> {
            forcedEngine.clear();
            forcedEngine.makeMove("f2f3");
            forcedEngine.makeMove("e7e5");
            forcedEngine.makeMove("g2g4");
            final var activeEngine = forcedEngine.go();
            final var move = activeEngine.readMove();
            assertEquals("d8h4", move);
            final var result = activeEngine.process().read(Result.class);
            assertEquals("0-1", result.code());
            activeEngine.force();
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

    private static void assertRegexMatches(final String regex, final String s) {
        final var matcher = Pattern.compile(regex).matcher(s);
        assertTrue(matcher.matches(), "regex '" + regex + "' does not match string '" + s + "'");
    }
}
