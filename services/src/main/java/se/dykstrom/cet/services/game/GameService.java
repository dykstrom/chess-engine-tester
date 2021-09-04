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

import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.config.GameConfig;

public interface GameService {

    PlayedGame playGame(final GameConfig gameConfig,
                        final IdlingEngine idlingWhiteEngine,
                        final IdlingEngine idlingBlackEngine);

    /**
     * Plays a game between the white and black engines with an extra engine that shadows the black engine.
     * It receives the same moves as the black engine, but its counter moves are never used, only logged.
     */
    PlayedGame playGameWithExtraEngine(final GameConfig gameConfig,
                                       final IdlingEngine idlingWhiteEngine,
                                       final IdlingEngine idlingBlackEngine,
                                       final IdlingEngine idlingExtraEngine);

    void stopGame();
}
