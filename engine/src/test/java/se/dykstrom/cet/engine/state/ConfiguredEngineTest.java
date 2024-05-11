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

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.dykstrom.cet.engine.util.XboardCommand.FORCE;
import static se.dykstrom.cet.engine.util.XboardCommand.PROTOVER;
import static se.dykstrom.cet.engine.util.XboardCommand.XBOARD;

class ConfiguredEngineTest {

    private static final int ID = 17;
    private static final String OS_COMMAND = "engine.sh";
    private static final File DIRECTORY = new File("/tmp");
    private static final EngineConfig CONFIG = new EngineConfig(ID, OS_COMMAND, DIRECTORY);
    private static final EngineFeatures FEATURES_FOO = EngineFeatures.builder().myName("foo").userMove("1").build();
    private static final EngineFeatures FEATURES_BAR = EngineFeatures.builder().myName("bar").userMove("1").build();

    private static final String FEATURE_DONE_0 = "feature done=0";
    private static final String FEATURE_DONE_1 = "feature done=1";
    private static final String FEATURE_MY_NAME_FOO = "feature myname=foo";
    private static final String FEATURE_MY_NAME_BAR = "feature myname=bar";
    private static final String FEATURE_USER_MOVE = "feature usermove=1";

    private static final List<String> FEATURE_RESPONSE_NORMAL = List.of(FEATURE_MY_NAME_FOO, FEATURE_USER_MOVE, FEATURE_DONE_1);
    private static final List<String> FEATURE_RESPONSE_DELAYED = List.of(FEATURE_DONE_0, FEATURE_MY_NAME_BAR, FEATURE_USER_MOVE, FEATURE_DONE_1);

    private final EngineProcess unloadedProcessMock = mock(EngineProcess.class);
    private final EngineProcess loadedProcessMock = mock(EngineProcess.class);

    @Test
    void shouldLoadEngine() {
        // Given
        final var configuredEngine = new ConfiguredEngine(CONFIG, unloadedProcessMock);
        when(unloadedProcessMock.startUp(ID, OS_COMMAND, DIRECTORY)).thenReturn(loadedProcessMock);
        when(loadedProcessMock.readUntil(FEATURE_DONE_1)).thenReturn(FEATURE_RESPONSE_NORMAL);

        // When
        final IdlingEngine idlingEngine = configuredEngine.load();

        // Then
        assertEquals(CONFIG, idlingEngine.engineConfig());
        assertEquals(FEATURES_FOO, idlingEngine.features());
        assertEquals(loadedProcessMock, idlingEngine.process());
        verify(unloadedProcessMock).startUp(ID, OS_COMMAND, DIRECTORY);
        verify(loadedProcessMock).sendCommand(XBOARD);
        verify(loadedProcessMock).sendCommand(PROTOVER, 2);
        verify(loadedProcessMock).sendCommand(FORCE);
        verify(loadedProcessMock).readUntil(FEATURE_DONE_1);
    }

    @Test
    void shouldLoadEngineWithFeatureDone0() {
        // Given
        final var configuredEngine = new ConfiguredEngine(CONFIG, unloadedProcessMock);
        when(unloadedProcessMock.startUp(ID, OS_COMMAND, DIRECTORY)).thenReturn(loadedProcessMock);
        when(loadedProcessMock.readUntil(FEATURE_DONE_1)).thenReturn(FEATURE_RESPONSE_DELAYED);

        // When
        final IdlingEngine idlingEngine = configuredEngine.load();

        // Then
        assertEquals(CONFIG, idlingEngine.engineConfig());
        assertEquals(FEATURES_BAR, idlingEngine.features());
        assertEquals(loadedProcessMock, idlingEngine.process());
        verify(unloadedProcessMock).startUp(ID, OS_COMMAND, DIRECTORY);
        verify(loadedProcessMock).sendCommand(XBOARD);
        verify(loadedProcessMock).sendCommand(PROTOVER, 2);
        verify(loadedProcessMock).sendCommand(FORCE);
        verify(loadedProcessMock).readUntil(FEATURE_DONE_1);
    }
}
