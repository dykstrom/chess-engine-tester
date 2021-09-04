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

import java.util.Objects;

import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.config.GameConfig;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.util.XboardCommand;

import static java.lang.System.Logger.Level.INFO;

public record IdlingEngine(EngineConfig engineConfig, EngineFeatures features, EngineProcess process) implements Engine {

    private static final System.Logger LOGGER = System.getLogger(IdlingEngine.class.getName());

    public ForcedEngine start(final GameConfig gameConfig) {
        process.sendCommand(XboardCommand.NEW);
        process.sendCommand(XboardCommand.RANDOM);
        process.sendCommand(gameConfig.timeControl().xboardCommand(), gameConfig.timeControl().parameters());
        process.sendCommand(XboardCommand.EASY);
        process.sendCommand(XboardCommand.FORCE);
        process.sendCommand(XboardCommand.COMPUTER);
        if (features.name()) {
            if (Objects.equals(features.myName(), gameConfig.white())) {
                process.sendCommand(XboardCommand.NAME, gameConfig.black());
            } else if (Objects.equals(features.myName(), gameConfig.black())) {
                process.sendCommand(XboardCommand.NAME, gameConfig.white());
            } else {
                throw new IllegalStateException("Cannot determine opponent name for engine '" + features.myName() + "' and game config " + gameConfig);
            }
        }
        return new ForcedEngine(engineConfig, features, gameConfig, process);
    }

    public ConfiguredEngine unload() {
        LOGGER.log(INFO, "Unloading engine in directory ''{0}''", engineConfig.directory());
        process.sendCommand(XboardCommand.QUIT);
        return new ConfiguredEngine(engineConfig, process.shutDown());
    }

    public String myName() {
        return features().myName();
    }
}
