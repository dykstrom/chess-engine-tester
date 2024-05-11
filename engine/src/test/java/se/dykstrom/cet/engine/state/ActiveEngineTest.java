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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.exception.UnexpectedException;
import se.dykstrom.cet.engine.parser.IllegalMove;
import se.dykstrom.cet.engine.parser.Move;
import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.config.GameConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.dykstrom.cet.engine.util.XboardCommand.FORCE;

class ActiveEngineTest {

    private static final EngineConfig CONFIG = new EngineConfig(17, "engine.sh", new File("/tmp"));
    private static final EngineFeatures FEATURES = EngineFeatures.builder().myName("name").name("1").build();

    private final EngineProcess loadedProcessMock = mock(EngineProcess.class);
    private final GameConfig gameConfigMock = mock(GameConfig.class);

    @Test
    void shouldForceEngine() {
        // Given
        final var activeEngine = new ActiveEngine(CONFIG, FEATURES, gameConfigMock, loadedProcessMock);

        // When
        final var forcedEngine = activeEngine.force();

        // Then
        assertEquals(CONFIG, forcedEngine.engineConfig());
        verify(loadedProcessMock).sendCommand(FORCE);
    }

    @Test
    void engineShouldPlayMoveAsWhite() {
        // Given
        final var activeEngine = new ActiveEngine(CONFIG, FEATURES, gameConfigMock, loadedProcessMock);
        when(loadedProcessMock.read(Move.class)).thenReturn(new Move("e2e4"));

        // When
        final var move = activeEngine.readMove();

        // Then
        assertEquals("e2e4", move);
    }

    @Test
    void engineShouldDetectIllegalMove() {
        // Given
        final var activeEngine = new ActiveEngine(CONFIG, FEATURES, gameConfigMock, loadedProcessMock);
        when(loadedProcessMock.read(Move.class)).thenThrow(new UnexpectedException(new IllegalMove("e1e1", "")));

        // When
        activeEngine.makeMove("e1e1");
        final var exception = assertThrows(UnexpectedException.class, activeEngine::readMove);

        // Then
        Assertions.assertEquals(IllegalMove.class, exception.response().getClass());
    }
}
