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

package se.dykstrom.cet.engine.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.TRACE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Parser {

    private static final System.Logger LOGGER = System.getLogger(Parser.class.getName());

    private static final Pattern REGEX_ILLEGAL_MOVE = Pattern.compile("^Illegal move( \\((.*)\\))?: (.+)$");
    private static final Pattern REGEX_INVALID_MOVE = Pattern.compile("^Invalid move: (.+)$");
    private static final Pattern REGEX_RESULT = Pattern.compile("^(0-1|1-0|1/2-1/2|\\*)\\s+\\{(.*)}$");

    private final BufferedReader reader;
    private final Queue<Response> buffer = new ArrayDeque<>();

    public Parser(final InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in, UTF_8));
    }

    public boolean hasNext() throws IOException {
        if (hasDataInBuffer()) {
            return true;
        }
        fillBufferIfPossible();
        return hasDataInBuffer();
    }

    public Response next() throws IOException {
        if (hasDataInBuffer()) {
            return nextFromBuffer();
        }
        fillBuffer();
        return nextFromBuffer();
    }

    private void fillBufferIfPossible() throws IOException {
        Response response = null;
        while (response == null && reader.ready()) {
            response = parse(reader.readLine());
        }
        if (response != null) {
            buffer.add(response);
        }
    }

    private void fillBuffer() throws IOException {
        Response response = null;
        while (response == null) {
            response = parse(reader.readLine());
        }
        buffer.add(response);
    }

    private Response parse(final String line) {
        if (line == null) {
            throw new IllegalStateException("End-of-stream");
        }

        final var resultMatcher = REGEX_RESULT.matcher(line);
        final var illegalMoveMatcher = REGEX_ILLEGAL_MOVE.matcher(line);
        final var invalidMoveMatcher = REGEX_INVALID_MOVE.matcher(line);

        if (line.isBlank() || line.startsWith("#")) {
            return null;
        } else if (line.equals("Invalid move: ")) {
            LOGGER.log(TRACE, "Ignoring: {0}", line);
            return null;
        } else if (illegalMoveMatcher.matches()) {
            return new IllegalMove(illegalMoveMatcher.group(3), illegalMoveMatcher.group(2));
        } else if (invalidMoveMatcher.matches()) {
            return new IllegalMove(invalidMoveMatcher.group(1), "");
        } else if (line.startsWith("Error")) {
            return new InvalidCommand(line);
        } else if (line.startsWith("move ")) {
            return new Move(line.strip().substring(5));
        } else if (line.startsWith("My move is : ")) {
            return new Move(line.strip().substring(13));
        } else if (resultMatcher.matches()) {
            return new Result(resultMatcher.group(1), resultMatcher.group(2));
        } else {
            LOGGER.log(TRACE, "Ignoring: {0}", line);
            return null;
        }
    }

    private boolean hasDataInBuffer() {
        return !buffer.isEmpty();
    }

    private Response nextFromBuffer() {
        return buffer.remove();
    }
}
