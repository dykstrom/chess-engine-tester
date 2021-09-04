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

import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.MoveList;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.state.IdlingEngine;

public record PlayedGame(GameConfig gameConfig,
                         IdlingEngine idlingWhiteEngine,
                         IdlingEngine idlingBlackEngine,
                         GameResult result,
                         String reason,
                         MoveList moves) {
}
