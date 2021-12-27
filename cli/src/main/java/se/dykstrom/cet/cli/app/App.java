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
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.github.bhlangonijr.chesslib.game.GameResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.time.TimeControlFormat;
import se.dykstrom.cet.services.engine.EngineService;
import se.dykstrom.cet.services.engine.EngineServiceImpl;
import se.dykstrom.cet.services.io.FileService;
import se.dykstrom.cet.services.io.FileServiceImpl;
import se.dykstrom.cet.services.match.MatchConfig;
import se.dykstrom.cet.services.match.MatchService;
import se.dykstrom.cet.services.match.MatchServiceImpl;
import se.dykstrom.cet.services.match.PlayedMatch;
import se.dykstrom.cet.services.util.PgnFileWriter;

import static com.github.bhlangonijr.chesslib.game.GameResult.BLACK_WON;
import static com.github.bhlangonijr.chesslib.game.GameResult.DRAW;
import static com.github.bhlangonijr.chesslib.game.GameResult.WHITE_WON;
import static java.util.Locale.US;
import static se.dykstrom.cet.engine.util.StringUtils.EOL;

@SuppressWarnings("unused")
@Command(name = "cet",
         mixinStandardHelpOptions = true,
         version = "chess-engine-tester 0.1.0",
         description = "Tests chess engines by letting them play each other.")
public class App implements Callable<Integer> {

    @Option(names = {"-1", "--engine1"}, description = "Chess engine 1 config FILENAME.", paramLabel = "FILENAME", required = true)
    private File engine1File;

    @Option(names = {"-2", "--engine2"}, description = "Chess engine 2 config FILENAME.", paramLabel = "FILENAME", required = true)
    private File engine2File;

    @Option(names = {"-3", "--engine3"},
            description = "Chess engine 3 config FILENAME. The optional third engine will shadow the engine playing black. " +
                          "It will think about the same moves as the black engine, but its counter moves will only be logged, and not played.",
            paramLabel = "FILENAME")
    private File engine3File;

    @Option(names = {"-o", "--output"},
            description = "PGN game file FILENAME. If not specified, no file will be written.",
            paramLabel = "FILENAME")
    private File outputFile;

    @Option(names = {"-n", "--number"},
            description = "Number of games to play. Either 1 or a positive, even number.",
            paramLabel = "NUMBER",
            required = true)
    private int numberOfGames;

    @Option(names = {"-t", "--time"},
            description = "Time control in PGN format. Either moves/seconds or initial+increase (both in seconds).",
            paramLabel = "TIME CONTROL",
            required = true)
    private String timeControlString;

    @Spec
    private CommandSpec spec;

    private final FileService fileService;
    private final EngineService engineService;
    private final MatchService matchService;

    public App() {
        this(new FileServiceImpl(), new EngineServiceImpl(), new MatchServiceImpl());
    }

    public App(final FileService fileService, final EngineService engineService, final MatchService matchService) {
        this.engineService = engineService;
        this.matchService = matchService;
        this.fileService = fileService;
    }

    @Override
    public Integer call() {
        final boolean numberOfGamesOk = numberOfGames == 1 || (numberOfGames > 1 && numberOfGames % 2 == 0);
        if (!numberOfGamesOk) {
            spec.commandLine().getErr().println("Number of games must be either 1 or a positive, even number.");
            return ExitCode.USAGE;
        }

        final TimeControl timeControl;
        try {
            timeControl = TimeControlFormat.parse(timeControlString);
        } catch (ParseException e) {
            spec.commandLine().getErr().println("Cannot parse time control: " + timeControlString);
            return ExitCode.USAGE;
        }

        if (!fileService.canRead(engine1File)) {
            spec.commandLine().getErr().println("Cannot open engine 1 file: " + engine1File);
            return ExitCode.USAGE;
        }
        if (!fileService.canRead(engine2File)) {
            spec.commandLine().getErr().println("Cannot open engine 2 file: " + engine2File);
            return ExitCode.USAGE;
        }
        if (engine3File != null && !fileService.canRead(engine3File)) {
            spec.commandLine().getErr().println("Cannot open engine 3 file: " + engine3File);
            return ExitCode.USAGE;
        }

        final IdlingEngine engine1;
        final IdlingEngine engine2;
        final IdlingEngine engine3;
        try {
             engine1 = engineService.load(engine1File);
        } catch (IOException e) {
            spec.commandLine().getErr().println("Cannot read engine 1 file: " + e.getMessage());
            return ExitCode.SOFTWARE;
        }
        try {
             engine2 = engineService.load(engine2File);
        } catch (IOException e) {
            spec.commandLine().getErr().println("Cannot read engine 2 file: " + e.getMessage());
            return ExitCode.SOFTWARE;
        }
        try {
            if (engine3File != null) {
                engine3 = engineService.load(engine3File);
            } else {
                engine3 = null;
            }
        } catch (IOException e) {
            spec.commandLine().getErr().println("Cannot read engine 3 file: " + e.getMessage());
            return ExitCode.SOFTWARE;
        }

        spec.commandLine().getOut().println("Starting match of " + numberOfGames + " game(s) between " +
                                            engine1.myName() + " and " + engine2.myName());
        if (engine3 != null) {
            spec.commandLine().getOut().println("Black engine is shadowed by " + engine3.myName());
        }
        spec.commandLine().getOut().println("Time control is " + timeControl.toPgn());
        if (outputFile != null) {
            spec.commandLine().getOut().println("Saving games to " + outputFile);
        }
        matchService.addGameListener(new ProgressBarWriter(numberOfGames));
        matchService.addGameListener(new PgnFileWriter(outputFile, fileService));
        final PlayedMatch playedMatch;
        if (numberOfGames == 1) {
            if (engine3File != null) {
                playedMatch = matchService.playSingleGameMatchWithExtraEngine(timeControl, engine1, engine2, engine3);
            } else {
                playedMatch = matchService.playSingleGameMatch(timeControl, engine1, engine2);
            }
        } else {
            playedMatch = matchService.playMatch(new MatchConfig(numberOfGames, timeControl), engine1, engine2);
        }
        printResult(playedMatch);

        engineService.unload(engine1);
        engineService.unload(engine2);
        if (engine3 != null) {
            engineService.unload(engine3);
        }

        return ExitCode.OK;
    }

    private void printResult(final PlayedMatch playedMatch) {
        final String engine1 = playedMatch.idlingEngine1().myName();
        final String engine2 = playedMatch.idlingEngine2().myName();
        final List<GameResult> results = playedMatch.results();
        final List<String> reasons = playedMatch.reasons();

        final var numberColumnWidth = 3;
        final var namesColumnWidth = engine1.length() + 3 + engine2.length();
        final var resultColumnWidth = maxWidth(results, GameResult::getDescription);
        final var reasonColumnWidth = maxWidth(reasons, s -> s);
        final var totalWidth = 3 + numberColumnWidth + 5 + namesColumnWidth + 5 + resultColumnWidth + 5 + reasonColumnWidth + 3;

        var engine1Score = 0.0;
        var engine2Score = 0.0;
        final var builder = new StringBuilder();
        builder.append(" ").append("-".repeat(totalWidth - 2)).append(" ").append(EOL);
        for (var gameNumber = 1; gameNumber <= numberOfGames; gameNumber++) {
            final var result = results.get(gameNumber - 1);
            final var reason = reasons.get(gameNumber - 1);

            builder.append("|  ").append(String.format("%3d", gameNumber)).append("  |  ");
            if (gameNumber % 2 != 0) {
                builder.append(engine1).append(" - ").append(engine2).append("  |  ");
            } else {
                builder.append(engine2).append(" - ").append(engine1).append("  |  ");
            }
            builder.append(result.getDescription()).append("  |  ");
            builder.append(reason).append("  |").append(EOL);

            if (gameNumber % 2 != 0) {
                engine1Score += (result == WHITE_WON) ? 1.0 : 0.0;
                engine2Score += (result == BLACK_WON) ? 1.0 : 0.0;
            } else {
                engine1Score += (result == BLACK_WON) ? 1.0 : 0.0;
                engine2Score += (result == WHITE_WON) ? 1.0 : 0.0;
            }
            engine1Score += (result == DRAW) ? 0.5 : 0.0;
            engine2Score += (result == DRAW) ? 0.5 : 0.0;
        }
        builder.append(" ").append("-".repeat(totalWidth - 2)).append(" ").append(EOL);
        builder.append("Final result:").append(EOL);
        builder.append(String.format(US, "%-20s : %2.1f", engine1, engine1Score)).append(EOL);
        builder.append(String.format(US, "%-20s : %2.1f", engine2, engine2Score));
        spec.commandLine().getOut().println(builder);
    }

    public static <T> int maxWidth(Collection<T> collection, Function<T, String> extractor) {
        return collection.stream().map(extractor).mapToInt(String::length).max().orElseThrow();
    }

    public int execute(final String[] args) {
        return new CommandLine(this).execute(args);
    }

    public static void main(String[] args) {
        System.exit(new App().execute(args));
    }
}
