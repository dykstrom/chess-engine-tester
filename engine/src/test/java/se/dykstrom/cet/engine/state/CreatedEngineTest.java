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
import se.dykstrom.cet.engine.util.EngineProcess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CreatedEngineTest {

    private static final int ID = 17;
    private static final String COMMAND = "engine.sh";
    private static final File DIRECTORY = new File("/tmp");
    private static final EngineConfig CONFIG = new EngineConfig(ID, COMMAND, DIRECTORY);

    private final EngineProcess unloadedProcessMock = mock(EngineProcess.class);

    @Test
    void shouldConfigureEngine() {
        // Given
        final var createdEngine = new CreatedEngine(unloadedProcessMock);

        // When
        final ConfiguredEngine configuredEngine = createdEngine.configure(ID, COMMAND, DIRECTORY);

        // Then
        assertEquals(CONFIG, configuredEngine.config());
        assertEquals(unloadedProcessMock, configuredEngine.process());
    }
}
