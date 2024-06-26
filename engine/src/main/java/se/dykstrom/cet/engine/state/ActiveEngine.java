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

package se.dykstrom.cet.engine.state;

import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.parser.Move;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.util.XboardCommand;

public record ActiveEngine(EngineConfig engineConfig,
                           EngineFeatures features,
                           GameConfig gameConfig,
                           EngineProcess process) implements Engine {

    public ForcedEngine force() {
        process.sendCommand(XboardCommand.FORCE);
        return new ForcedEngine(engineConfig, features, gameConfig, process);
    }

    public void makeMove(final String move) {
        process.checkStatus();
        if (features.userMove()) {
            process.sendCommand(XboardCommand.USERMOVE, move);
        } else {
            process.sendCommand(move);
        }
    }

    public String readMove() {
        final var move = process.read(Move.class);
        return move.text();
    }

    public String makeAndReadMove(final String move) {
        makeMove(move);
        return readMove();
    }

    public void postResult(String code, String reason) {
        process.sendCommand(XboardCommand.RESULT, code, "{" + reason + "}");
    }

    public void postTime(final long time, final long otim) {
        if (features().time()) {
            process.sendCommand(XboardCommand.TIME, time / 10);
            process.sendCommand(XboardCommand.OTIM, otim / 10);
        }
    }
}
