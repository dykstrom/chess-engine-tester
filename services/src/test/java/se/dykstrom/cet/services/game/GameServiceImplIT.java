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

package se.dykstrom.cet.services.game;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.state.CreatedEngine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineProcessImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_2_COMMAND_LINUX;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_2_COMMAND_WINDOWS;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_2_DIRECTORY;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_3_COMMAND_LINUX;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_3_COMMAND_WINDOWS;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_3_DIRECTORY;

@Disabled
class GameServiceImplIT {

    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 0, 20);

    private final GameServiceImpl gameService = new GameServiceImpl();

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldPlayGameOnLinux() throws Exception {
        shouldPlayGame(ENGINE_1_COMMAND_LINUX, ENGINE_2_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldPlayGameOnWindows() throws Exception {
        shouldPlayGame(ENGINE_1_COMMAND_WINDOWS, ENGINE_2_COMMAND_WINDOWS);
    }

    void shouldPlayGame(final String whiteCommand,
                        final String blackCommand) throws Exception {
        final IdlingEngine whiteEngine = loadEngine(1, whiteCommand, ENGINE_1_DIRECTORY);
        final IdlingEngine blackEngine = loadEngine(2, blackCommand, ENGINE_2_DIRECTORY);

        final var gameConfig = new GameConfig(whiteEngine.features().myName(), blackEngine.features().myName(), TIME_CONTROL);

        try {
            PlayedGame playedGame = gameService.playGame(gameConfig, whiteEngine, blackEngine);
            System.out.println("Game result: " + playedGame.result());
            assertEquals(gameConfig, playedGame.gameConfig());
            assertNotNull(playedGame.result());
        } finally {
            unloadEngine(whiteEngine);
            unloadEngine(blackEngine);
        }
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldPlayGameWithExtraEngineOnLinux() throws Exception {
        shouldPlayGameWithExtraEngine(ENGINE_1_COMMAND_LINUX, ENGINE_2_COMMAND_LINUX, ENGINE_3_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldPlayGameWithExtraEngineOnWindows() throws Exception {
        shouldPlayGameWithExtraEngine(ENGINE_1_COMMAND_WINDOWS, ENGINE_2_COMMAND_WINDOWS, ENGINE_3_COMMAND_WINDOWS);
    }

    void shouldPlayGameWithExtraEngine(final String whiteCommand,
                                       final String blackCommand,
                                       final String extraCommand) throws Exception {
        final IdlingEngine whiteEngine = loadEngine(1, whiteCommand, ENGINE_1_DIRECTORY);
        final IdlingEngine blackEngine = loadEngine(2, blackCommand, ENGINE_2_DIRECTORY);
        final IdlingEngine extraEngine = loadEngine(3, extraCommand, ENGINE_3_DIRECTORY);

        final var gameConfig = new GameConfig(whiteEngine.features().myName(), blackEngine.features().myName(), TIME_CONTROL);

        try {
            PlayedGame playedGame = gameService.playGameWithExtraEngine(gameConfig, whiteEngine, blackEngine, extraEngine);
            System.out.println("Game result: " + playedGame.result());
            assertEquals(gameConfig, playedGame.gameConfig());
            assertNotNull(playedGame.result());
            assertNotNull(playedGame.extraMoves());
        } finally {
            unloadEngine(whiteEngine);
            unloadEngine(blackEngine);
            unloadEngine(extraEngine);
        }
    }

    private IdlingEngine loadEngine(int id, String command, File directory) {
        final var createdEngine = new CreatedEngine(new EngineProcessImpl());
        final var configuredEngine = createdEngine.configure(id, command, directory);
        return configuredEngine.load();
    }

    private void unloadEngine(final IdlingEngine idlingEngine) throws Exception {
        final var process = idlingEngine.process().process();
        idlingEngine.unload();
        process.waitFor(5, TimeUnit.SECONDS);
    }
}
