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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import se.dykstrom.cet.services.game.PlayedGame;

import static java.lang.System.Logger.Level.ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static se.dykstrom.cet.services.util.PgnUtils.tag;

public record PgnFileWriter(File outputFile) implements GameListener {

    private static final System.Logger LOGGER = System.getLogger(PgnFileWriter.class.getName());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @SuppressWarnings("java:S106")
    @Override
    public void gameOver(final int round, final LocalDateTime startTime, final PlayedGame game) {
        if (outputFile != null) {
            List<String> lines = new ArrayList<>();

            lines.add(tag("Event", "Chess Game"));
            lines.add(tag("Site", getHostName()));
            lines.add(tag("Date", DATE_FORMATTER.format(startTime)));
            lines.add(tag("Round", round));
            lines.add(tag("White", game.gameConfig().white()));
            lines.add(tag("Black", game.gameConfig().black()));
            lines.add(tag("Result", game.result().getDescription()));
            lines.add(tag("PlyCount", game.moves().size()));
            lines.add(tag("Time", TIME_FORMATTER.format(startTime)));
            lines.add(tag("TimeControl", game.gameConfig().timeControl().toPgn()));
            lines.add("");

            lines.addAll(PgnUtils.formatMoveText(game.moves().toSanArray()));
            lines.add(game.result().getDescription() + " {" + game.reason() + "}");
            lines.add("");

            try {
                Files.write(outputFile.toPath(), lines, UTF_8, CREATE, WRITE, APPEND);
            } catch (IOException e) {
                LOGGER.log(ERROR, "Cannot write game to output file ''{0}'': {1}", outputFile, e.getMessage());
            }
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }
}
