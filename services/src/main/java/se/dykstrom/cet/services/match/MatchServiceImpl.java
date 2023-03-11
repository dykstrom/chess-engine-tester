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

package se.dykstrom.cet.services.match;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.bhlangonijr.chesslib.game.GameResult;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.services.game.GameService;
import se.dykstrom.cet.services.game.GameServiceImpl;
import se.dykstrom.cet.services.game.PlayedGame;
import se.dykstrom.cet.services.util.GameListener;
import se.dykstrom.cet.services.util.ThreadUtils;

import static java.lang.System.Logger.Level.INFO;
import static java.util.Objects.requireNonNull;
import static se.dykstrom.cet.engine.util.Args.ensure;

public class MatchServiceImpl implements MatchService {

    private static final System.Logger LOGGER = System.getLogger(MatchServiceImpl.class.getName());

    private final GameService gameService;

    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final List<GameListener> gameListeners = new ArrayList<>();

    public MatchServiceImpl() {
        this(new GameServiceImpl());
    }

    public MatchServiceImpl(final GameService gameService) {
        this.gameService = requireNonNull(gameService);
    }

    @Override
    public void addGameListener(final GameListener gameListener) {
        gameListeners.add(requireNonNull(gameListener));
    }

    @Override
    public PlayedMatch playSingleGameMatch(final TimeControl timeControl,
                                           final IdlingEngine engine1,
                                           final IdlingEngine engine2) {
        LOGGER.log(INFO, "Starting new match of 1 game(s) between ''{0}'' and ''{1}''. Time control is {2}.",
                engine1.myName(), engine2.myName(), timeControl);
        playing.set(true);

        final var gameConfig = new GameConfig(engine1.myName(), engine2.myName(), timeControl);
        final var startTime = LocalDateTime.now();
        final var playedGame = gameService.playGame(gameConfig, engine1, engine2);
        notifyListeners(1, startTime, playedGame);

        final var results = List.of(playedGame.result());
        final var reasons = List.of(playedGame.reason());
        LOGGER.log(INFO, "Final results: {0}", results);
        return new PlayedMatch(
                new MatchConfig(1, timeControl),
                playedGame.whiteEngine(),
                playedGame.blackEngine(),
                null,
                results,
                reasons
        );
    }

    @Override
    public PlayedMatch playSingleGameMatchWithExtraEngine(final TimeControl timeControl,
                                                          final IdlingEngine engine1,
                                                          final IdlingEngine engine2,
                                                          final IdlingEngine engine3) {
        LOGGER.log(INFO, "Starting new match of 1 game(s) between ''{0}'' and ''{1}''. " +
                         "Using ''{2}'' as extra engine. Time control is {3}.",
                engine1.myName(), engine2.myName(), engine3.myName(), timeControl);
        playing.set(true);

        final var gameConfig = new GameConfig(engine1.myName(), engine2.myName(), timeControl);
        final var startTime = LocalDateTime.now();
        final var playedGame = gameService.playGameWithExtraEngine(gameConfig, engine1, engine2, engine3);
        notifyListeners(1, startTime, playedGame);

        final var results = List.of(playedGame.result());
        final var reasons = List.of(playedGame.reason());
        LOGGER.log(INFO, "Final results: {0}", results);
        return new PlayedMatch(
                new MatchConfig(1, timeControl),
                playedGame.whiteEngine(),
                playedGame.blackEngine(),
                playedGame.extraEngine(),
                results,
                reasons
        );
    }

    @Override
    public PlayedMatch playMatch(final MatchConfig matchConfig,
                                 final IdlingEngine engine1,
                                 final IdlingEngine engine2) {
        ensure(matchConfig.numberOfGames() % 2 == 0, "numberOfGames must be even");
        LOGGER.log(INFO, "Starting new match of {0} game(s) between ''{1}'' and ''{2}''. Time control is {3}.",
                matchConfig.numberOfGames(), engine1.myName(), engine2.myName(), matchConfig.timeControl());
        playing.set(true);
        final List<GameResult> results = new ArrayList<>();
        final List<String> reasons = new ArrayList<>();
        var idlingEngine1 = engine1;
        var idlingEngine2 = engine2;

        var round = 1;
        while (playing.get() && round <= matchConfig.numberOfGames()) {
            // Odd game
            var gameConfig = new GameConfig(idlingEngine1.myName(), idlingEngine2.myName(), matchConfig.timeControl());
            var startTime = LocalDateTime.now();
            var playedGame = gameService.playGame(gameConfig, idlingEngine1, idlingEngine2);
            notifyListeners(round, startTime, playedGame);
            idlingEngine1 = restartEngineIfNeeded(playedGame.whiteEngine());
            idlingEngine2 = restartEngineIfNeeded(playedGame.blackEngine());
            results.add(playedGame.result());
            reasons.add(playedGame.reason());
            round++;
            ThreadUtils.sleepSilently(1_000);

            // Even game
            gameConfig = new GameConfig(idlingEngine2.myName(), idlingEngine1.myName(), matchConfig.timeControl());
            startTime = LocalDateTime.now();
            playedGame = gameService.playGame(gameConfig, idlingEngine2, idlingEngine1);
            notifyListeners(round, startTime, playedGame);
            idlingEngine1 = restartEngineIfNeeded(playedGame.blackEngine());
            idlingEngine2 = restartEngineIfNeeded(playedGame.whiteEngine());
            results.add(playedGame.result());
            reasons.add(playedGame.reason());
            round++;
            ThreadUtils.sleepSilently(1_000);
        }

        LOGGER.log(INFO, "Final results: {0}", results);
        return new PlayedMatch(matchConfig, idlingEngine1, idlingEngine2, null, results, reasons);
    }

    /**
     * Restarts the engine process if reuse is disabled in the engine features.
     * If reuse is enabled, this method just returns the given idling engine.
     */
    private IdlingEngine restartEngineIfNeeded(final IdlingEngine idlingEngine) {
        if (idlingEngine.features().reuse()) {
            return idlingEngine;
        }

        return idlingEngine.unload().load();
    }

    @Override
    public void stopMatch() {
        gameService.stopGame();
        playing.set(false);
    }

    private void notifyListeners(final int round, final LocalDateTime startTime, final PlayedGame playedGame) {
        gameListeners.forEach(listener -> listener.gameOver(round, startTime, playedGame));
    }
}
