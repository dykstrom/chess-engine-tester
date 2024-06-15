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

package se.dykstrom.cet.engine.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import se.dykstrom.cet.engine.exception.EngineException;
import se.dykstrom.cet.engine.exception.UnexpectedException;
import se.dykstrom.cet.engine.parser.Parser;
import se.dykstrom.cet.engine.parser.Response;

import static java.lang.System.Logger.Level.TRACE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class EngineProcessImpl implements EngineProcess {

    private static final System.Logger LOGGER = System.getLogger(EngineProcessImpl.class.getName());

    private final int id;
    private final Process process;
    private final Parser parser;

    public EngineProcessImpl() {
        this.id = -1;
        this.process = null;
        this.parser = null;
    }

    public EngineProcessImpl(final int id, final Process process) {
        this.id = id;
        this.process = requireNonNull(process);
        this.parser = new Parser(process.getInputStream());
    }

    @Override
    public Process process() {
        return process;
    }

    @Override
    public EngineProcess startUp(final int id, final String osCommand, final File directory) {
        assert process == null;
        final List<String> commandSplitByWhiteSpace = List.of(osCommand.split("\\s+"));
        try {
            return new EngineProcessImpl(id, ProcessUtils.setUpProcess(commandSplitByWhiteSpace, directory));
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public EngineProcess shutDown() {
        assert process != null;
        ProcessUtils.tearDownProcess(process);
        return new EngineProcessImpl();
    }

    @Override
    public void sendCommand(final XboardCommand xboardCommand, final Object... params) {
        final String input = createInput(xboardCommand, params);
        sendCommand(input);
    }

    @Override
    public void sendCommand(final Object... params) {
        final String input = Stream.of(params).map(Objects::toString).collect(joining(" "));
        sendCommand(input);
    }

    private void sendCommand(final String input) {
        assert process != null;
        try {
            LOGGER.log(TRACE, "Sending to {0}: {1}", id, input);
            ProcessUtils.writeToProcess(input, process);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void checkStatus() {
        try {
            // TODO: Replace with busy-wait loop?
            Thread.sleep(50);
            if (parser.hasNext()) {
                final var response = parser.next();
                // This method does not expect any response at all
                throw new UnexpectedException(response);
            }
        } catch (IOException e) {
            throw new EngineException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EngineException(e);
        }
    }

    @Override
    public List<String> readAllLines() {
        assert process != null;
        try {
            return ProcessUtils.readAllLines(process);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public List<String> readUntil(final String regex) {
        assert process != null;
        try {
            return ProcessUtils.readUntil(process, regex);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public <T extends Response> T read(final Class<T> clazz) {
        try {
            var response = parser.next();
            if (clazz.isInstance(response)) {
                return clazz.cast(response);
            } else {
                throw new UnexpectedException(response);
            }
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void clearOutput() {
        try {
            while (parser.hasNext()) {
                LOGGER.log(TRACE, "Ignoring: {0}", parser.next().text());
            }
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    private String createInput(XboardCommand xboardCommand, Object[] params) {
        final var input = new StringBuilder();
        input.append(xboardCommand.command());
        Stream.of(params).forEach(p -> input.append(" ").append(p));
        return input.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EngineProcessImpl that = (EngineProcessImpl) o;
        return Objects.equals(process, that.process);
    }

    @Override
    public int hashCode() {
        return Objects.hash(process);
    }
}
