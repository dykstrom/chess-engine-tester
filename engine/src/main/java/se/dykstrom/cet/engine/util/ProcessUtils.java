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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static se.dykstrom.cet.engine.util.StringUtils.EOL;

/**
 * Contains static utility methods related to process management.
 */
public final class ProcessUtils {

    private ProcessUtils() { }

    /**
     * Sets up and returns a new process that executes the given {@code command}.
     *
     * @param command The command to execute.
     * @param directory The current directory to execute the command in.
     */
    public static Process setUpProcess(List<String> command, final File directory) throws IOException {
        final ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true).directory(directory);
        return builder.start();
    }

    /**
     * Tears down the given process.
     */
    public static void tearDownProcess(Process process) {
        process.destroy();
    }

    /**
     * Reads all output lines that are available from the given {@code process},
     * and returns this as a list of strings. This method does not block.
     *
     * @param process The process to read from.
     * @return The process output.
     * @see #readUntil(Process, String)
     */
    public static List<String> readAllLines(final Process process) throws IOException {
        final var output = new ArrayList<String>();

        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));
        while (reader.ready()) {
            output.add(reader.readLine());
        }

        return output;
    }

    /**
     * Reads output lines from the given {@code process} until a line is found that matches the given
     * {@code regex}. Returns the output as a list of strings. This method blocks until it has read a
     * line that matches the regex.
     *
     * @param process The process to read from.
     * @param regex The regex to match.
     * @return The process output.
     */
    public static List<String> readUntil(final Process process, final String regex) throws IOException {
        final var pattern = Pattern.compile(regex);
        final var output = new ArrayList<String>();

        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));
        while (true) {
            final var line = reader.readLine();
            output.add(line);
            if (pattern.matcher(line).find()) {
                break;
            }
        }

        return output;
    }

    /**
     * Writes the given {@code input} string to the given {@code process}.
     *
     * @param input The string to write to the process.
     * @param process The process to write to.
     */
    public static void writeToProcess(String input, Process process) throws IOException {
        final var writer = new OutputStreamWriter(process.getOutputStream(), UTF_8);
        writer.write(input + EOL);
        writer.flush();
    }
}
