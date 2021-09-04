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

import java.util.List;

import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.time.IncrementalTimeControl;
import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.config.GameConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EngineTest {

    private static final int ID = 17;
    private static final String COMMAND = "engine.sh";
    private static final EngineConfig CONFIG = new EngineConfig(ID, COMMAND, null);
    private static final List<String> FEATURE_DONE = List.of("feature done=1");

    private final EngineProcess unloadedProcessMock = mock(EngineProcess.class);
    private final EngineProcess loadedProcessMock = mock(EngineProcess.class);
    private final GameConfig gameConfigMock = mock(GameConfig.class);

    @Test
    void shouldPerformEntireLifeCycle() {
        when(unloadedProcessMock.startUp(ID, COMMAND, null)).thenReturn(loadedProcessMock);
        when(loadedProcessMock.shutDown()).thenReturn(unloadedProcessMock);
        when(loadedProcessMock.readUntil("feature done")).thenReturn(FEATURE_DONE);
        when(gameConfigMock.timeControl()).thenReturn(new IncrementalTimeControl(1, 0, 5));

        final var createdEngine = new CreatedEngine(unloadedProcessMock);
        assertEquals(unloadedProcessMock, createdEngine.process());

        final ConfiguredEngine configuredEngine = createdEngine.configure(ID, COMMAND, null);
        assertEquals(CONFIG, configuredEngine.config());
        assertEquals(unloadedProcessMock, configuredEngine.process());

        final IdlingEngine idlingEngine = configuredEngine.load();
        assertEquals(CONFIG, idlingEngine.engineConfig());
        assertEquals(loadedProcessMock, idlingEngine.process());

        final ForcedEngine forcedEngine = idlingEngine.start(gameConfigMock);
        assertEquals(CONFIG, forcedEngine.engineConfig());
        assertEquals(loadedProcessMock, forcedEngine.process());

        final IdlingEngine onceAgainIdlingEngine = forcedEngine.stop();
        assertEquals(CONFIG, onceAgainIdlingEngine.engineConfig());
        assertEquals(loadedProcessMock, onceAgainIdlingEngine.process());

        final ConfiguredEngine onceAgainConfiguredEngine = onceAgainIdlingEngine.unload();
        assertEquals(CONFIG, onceAgainConfiguredEngine.config());
        assertEquals(unloadedProcessMock, onceAgainConfiguredEngine.process());

        verify(unloadedProcessMock).startUp(ID, COMMAND, null);
        verify(loadedProcessMock).shutDown();
    }
}
