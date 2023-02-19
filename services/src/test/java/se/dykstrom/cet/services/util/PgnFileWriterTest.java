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

package se.dykstrom.cet.services.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.services.game.PlayedGame;
import se.dykstrom.cet.services.io.FileService;

import static com.github.bhlangonijr.chesslib.Side.BLACK;
import static com.github.bhlangonijr.chesslib.Side.WHITE;
import static com.github.bhlangonijr.chesslib.game.GameResult.BLACK_WON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PgnFileWriterTest {

    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 0, 30);
    private static final GameConfig GAME_CONFIG = new GameConfig("w", "b", TIME_CONTROL);

    private final FileService fileServiceMock = mock(FileService.class);
    private final ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<Iterable<String>> linesCaptor = ArgumentCaptor.forClass(Iterable.class);
    private final ArgumentCaptor<Charset> charsetCaptor = ArgumentCaptor.forClass(Charset.class);

    private final File file = new File("foo");
    private final PgnFileWriter writer = new PgnFileWriter(file, fileServiceMock);
    private final MoveList moves = new MoveList();

    @BeforeEach
    void setUp() {
        moves.add(new Move("f2f3", WHITE));
        moves.add(new Move("e7e5", BLACK));
        moves.add(new Move("g2g4", WHITE));
        moves.add(new Move("d8h4", BLACK));
    }

    @Test
    void shouldWriteFileWithoutExtraMoves() throws Exception {
        // Given
        final PlayedGame playedGame = new PlayedGame(
                GAME_CONFIG,
                null,
                null,
                BLACK_WON,
                "Checkmate",
                moves,
                null);

        // When
        writer.gameOver(1, LocalDateTime.now(), playedGame);

        // Then
        verify(fileServiceMock).write(pathCaptor.capture(), linesCaptor.capture(), charsetCaptor.capture(), any(), any(), any());
        assertEquals(file.toPath(), pathCaptor.getValue());
        assertEquals(StandardCharsets.UTF_8, charsetCaptor.getValue());
        final List<String> list = StreamSupport.stream(linesCaptor.getValue().spliterator(), false).toList();
        assertTrue(list.contains("[Round \"1\"]"));
        assertTrue(list.contains("[White \"w\"]"));
        assertTrue(list.contains("[Black \"b\"]"));
        assertTrue(list.contains("[TimeControl \"40/30\"]"));
        assertTrue(list.contains("1. f3 e5 2. g4 Qh4#"));
    }

    @Test
    void shouldWriteFileWithExtraMoves() throws Exception {
        // Given
        final PlayedGame playedGame = new PlayedGame(
                GAME_CONFIG,
                null,
                null,
                BLACK_WON,
                "Checkmate",
                moves,
                Map.of(1, "a5"));

        // When
        writer.gameOver(1, LocalDateTime.now(), playedGame);

        // Then
        verify(fileServiceMock).write(pathCaptor.capture(), linesCaptor.capture(), charsetCaptor.capture(), any(), any(), any());
        assertEquals(file.toPath(), pathCaptor.getValue());
        assertEquals(StandardCharsets.UTF_8, charsetCaptor.getValue());
        final List<String> list = StreamSupport.stream(linesCaptor.getValue().spliterator(), false).toList();
        assertTrue(list.contains("1. f3 e5 {1... a5} 2. g4 Qh4#"));
    }
}
