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
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.util.XboardCommand;

public record ForcedEngine(EngineConfig engineConfig,
                           EngineFeatures features,
                           GameConfig gameConfig,
                           EngineProcess process) implements Engine {

    public IdlingEngine stop() {
        return new IdlingEngine(engineConfig, features, process);
    }

    public ActiveEngine go() {
        process.checkStatus();
        process.sendCommand(XboardCommand.GO);
        return new ActiveEngine(engineConfig, features, gameConfig, process);
    }

    public ActiveEngine playOther() {
        process.checkStatus();
        process.sendCommand(XboardCommand.PLAYOTHER);
        return new ActiveEngine(engineConfig, features, gameConfig, process);
    }

    public void makeMove(final String move) {
        process.checkStatus();
        if (features.userMove()) {
            process.sendCommand(XboardCommand.USERMOVE, move);
        } else {
            process.sendCommand(move);
        }
    }

    public void postResult(String code, String reason) {
        process.sendCommand(XboardCommand.RESULT, code, "{" + reason + "}");
    }

    public void postTime(final long time, final long otim) {
        process.sendCommand(XboardCommand.TIME, time / 10);
        process.sendCommand(XboardCommand.OTIM, otim / 10);
    }

    public void takeBack() {
        process.checkStatus();
        process.sendCommand(XboardCommand.REMOVE);
    }

    public void clear() {
        process.clearOutput();
    }
}
