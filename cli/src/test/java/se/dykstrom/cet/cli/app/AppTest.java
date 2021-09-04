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

package se.dykstrom.cet.cli.app;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.ClassicTimeControl;
import se.dykstrom.cet.services.engine.EngineService;
import se.dykstrom.cet.services.io.FileService;
import se.dykstrom.cet.services.match.MatchConfig;
import se.dykstrom.cet.services.match.MatchService;
import se.dykstrom.cet.services.match.PlayedMatch;
import se.dykstrom.cet.services.util.PgnFileWriter;

import static com.github.bhlangonijr.chesslib.game.GameResult.BLACK_WON;
import static com.github.bhlangonijr.chesslib.game.GameResult.DRAW;
import static com.github.bhlangonijr.chesslib.game.GameResult.WHITE_WON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppTest {

    private static final ClassicTimeControl TIME_CONTROL = new ClassicTimeControl(40, 1, 0);

    private final StringWriter stdout = new StringWriter();
    private final StringWriter stderr = new StringWriter();
    private final CommandLine commandLine = new CommandLine(new App());

    private final FileService fileServiceMock = mock(FileService.class);
    private final EngineService engineServiceMock = mock(EngineService.class);
    private final MatchService matchServiceMock = mock(MatchService.class);

    private final IdlingEngine idlingEngine1Mock = mock(IdlingEngine.class);
    private final IdlingEngine idlingEngine2Mock = mock(IdlingEngine.class);
    private final IdlingEngine idlingEngine3Mock = mock(IdlingEngine.class);

    @BeforeEach
    public void setUp() {
        commandLine.setOut(new PrintWriter(stdout));
        commandLine.setErr(new PrintWriter(stderr));
    }

    @Test
    void shouldShowHelp() {
        // Given
        final String[] args = {
                "-h"
        };

        // When
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.OK, exitCode);
        assertTrue(stdout.toString().contains("Usage:"));
        assertTrue(stderr.toString().isBlank());
    }

    @Test
    void shouldNotAllowOddNumberOfGames() {
        // Given
        final String[] args = {
                "-n", "3"
        };

        // When
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        assertTrue(stdout.toString().isBlank());
        assertTrue(stderr.toString().contains("Number of games"));
    }

    @Test
    void shouldNotAllowNegativeNumberOfGames() {
        // Given
        final String[] args = {
                "-n", "-2"
        };

        // When
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        assertTrue(stdout.toString().isBlank());
        assertTrue(stderr.toString().contains("Number of games"));
    }

    @Test
    void shouldNotParseTimeControl() {
        // Given
        final String[] args = {
                "-n", "2",
                "-t", "foo",
                "-1", "foo.json",
                "-2", "bar.json"
        };

        // When
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        assertTrue(stdout.toString().isBlank());
        assertTrue(stderr.toString().contains("Cannot parse time control"));
    }

    @Test
    void shouldCheckInputFile() {
        // Given
        final String[] args = {
                "-n", "1",
                "-t", "40/60",
                "-1", "foo.json",
                "-2", "bar.json"
        };

        // When
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.USAGE, exitCode);
        assertTrue(stdout.toString().isBlank());
        assertTrue(stderr.toString().contains("Cannot open engine 1 file:"));
    }

    @Test
    void shouldPlaySingleGameMatch() throws Exception {
        // Given
        final var foo = new File("foo.json");
        final var bar = new File("bar.json");
        final String[] args = {
                "-n", "1",
                "-t", "40/60",
                "-1", foo.getPath(),
                "-2", bar.getPath()
        };
        final MatchConfig matchConfig = new MatchConfig(1, TIME_CONTROL);
        final PlayedMatch playedMatch = new PlayedMatch(matchConfig, idlingEngine1Mock, idlingEngine2Mock, List.of(BLACK_WON), List.of("Checkmate"));
        when(fileServiceMock.canRead(foo)).thenReturn(true);
        when(fileServiceMock.canRead(bar)).thenReturn(true);
        when(engineServiceMock.load(any())).thenReturn(idlingEngine1Mock);
        when(matchServiceMock.playSingleGameMatch(any(), any(), any())).thenReturn(playedMatch);
        when(idlingEngine1Mock.myName()).thenReturn("foo");
        when(idlingEngine2Mock.myName()).thenReturn("bar");

        // When
        final var commandLine = new CommandLine(new App(fileServiceMock, engineServiceMock, matchServiceMock));
        commandLine.setOut(new PrintWriter(stdout));
        commandLine.setErr(new PrintWriter(stderr));
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.OK, exitCode);
        assertTrue(stdout.toString().contains("1  |  foo - bar  |  0-1"));
        assertTrue(stdout.toString().contains(" : 0.0"));
        assertTrue(stdout.toString().contains(" : 1.0"));
        verify(fileServiceMock).canRead(foo);
        verify(fileServiceMock).canRead(bar);
        verify(engineServiceMock, times(2)).load(any());
        verify(matchServiceMock).addGameListener(any(ProgressBarWriter.class));
        verify(matchServiceMock).addGameListener(any(PgnFileWriter.class));
        verify(matchServiceMock).playSingleGameMatch(any(), any(), any());
    }

    @Test
    void shouldPlaySingleGameMatchWithExtraEngine() throws Exception {
        // Given
        final var foo = new File("foo.json");
        final var bar = new File("bar.json");
        final var tee = new File("tee.json");
        final String[] args = {
                "-n", "1",
                "-t", "40/60",
                "-1", foo.getPath(),
                "-2", bar.getPath(),
                "-3", tee.getPath()
        };
        final MatchConfig matchConfig = new MatchConfig(1, TIME_CONTROL);
        final PlayedMatch playedMatch = new PlayedMatch(matchConfig, idlingEngine1Mock, idlingEngine2Mock, List.of(BLACK_WON), List.of("Checkmate"));
        when(fileServiceMock.canRead(foo)).thenReturn(true);
        when(fileServiceMock.canRead(bar)).thenReturn(true);
        when(fileServiceMock.canRead(tee)).thenReturn(true);
        when(engineServiceMock.load(any())).thenReturn(idlingEngine1Mock);
        when(matchServiceMock.playSingleGameMatchWithExtraEngine(any(), any(), any(), any())).thenReturn(playedMatch);
        when(idlingEngine1Mock.myName()).thenReturn("foo");
        when(idlingEngine2Mock.myName()).thenReturn("bar");
        when(idlingEngine3Mock.myName()).thenReturn("tee");

        // When
        final var commandLine = new CommandLine(new App(fileServiceMock, engineServiceMock, matchServiceMock));
        commandLine.setOut(new PrintWriter(stdout));
        commandLine.setErr(new PrintWriter(stderr));
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.OK, exitCode);
        assertTrue(stdout.toString().contains("1  |  foo - bar  |  0-1"));
        assertTrue(stdout.toString().contains(" : 0.0"));
        assertTrue(stdout.toString().contains(" : 1.0"));
        verify(fileServiceMock).canRead(foo);
        verify(fileServiceMock).canRead(bar);
        verify(fileServiceMock).canRead(tee);
        verify(engineServiceMock, times(3)).load(any());
        verify(matchServiceMock).addGameListener(any(ProgressBarWriter.class));
        verify(matchServiceMock).addGameListener(any(PgnFileWriter.class));
        verify(matchServiceMock).playSingleGameMatchWithExtraEngine(any(), any(), any(), any());
    }

    @Test
    void shouldPlayMultiGameMatch() throws Exception {
        // Given
        final String[] args = {
                "-n", "4",
                "-t", "40/60",
                "-1", "foo.json",
                "-2", "bar.json"
        };
        final MatchConfig matchConfig = new MatchConfig(4, TIME_CONTROL);
        final PlayedMatch playedMatch = new PlayedMatch(matchConfig, idlingEngine1Mock, idlingEngine2Mock,
                List.of(BLACK_WON, WHITE_WON, WHITE_WON, DRAW),
                List.of("Checkmate", "Checkmate", "Time forfeit", "Draw by repetition"));
        when(fileServiceMock.canRead(any())).thenReturn(true);
        when(engineServiceMock.load(any())).thenReturn(idlingEngine1Mock);
        when(matchServiceMock.playMatch(any(), any(), any())).thenReturn(playedMatch);
        when(idlingEngine1Mock.myName()).thenReturn("foo");
        when(idlingEngine2Mock.myName()).thenReturn("bar");

        // When
        final var commandLine = new CommandLine(new App(fileServiceMock, engineServiceMock, matchServiceMock));
        commandLine.setOut(new PrintWriter(stdout));
        commandLine.setErr(new PrintWriter(stderr));
        final var exitCode = commandLine.execute(args);

        // Then
        assertEquals(CommandLine.ExitCode.OK, exitCode);
        assertTrue(stdout.toString().contains("1  |  foo - bar  |  0-1"));
        assertTrue(stdout.toString().contains("2  |  bar - foo  |  1-0"));
        assertTrue(stdout.toString().contains(" : 1.5"));
        assertTrue(stdout.toString().contains(" : 2.5"));
        verify(fileServiceMock, times(2)).canRead(any());
        verify(engineServiceMock, times(2)).load(any());
        verify(matchServiceMock).addGameListener(any(ProgressBarWriter.class));
        verify(matchServiceMock).addGameListener(any(PgnFileWriter.class));
        verify(matchServiceMock).playMatch(any(), any(), any());
    }
}
