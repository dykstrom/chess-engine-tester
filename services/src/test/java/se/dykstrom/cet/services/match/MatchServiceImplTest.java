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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.bhlangonijr.chesslib.move.MoveList;
import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.state.ConfiguredEngine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.IncrementalTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.services.game.GameService;
import se.dykstrom.cet.services.game.PlayedGame;

import static com.github.bhlangonijr.chesslib.game.GameResult.DRAW;
import static com.github.bhlangonijr.chesslib.game.GameResult.WHITE_WON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchServiceImplTest {

    private static final TimeControl TIME_CONTROL = new IncrementalTimeControl(5, 0, 5);
    private static final String ENGINE_1_NAME = "e1";
    private static final String ENGINE_2_NAME = "e2";
    private static final String ENGINE_3_NAME = "e3";

    private static final EngineFeatures FEATURE_CONFIG_ENGINE_1_REUSE_YES = EngineFeatures.builder().myName(ENGINE_1_NAME).reuse("1").build();
    private static final EngineFeatures FEATURE_CONFIG_ENGINE_2_REUSE_YES = EngineFeatures.builder().myName(ENGINE_2_NAME).reuse("1").build();
    private static final EngineFeatures FEATURE_CONFIG_ENGINE_1_REUSE_NO = EngineFeatures.builder().myName(ENGINE_1_NAME).reuse("0").build();
    private static final EngineFeatures FEATURE_CONFIG_ENGINE_2_REUSE_NO = EngineFeatures.builder().myName(ENGINE_2_NAME).reuse("0").build();
    private static final GameConfig GAME_CONFIG_ENGINE_1_IS_WHITE = new GameConfig(ENGINE_1_NAME, ENGINE_2_NAME, TIME_CONTROL);
    private static final GameConfig GAME_CONFIG_ENGINE_1_IS_BLACK = new GameConfig(ENGINE_2_NAME, ENGINE_1_NAME, TIME_CONTROL);
    private static final MatchConfig MATCH_CONFIG = new MatchConfig(2, TIME_CONTROL);

    private final GameService gameServiceMock = mock(GameService.class);
    private final IdlingEngine initialIdlingEngine1Mock = mock(IdlingEngine.class);
    private final IdlingEngine initialIdlingEngine2Mock = mock(IdlingEngine.class);
    private final IdlingEngine initialIdlingEngine3Mock = mock(IdlingEngine.class);
    private final IdlingEngine finalIdlingEngine1Mock = mock(IdlingEngine.class);
    private final IdlingEngine finalIdlingEngine2Mock = mock(IdlingEngine.class);
    private final ConfiguredEngine configuredEngine1Mock = mock(ConfiguredEngine.class);
    private final ConfiguredEngine configuredEngine2Mock = mock(ConfiguredEngine.class);

    private final PlayedGame gamePlayedWithEngine1AsWhite =
            new PlayedGame(GAME_CONFIG_ENGINE_1_IS_WHITE, initialIdlingEngine1Mock, initialIdlingEngine2Mock, null, WHITE_WON, "Checkmate", new MoveList(), null);
    private final PlayedGame gamePlayedWithEngine1AsBlack =
            new PlayedGame(GAME_CONFIG_ENGINE_1_IS_WHITE, initialIdlingEngine2Mock, initialIdlingEngine1Mock, null, DRAW, "Stalemate", new MoveList(), null);

    private final MatchService matchService = new MatchServiceImpl(gameServiceMock);

    @Test
    void shouldPlaySingleGameMatch() {
        // Given
        when(initialIdlingEngine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(initialIdlingEngine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_WHITE, initialIdlingEngine1Mock, initialIdlingEngine2Mock))
                .thenReturn(gamePlayedWithEngine1AsWhite);

        // When
        final var playedMatch = matchService.playSingleGameMatch(TIME_CONTROL, initialIdlingEngine1Mock, initialIdlingEngine2Mock);

        // Then
        assertEquals(List.of(WHITE_WON), playedMatch.results());
        assertEquals(initialIdlingEngine1Mock, playedMatch.engine1());
        assertEquals(initialIdlingEngine2Mock, playedMatch.engine2());
    }

    @Test
    void shouldPlaySingleGameMatchWithExtraEngine() {
        // Given
        when(initialIdlingEngine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(initialIdlingEngine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(initialIdlingEngine3Mock.myName()).thenReturn(ENGINE_3_NAME);
        when(gameServiceMock.playGameWithExtraEngine(GAME_CONFIG_ENGINE_1_IS_WHITE, initialIdlingEngine1Mock, initialIdlingEngine2Mock, initialIdlingEngine3Mock))
                .thenReturn(gamePlayedWithEngine1AsWhite);

        // When
        final var playedMatch = matchService.playSingleGameMatchWithExtraEngine(TIME_CONTROL, initialIdlingEngine1Mock, initialIdlingEngine2Mock, initialIdlingEngine3Mock);

        // Then
        assertEquals(List.of(WHITE_WON), playedMatch.results());
        assertEquals(initialIdlingEngine1Mock, playedMatch.engine1());
        assertEquals(initialIdlingEngine2Mock, playedMatch.engine2());
    }

    @Test
    void shouldPlayMatch() {
        // Given
        when(initialIdlingEngine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(initialIdlingEngine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(initialIdlingEngine1Mock.features()).thenReturn(FEATURE_CONFIG_ENGINE_1_REUSE_YES);
        when(initialIdlingEngine2Mock.features()).thenReturn(FEATURE_CONFIG_ENGINE_2_REUSE_YES);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_WHITE, initialIdlingEngine1Mock, initialIdlingEngine2Mock))
                .thenReturn(gamePlayedWithEngine1AsWhite);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_BLACK, initialIdlingEngine2Mock, initialIdlingEngine1Mock))
                .thenReturn(gamePlayedWithEngine1AsBlack);
        final var matchCount = new AtomicInteger(0);

        // When
        matchService.addGameListener((gameNumber, startTime, playedGame) -> matchCount.incrementAndGet());
        final var playedMatch = matchService.playMatch(MATCH_CONFIG, initialIdlingEngine1Mock, initialIdlingEngine2Mock);

        // Then
        assertEquals(List.of(WHITE_WON, DRAW), playedMatch.results());
        assertEquals(initialIdlingEngine1Mock, playedMatch.engine1());
        assertEquals(initialIdlingEngine2Mock, playedMatch.engine2());
        assertEquals(2, matchCount.get());
    }

    @Test
    void shouldPlayMatchWithReuseNo() {
        // Given
        when(initialIdlingEngine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(initialIdlingEngine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(initialIdlingEngine1Mock.features()).thenReturn(FEATURE_CONFIG_ENGINE_1_REUSE_NO);
        when(initialIdlingEngine2Mock.features()).thenReturn(FEATURE_CONFIG_ENGINE_2_REUSE_NO);

        when(finalIdlingEngine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(finalIdlingEngine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(finalIdlingEngine1Mock.features()).thenReturn(FEATURE_CONFIG_ENGINE_1_REUSE_NO);
        when(finalIdlingEngine2Mock.features()).thenReturn(FEATURE_CONFIG_ENGINE_2_REUSE_NO);

        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_WHITE, initialIdlingEngine1Mock, initialIdlingEngine2Mock))
                .thenReturn(gamePlayedWithEngine1AsWhite);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_BLACK, finalIdlingEngine2Mock, finalIdlingEngine1Mock))
                .thenReturn(gamePlayedWithEngine1AsBlack);

        when(initialIdlingEngine1Mock.unload()).thenReturn(configuredEngine1Mock);
        when(initialIdlingEngine2Mock.unload()).thenReturn(configuredEngine2Mock);
        when(configuredEngine1Mock.load()).thenReturn(finalIdlingEngine1Mock);
        when(configuredEngine2Mock.load()).thenReturn(finalIdlingEngine2Mock);

        final var matchCount = new AtomicInteger(0);

        // When
        matchService.addGameListener((gameNumber, startTime, playedGame) -> matchCount.incrementAndGet());
        final var playedMatch = matchService.playMatch(MATCH_CONFIG, initialIdlingEngine1Mock, initialIdlingEngine2Mock);

        // Then
        assertEquals(List.of(WHITE_WON, DRAW), playedMatch.results());
        assertEquals(finalIdlingEngine1Mock, playedMatch.engine1());
        assertEquals(finalIdlingEngine2Mock, playedMatch.engine2());
        assertEquals(2, matchCount.get());
    }
}
