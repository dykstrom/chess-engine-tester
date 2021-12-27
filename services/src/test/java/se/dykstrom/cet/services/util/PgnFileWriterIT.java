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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.services.game.PlayedGame;
import se.dykstrom.cet.services.io.FileService;
import se.dykstrom.cet.services.io.FileServiceImpl;

import static com.github.bhlangonijr.chesslib.Side.BLACK;
import static com.github.bhlangonijr.chesslib.Side.WHITE;
import static com.github.bhlangonijr.chesslib.game.GameResult.BLACK_WON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PgnFileWriterIT {

    private static final TimeControl TIME_CONTROL = new ClassicTimeControl(40, 0, 30);
    private static final GameConfig GAME_CONFIG = new GameConfig("w", "b", TIME_CONTROL);
    private static final FileService FILE_SERVICE = new FileServiceImpl();

    private PlayedGame playedGame;

    @BeforeEach
    void setUp() {
        final MoveList moves = new MoveList();
        moves.add(new Move("f2f3", WHITE));
        moves.add(new Move("e7e5", BLACK));
        moves.add(new Move("g2g4", WHITE));
        moves.add(new Move("d8h4", BLACK));

        playedGame = new PlayedGame(
                GAME_CONFIG,
                null,
                null,
                BLACK_WON,
                "Checkmate",
                moves,
                null);
    }

    @Test
    void shouldWriteFileWhenGameOver() throws Exception {
        // Given
        final Path path = Files.createTempFile(null, null);
        final File file = path.toFile();
        file.deleteOnExit();
        PgnFileWriter writer = new PgnFileWriter(file, FILE_SERVICE);

        // When
        writer.gameOver(1, LocalDateTime.now(), playedGame);

        // Then
        assertTrue(file.exists());
        final var text = Files.readString(path, UTF_8);
        assertTrue(text.contains("[Round \"1\"]"));
        assertTrue(text.contains("[Result \"0-1\"]"));
        assertTrue(text.contains("1. f3 e5 2. g4 Qh4#"));
    }
}
