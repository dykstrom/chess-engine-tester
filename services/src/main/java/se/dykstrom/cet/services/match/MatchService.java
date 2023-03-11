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

import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.time.TimeControl;
import se.dykstrom.cet.services.util.GameListener;

public interface MatchService {

    void addGameListener(final GameListener gameListener);

    PlayedMatch playSingleGameMatch(final TimeControl timeControl,
                                    final IdlingEngine engine1,
                                    final IdlingEngine engine2);

    PlayedMatch playSingleGameMatchWithExtraEngine(final TimeControl timeControl,
                                                   final IdlingEngine engine1,
                                                   final IdlingEngine engine2,
                                                   final IdlingEngine engine3);

    PlayedMatch playMatch(final MatchConfig matchConfig,
                          final IdlingEngine engine1,
                          final IdlingEngine engine2);

    void stopMatch();
}
