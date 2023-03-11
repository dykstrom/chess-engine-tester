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

package se.dykstrom.cet.services.match;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import se.dykstrom.cet.engine.state.CreatedEngine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineProcessImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_1_COMMAND_LINUX;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_1_COMMAND_WINDOWS;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_1_DIRECTORY;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_2_COMMAND_LINUX;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_2_COMMAND_WINDOWS;
import static se.dykstrom.cet.services.util.TestConfig.ENGINE_2_DIRECTORY;

@Tag("slow")
class MatchServiceImplIT {

    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 0, 20);
    private static final int NUMBER_OF_GAMES = 2;

    private final MatchServiceImpl matchService = new MatchServiceImpl();

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldPlayMatchOnLinux() throws Exception {
        shouldPlayMatch(ENGINE_1_COMMAND_LINUX, ENGINE_2_COMMAND_LINUX);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldPlayMatchOnWindows() throws Exception {
        shouldPlayMatch(ENGINE_1_COMMAND_WINDOWS, ENGINE_2_COMMAND_WINDOWS);
    }

    void shouldPlayMatch(final String engine1Command, final String engine2Command) throws Exception {
        final IdlingEngine engine1 = loadEngine(1, engine1Command, ENGINE_1_DIRECTORY);
        final IdlingEngine engine2 = loadEngine(2, engine2Command, ENGINE_2_DIRECTORY);

        final var matchConfig = new MatchConfig(NUMBER_OF_GAMES, TIME_CONTROL);
        final var matchCount = new AtomicInteger(0);

        PlayedMatch playedMatch = null;
        try {
            matchService.addGameListener((gameNumber, startTime, playedGame) -> matchCount.incrementAndGet());
            playedMatch = matchService.playMatch(matchConfig, engine1, engine2);
            assertEquals(matchConfig, playedMatch.matchConfig());
            assertEquals(NUMBER_OF_GAMES, playedMatch.results().size());
            assertEquals(NUMBER_OF_GAMES, matchCount.get());
        } finally {
            assert playedMatch != null;
            unloadEngine(playedMatch.engine1());
            unloadEngine(playedMatch.engine2());
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
