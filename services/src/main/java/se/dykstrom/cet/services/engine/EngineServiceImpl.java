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

package se.dykstrom.cet.services.engine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import se.dykstrom.cet.engine.state.CreatedEngine;
import se.dykstrom.cet.engine.state.IdlingEngine;
import se.dykstrom.cet.engine.util.EngineProcessImpl;
import se.dykstrom.cet.services.io.FileService;
import se.dykstrom.cet.services.io.FileServiceImpl;

import static java.lang.System.Logger.Level.INFO;

public class EngineServiceImpl implements EngineService {

    private static final System.Logger LOGGER = System.getLogger(EngineServiceImpl.class.getName());

    private static final AtomicInteger IDS = new AtomicInteger(0);

    private final FileService fileService = new FileServiceImpl();

    @Override
    public IdlingEngine load(final File configFile) throws IOException {
        LOGGER.log(INFO, "Loading engine config from file ''{0}''", configFile);
        final var dto = fileService.load(configFile);
        final var createdEngine = new CreatedEngine(new EngineProcessImpl());
        final var configuredEngine = createdEngine.configure(IDS.getAndIncrement(), dto.command(), dto.directory());
        return configuredEngine.load();
    }

    @Override
    public void unload(final IdlingEngine idlingEngine) {
        idlingEngine.unload();
    }
}
