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

package se.dykstrom.cet.services.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.exception.UnexpectedException;
import se.dykstrom.cet.engine.parser.IllegalMove;
import se.dykstrom.cet.engine.parser.Result;
import se.dykstrom.cet.engine.state.ActiveEngine;
import se.dykstrom.cet.engine.state.ForcedEngine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.IncrementalTimeControl;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.services.exception.TimeoutException;

import static com.github.bhlangonijr.chesslib.game.GameResult.BLACK_WON;
import static com.github.bhlangonijr.chesslib.game.GameResult.DRAW;
import static com.github.bhlangonijr.chesslib.game.GameResult.WHITE_WON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameServiceImplTest {

    private static final String WHITE_NAME = "WhiteEngine";
    private static final String BLACK_NAME = "BlackEngine";
    private static final String EXTRA_NAME = "ExtraEngine";

    private static final TimeControl TIME_CONTROL = new IncrementalTimeControl(5, 0, 5);
    private static final GameConfig GAME_CONFIG = new GameConfig(WHITE_NAME, BLACK_NAME, TIME_CONTROL);
    private static final EngineFeatures EXTRA_ENGINE_PLAY_OTHER_YES = EngineFeatures.builder().myName(EXTRA_NAME).playOther("1").build();
    private static final EngineFeatures EXTRA_ENGINE_PLAY_OTHER_NO = EngineFeatures.builder().myName(EXTRA_NAME).playOther("0").build();

    private final IdlingEngine idlingWhiteEngineMock = mock(IdlingEngine.class);
    private final IdlingEngine idlingBlackEngineMock = mock(IdlingEngine.class);
    private final IdlingEngine idlingExtraEngineMock = mock(IdlingEngine.class);
    private final ForcedEngine forcedWhiteEngineMock = mock(ForcedEngine.class);
    private final ForcedEngine forcedBlackEngineMock = mock(ForcedEngine.class);
    private final ForcedEngine forcedExtraEngineMock = mock(ForcedEngine.class);
    private final ActiveEngine activeWhiteEngine = mock(ActiveEngine.class);
    private final ActiveEngine activeBlackEngine = mock(ActiveEngine.class);
    private final ActiveEngine activeExtraEngine = mock(ActiveEngine.class);

    private final GameServiceImpl gameService = new GameServiceImpl();

    @BeforeEach
    void setUp() {
        setUpEngine(idlingWhiteEngineMock, forcedWhiteEngineMock, activeWhiteEngine, WHITE_NAME);
        setUpEngine(idlingBlackEngineMock, forcedBlackEngineMock, activeBlackEngine, BLACK_NAME);
        setUpEngine(idlingExtraEngineMock, forcedExtraEngineMock, activeExtraEngine, EXTRA_NAME);
    }

    private void setUpEngine(final IdlingEngine idlingEngineMock,
                             final ForcedEngine forcedEngineMock,
                             final ActiveEngine activeEngine,
                             final String name) {
        when(idlingEngineMock.myName()).thenReturn(name);
        when(idlingEngineMock.start(any())).thenReturn(forcedEngineMock);
        when(forcedEngineMock.go()).thenReturn(activeEngine);
        when(activeEngine.force()).thenReturn(forcedEngineMock);
        when(forcedEngineMock.stop()).thenReturn(idlingEngineMock);
    }

    @Test
    void shouldPlayUntilBlackMates() {
        // Given
        when(activeWhiteEngine.readMove()).thenReturn("f2f3");
        when(activeBlackEngine.readMove()).thenReturn("e7e5");
        when(activeWhiteEngine.makeAndReadMove("e7e5")).thenReturn("g2g4");
        when(activeBlackEngine.makeAndReadMove("g2g4")).thenReturn("d8h4");
        when(activeWhiteEngine.makeAndReadMove("d8h4")).thenThrow(new UnexpectedException(new Result("0-1", "Black mates")));

        // When
        final var playedGame = gameService.playGame(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock);

        // Then
        assertEquals(BLACK_WON, playedGame.result());
    }

    @Test
    void shouldPlayUntilBlackMatesButWhiteDoesNotRecognize() {
        // Given
        when(activeWhiteEngine.readMove()).thenReturn("f2f3");
        when(activeBlackEngine.readMove()).thenReturn("e7e5");
        when(activeWhiteEngine.makeAndReadMove("e7e5")).thenReturn("g2g4");
        when(activeBlackEngine.makeAndReadMove("g2g4")).thenReturn("d8h4");
        when(activeWhiteEngine.makeAndReadMove("d8h4")).thenReturn("a2a3");
        when(activeBlackEngine.makeAndReadMove("a2a3")).thenThrow(new UnexpectedException(new Result("0-1", "Black mates")));

        // When
        final var playedGame = gameService.playGame(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock);

        // Then
        assertEquals(BLACK_WON, playedGame.result());
    }

    @Test
    void shouldPlayUntilWhiteTimesOut() {
        // Given
        when(activeWhiteEngine.readMove()).thenReturn("f2f3");
        when(activeBlackEngine.readMove()).thenReturn("e7e5");
        when(activeWhiteEngine.makeAndReadMove("e7e5")).thenThrow(new TimeoutException("Timeout"));

        // When
        final var playedGame = gameService.playGame(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock);

        // Then
        assertEquals(BLACK_WON, playedGame.result());
    }

    @Test
    void shouldPlayUntilWhiteMakesNonsenseMove() {
        // Given
        when(activeWhiteEngine.readMove()).thenReturn("foo");

        // When
        final var playedGame = gameService.playGame(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock);

        // Then
        assertEquals(BLACK_WON, playedGame.result());
    }

    @Test
    void shouldPlayUntilBlackMakesIllegalMove() {
        // Given
        when(activeWhiteEngine.readMove()).thenReturn("e2e4");
        when(activeBlackEngine.readMove()).thenReturn("e7e5");
        when(activeWhiteEngine.makeAndReadMove("e7e5")).thenReturn("g1f3");
        when(activeBlackEngine.makeAndReadMove("g1f3")).thenReturn("e8e8");
        when(activeWhiteEngine.makeAndReadMove("e8e8")).thenThrow(new UnexpectedException(new IllegalMove("e8e8", "invalid")));

        // When
        final var playedGame = gameService.playGame(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock);

        // Then
        assertEquals(WHITE_WON, playedGame.result());
    }

    @Test
    void shouldPlayUntilDrawByRepetition() {
        // Given
        when(activeWhiteEngine.readMove()).thenReturn("g1f3");
        when(activeBlackEngine.readMove()).thenReturn("g8f6");
        when(activeWhiteEngine.makeAndReadMove("g8f6")).thenReturn("f3g1");
        when(activeBlackEngine.makeAndReadMove("f3g1")).thenReturn("f6g8");
        when(activeWhiteEngine.makeAndReadMove("f6g8")).thenReturn("g1f3");
        when(activeBlackEngine.makeAndReadMove("g1f3")).thenReturn("g8f6");

        // When
        final var playedGame = gameService.playGame(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock);

        // Then
        assertEquals(DRAW, playedGame.result());
    }

    @Test
    void shouldPlayUntilBlackMatesWithExtraEngine() {
        // Given
        final var reason = "Black mates";
        when(activeWhiteEngine.readMove()).thenReturn("f2f3");
        when(activeBlackEngine.readMove()).thenReturn("e7e5");
        when(activeExtraEngine.readMove()).thenReturn("a7a5", "d8h4");
        when(activeWhiteEngine.makeAndReadMove("e7e5")).thenReturn("g2g4");
        when(activeBlackEngine.makeAndReadMove("g2g4")).thenReturn("d8h4");
        when(activeWhiteEngine.makeAndReadMove("d8h4")).thenThrow(new UnexpectedException(new Result("0-1", reason)));
        when(idlingExtraEngineMock.features()).thenReturn(EXTRA_ENGINE_PLAY_OTHER_YES);
        when(forcedExtraEngineMock.playOther()).thenReturn(activeExtraEngine);

        // When
        final var playedGame = gameService.playGameWithExtraEngine(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock, idlingExtraEngineMock);

        // Then
        assertEquals(BLACK_WON, playedGame.result());
        assertEquals(reason, playedGame.reason());
        assertEquals("f3 e5 g4 Qh4#", playedGame.moves().toSan().strip());
        assertEquals("a5", playedGame.extraMoves().get(1));
        assertNull(playedGame.extraMoves().get(2));
    }

    @Test
    void extraEngineDoesNotSupportPlayOther() {
        // Given
        when(idlingExtraEngineMock.features()).thenReturn(EXTRA_ENGINE_PLAY_OTHER_NO);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> gameService.playGameWithExtraEngine(GAME_CONFIG, idlingWhiteEngineMock, idlingBlackEngineMock, idlingExtraEngineMock)
        );
    }
}
