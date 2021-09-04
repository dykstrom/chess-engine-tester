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

package se.dykstrom.cet.engine.parser;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.dykstrom.cet.engine.util.StringUtils.EOL;

class ParserTest {

    @Test
    void hasNextShouldReturnFalseIfStreamIsEmpty() throws Exception {
        // Given
        final var output = "";
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var hasNext = parser.hasNext();

        // Then
        assertFalse(hasNext);
    }

    @Test
    void hasNextShouldReturnTrueIfStreamHasContent() throws Exception {
        // Given
        final var output = "move e2e4" + EOL;
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var hasNext = parser.hasNext();

        // Then
        assertTrue(hasNext);
    }

    @Test
    void hasNextShouldReturnFalseIfStreamContainsGarbage() throws Exception {
        // Given
        final var output = "# Comment";
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var hasNext = parser.hasNext();

        // Then
        assertFalse(hasNext);
    }

    @ParameterizedTest
    @CsvSource({
                       "Illegal move,moving into check,e1g1",
                       "Illegal move,,e8e8",
                       "Invalid move,,e8e8"
               })
    void shouldParseIllegalMove(final String text, final String reason, final String move) throws Exception {
        // Given
        final var formattedReason = reason != null ? " (" + reason + ")" : "";
        final var output = text + formattedReason + ": " + move + EOL;
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var response = parser.next();

        // Then
        assertEquals(new IllegalMove(move, reason), response);
    }

    @Test
    void shouldParseInvalidCommand() throws Exception {
        // Given
        final var output = "Error: foo" + EOL;
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var response = parser.next();

        // Then
        assertEquals(new InvalidCommand("Error: foo"), response);
    }

    @ParameterizedTest
    @CsvSource({
                       "move,e2e4",
                       "My move is :,e7e5"
               })
    void shouldParseMove(final String text, final String move) throws Exception {
        // Given
        final var output = text + " " + move + EOL;
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var response = parser.next();

        // Then
        assertEquals(new Move(move), response);
    }

    @ParameterizedTest
    @CsvSource({
                       "1-0,White mates",
                       "0-1,Black mates",
                       "1/2-1/2,Draw",
                       "*,Shut down"
               })
    void shouldParseResult(final String code, final String text) throws Exception {
        // Given
        final var output = code + " {" + text + "}" + EOL;
        final var in = new ByteArrayInputStream(output.getBytes(UTF_8));

        // When
        final var parser = new Parser(in);
        final var response = parser.next();

        // Then
        assertEquals(new Result(code, text), response);
    }
}
