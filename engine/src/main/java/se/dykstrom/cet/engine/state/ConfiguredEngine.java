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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.dykstrom.cet.engine.config.EngineConfig;
import se.dykstrom.cet.engine.util.EngineFeatures;
import se.dykstrom.cet.engine.util.EngineProcess;
import se.dykstrom.cet.engine.util.StringUtils;
import se.dykstrom.cet.engine.util.XboardCommand;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.stream.Collectors.toMap;

public record ConfiguredEngine(EngineConfig config, EngineProcess process) implements Engine {

    private static final System.Logger LOGGER = System.getLogger(ConfiguredEngine.class.getName());

    private static final String FEATURE_DEBUG = "debug";
    private static final String FEATURE_DONE = "done";
    private static final String FEATURE_MY_NAME = "myname";
    private static final String FEATURE_NAME = "name";
    private static final String FEATURE_PLAY_OTHER = "playother";
    private static final String FEATURE_REUSE = "reuse";
    private static final String FEATURE_TIME = "time";
    private static final String FEATURE_USER_MOVE = "usermove";

    private static final Set<String> RECOGNIZED_FEATURES = Set.of(
            FEATURE_DEBUG,
            FEATURE_DONE,
            FEATURE_MY_NAME,
            FEATURE_NAME,
            FEATURE_PLAY_OTHER,
            FEATURE_REUSE,
            FEATURE_TIME,
            FEATURE_USER_MOVE
    );

    public IdlingEngine load() {
        LOGGER.log(INFO, "Loading engine by running command ''{0}'' in directory ''{1}''", config.command(), config.directory());
        final var loadedProcess = process.startUp(config.id(), config.command(), config.directory());
        loadedProcess.sendCommand(XboardCommand.XBOARD);
        loadedProcess.sendCommand(XboardCommand.PROTOVER, 2);
        loadedProcess.sendCommand(XboardCommand.FORCE);
        final var response = loadedProcess.readUntil("feature done=1");
        EngineFeatures features = parseFeatures(response, loadedProcess);
        LOGGER.log(DEBUG, "Recognized features: {0}", features);
        return new IdlingEngine(config, features, loadedProcess);
    }

    @SuppressWarnings("java:S3824")
    private EngineFeatures parseFeatures(final List<String> response, EngineProcess loadedProcess) {
        final Map<String, String> map = response.stream()
                                                .filter(line -> line.startsWith("feature "))
                                                .map(line -> line.substring("feature ".length()).split("="))
                                                .collect(toMap(feature -> feature[0],
                                                        feature -> StringUtils.unstringify(feature[1]),
                                                        (v1, v2) -> v2));
        // Accept recognized features
        RECOGNIZED_FEATURES.stream()
                           .filter(map::containsKey)
                           .sorted()
                           .forEach(feature -> loadedProcess.sendCommand(XboardCommand.ACCEPTED, feature));

        // Reject other features
        final var unrecognizedFeatures = new HashSet<>(map.keySet());
        RECOGNIZED_FEATURES.forEach(unrecognizedFeatures::remove);
        unrecognizedFeatures.stream()
                            .sorted()
                            .forEach(feature -> loadedProcess.sendCommand(XboardCommand.REJECTED, feature));

        // If the chess engine did not provide any name, we extract it from the command
        if (!map.containsKey(FEATURE_MY_NAME)) {
            final var name = StringUtils.getNameFromCommand(config.command());
            LOGGER.log(INFO,"Extracted chess engine name ''{0}'' from command ''{1}''", name, config.command());
            map.put(FEATURE_MY_NAME, name);
        }

        return EngineFeatures.builder()
                             .debug(map.get(FEATURE_DEBUG))
                             .myName(map.get(FEATURE_MY_NAME))
                             .name(map.get(FEATURE_NAME))
                             .playOther(map.get(FEATURE_PLAY_OTHER))
                             .reuse(map.get(FEATURE_REUSE))
                             .time(map.get(FEATURE_TIME))
                             .userMove(map.get(FEATURE_USER_MOVE))
                             .build();
    }
}
