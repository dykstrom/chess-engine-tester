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
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.IncrementalTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
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

    private static final GameConfig GAME_CONFIG_ENGINE_1_IS_WHITE = new GameConfig(ENGINE_1_NAME, ENGINE_2_NAME, TIME_CONTROL);
    private static final GameConfig GAME_CONFIG_ENGINE_1_IS_BLACK = new GameConfig(ENGINE_2_NAME, ENGINE_1_NAME, TIME_CONTROL);
    private static final MatchConfig MATCH_CONFIG = new MatchConfig(2, TIME_CONTROL);

    private final GameService gameServiceMock = mock(GameService.class);
    private final IdlingEngine engine1Mock = mock(IdlingEngine.class);
    private final IdlingEngine engine2Mock = mock(IdlingEngine.class);
    private final IdlingEngine engine3Mock = mock(IdlingEngine.class);
    private final PlayedGame gamePlayedWithEngine1AsWhite =
            new PlayedGame(GAME_CONFIG_ENGINE_1_IS_WHITE, engine1Mock, engine2Mock, WHITE_WON, "Checkmate", new MoveList(), null);
    private final PlayedGame gamePlayedWithEngine1AsBlack =
            new PlayedGame(GAME_CONFIG_ENGINE_1_IS_WHITE, engine2Mock, engine1Mock, DRAW, "Stalemate", new MoveList(), null);

    private final MatchService matchService = new MatchServiceImpl(gameServiceMock);

    @Test
    void shouldPlaySingleGameMatch() {
        // Given
        when(engine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(engine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_WHITE, engine1Mock, engine2Mock)).thenReturn(gamePlayedWithEngine1AsWhite);

        // When
        final var playedMatch = matchService.playSingleGameMatch(TIME_CONTROL, engine1Mock, engine2Mock);

        // Then
        assertEquals(List.of(WHITE_WON), playedMatch.results());
        assertEquals(engine1Mock, playedMatch.idlingEngine1());
        assertEquals(engine2Mock, playedMatch.idlingEngine2());
    }

    @Test
    void shouldPlaySingleGameMatchWithExtraEngine() {
        // Given
        when(engine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(engine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(engine3Mock.myName()).thenReturn(ENGINE_3_NAME);
        when(gameServiceMock.playGameWithExtraEngine(GAME_CONFIG_ENGINE_1_IS_WHITE, engine1Mock, engine2Mock, engine3Mock))
                .thenReturn(gamePlayedWithEngine1AsWhite);

        // When
        final var playedMatch = matchService.playSingleGameMatchWithExtraEngine(TIME_CONTROL, engine1Mock, engine2Mock, engine3Mock);

        // Then
        assertEquals(List.of(WHITE_WON), playedMatch.results());
        assertEquals(engine1Mock, playedMatch.idlingEngine1());
        assertEquals(engine2Mock, playedMatch.idlingEngine2());
    }

    @Test
    void shouldPlayMatch() {
        // Given
        when(engine1Mock.myName()).thenReturn(ENGINE_1_NAME);
        when(engine2Mock.myName()).thenReturn(ENGINE_2_NAME);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_WHITE, engine1Mock, engine2Mock)).thenReturn(gamePlayedWithEngine1AsWhite);
        when(gameServiceMock.playGame(GAME_CONFIG_ENGINE_1_IS_BLACK, engine2Mock, engine1Mock)).thenReturn(gamePlayedWithEngine1AsBlack);
        final var matchCount = new AtomicInteger(0);

        // When
        matchService.addGameListener((gameNumber, startTime, playedGame) -> matchCount.incrementAndGet());
        final var playedMatch = matchService.playMatch(MATCH_CONFIG, engine1Mock, engine2Mock);

        // Then
        assertEquals(List.of(WHITE_WON, DRAW), playedMatch.results());
        assertEquals(engine1Mock, playedMatch.idlingEngine1());
        assertEquals(engine2Mock, playedMatch.idlingEngine2());
        assertEquals(2, matchCount.get());
    }
}
