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

import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.config.GameConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static se.dykstrom.cet.engine.util.XboardCommand.GO;

class ForcedEngineTest {

    private static final EngineConfig CONFIG = new EngineConfig(17, "engine.sh", new File("/tmp"));
    private static final EngineFeatures FEATURES = EngineFeatures.builder().myName("name").name("1").build();

    private final EngineProcess loadedProcessMock = mock(EngineProcess.class);
    private final GameConfig gameConfigMock = mock(GameConfig.class);

    @Test
    void shouldStopEngine() {
        // Given
        final var forcedEngine = new ForcedEngine(CONFIG, FEATURES, gameConfigMock, loadedProcessMock);

        // When
        final IdlingEngine idlingEngine = forcedEngine.stop();

        // Then
        assertEquals(CONFIG, idlingEngine.engineConfig());
        assertEquals(FEATURES, idlingEngine.features());
        assertEquals(loadedProcessMock, idlingEngine.process());
    }

    @Test
    void shouldActivateEngine() {
        // Given
        final var forcedEngine = new ForcedEngine(CONFIG, FEATURES, gameConfigMock, loadedProcessMock);

        // When
        final ActiveEngine activeEngine = forcedEngine.go();

        // Then
        assertEquals(CONFIG, activeEngine.engineConfig());
        assertEquals(FEATURES, activeEngine.features());
        assertEquals(loadedProcessMock, activeEngine.process());
        verify(loadedProcessMock).sendCommand(GO);
    }
}
