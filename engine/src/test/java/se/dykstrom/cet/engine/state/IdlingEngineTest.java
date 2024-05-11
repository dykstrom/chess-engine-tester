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

import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.config.GameConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.dykstrom.cet.engine.util.XboardCommand.COMPUTER;
import static se.dykstrom.cet.engine.util.XboardCommand.FORCE;
import static se.dykstrom.cet.engine.util.XboardCommand.LEVEL;
import static se.dykstrom.cet.engine.util.XboardCommand.NAME;
import static se.dykstrom.cet.engine.util.XboardCommand.NEW;
import static se.dykstrom.cet.engine.util.XboardCommand.QUIT;

class IdlingEngineTest {

    private static final EngineConfig CONFIG = new EngineConfig(17, "engine.sh", null);
    private static final String MY_NAME = "my name";
    private static final String OPPONENT = "name of opponent";
    private static final EngineFeatures FEATURES = EngineFeatures.builder().myName(MY_NAME).name("1").build();
    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40,5, 10);

    private final EngineProcess unloadedProcessMock = mock(EngineProcess.class);
    private final EngineProcess loadedProcessMock = mock(EngineProcess.class);
    private final GameConfig gameConfigMock = mock(GameConfig.class);

    @Test
    void shouldStartEngine() {
        // Given
        final var idlingEngine = new IdlingEngine(CONFIG, FEATURES, loadedProcessMock);
        when(gameConfigMock.timeControl()).thenReturn(TIME_CONTROL);
        when(gameConfigMock.white()).thenReturn(OPPONENT);
        when(gameConfigMock.black()).thenReturn(MY_NAME);

        // When
        final ForcedEngine forcedEngine = idlingEngine.start(gameConfigMock);

        // Then
        assertEquals(CONFIG, forcedEngine.engineConfig());
        assertEquals(FEATURES, forcedEngine.features());
        assertEquals(loadedProcessMock, forcedEngine.process());
        verify(loadedProcessMock).sendCommand(NEW);
        verify(loadedProcessMock).sendCommand(FORCE);
        verify(loadedProcessMock).sendCommand(COMPUTER);
        verify(loadedProcessMock).sendCommand(NAME, OPPONENT);
        verify(loadedProcessMock).sendCommand(LEVEL, 40, "5:10", 0);
    }

    @Test
    void shouldUnloadEngine() {
        // Given
        final var idlingEngine = new IdlingEngine(CONFIG, FEATURES, loadedProcessMock);
        when(loadedProcessMock.shutDown()).thenReturn(unloadedProcessMock);

        // When
        final ConfiguredEngine configuredEngine = idlingEngine.unload();

        // Then
        assertEquals(CONFIG, configuredEngine.config());
        assertEquals(unloadedProcessMock, configuredEngine.process());
        verify(loadedProcessMock).sendCommand(QUIT);
        verify(loadedProcessMock).shutDown();
    }
}
