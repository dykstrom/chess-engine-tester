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

package se.dykstrom.cet.engine.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.parser.Move;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.dykstrom.cet.engine.util.StringUtils.EOL;

class EngineProcessImplTest {

    private final Process processMock = mock(Process.class);

    @Test
    void shouldReadAllLinesOfEmptyOutput() {
        // Given
        final var output = "";
        final InputStream in = new ByteArrayInputStream(output.getBytes(UTF_8));
        when(processMock.getInputStream()).thenReturn(in);

        // When
        final var engineProcess = new EngineProcessImpl(0, processMock);
        final var lines = engineProcess.readAllLines();

        // Then
        assertEquals(List.of(), lines);
    }

    @Test
    void shouldReadAllLines() {
        // Given
        final var output = "one" + EOL + "two" + EOL;
        final InputStream in = new ByteArrayInputStream(output.getBytes(UTF_8));
        when(processMock.getInputStream()).thenReturn(in);

        // When
        final var engineProcess = new EngineProcessImpl(0, processMock);
        final var lines = engineProcess.readAllLines();

        // Then
        assertEquals(List.of("one", "two"), lines);
    }

    @Test
    void shouldReadUntil() {
        // Given
        final var output = "one" + EOL + "two" + EOL;
        final InputStream in = new ByteArrayInputStream(output.getBytes(UTF_8));
        when(processMock.getInputStream()).thenReturn(in);

        // When
        final var engineProcess = new EngineProcessImpl(0, processMock);
        final var lines = engineProcess.readUntil("^o");

        // Then
        assertEquals(List.of("one"), lines);
    }

    @Test
    void shouldReadResponse() {
        // Given
        final var output = "move g1f3" + EOL;
        final InputStream in = new ByteArrayInputStream(output.getBytes(UTF_8));
        when(processMock.getInputStream()).thenReturn(in);

        // When
        final var engineProcess = new EngineProcessImpl(0, processMock);
        final var move = engineProcess.read(Move.class);

        // Then
        assertEquals("g1f3", move.text());
    }
}
