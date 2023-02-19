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

package se.dykstrom.cet.cli.app;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.dykstrom.cet.cli.util.TestConfig.ENGINE_1_CONFIG_FILE;
import static se.dykstrom.cet.cli.util.TestConfig.ENGINE_2_CONFIG_FILE;

@Tag("slow")
class AppIT {

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldPlaySingleGameMatch() {
        // Given
        final String[] args = {
                "-n", "1",
                "-t", "40/10",
                "-1", ENGINE_1_CONFIG_FILE.getPath(),
                "-2", ENGINE_2_CONFIG_FILE.getPath()
        };

        // When
        final var exitCode = new App().execute(args);

        // Then
        assertEquals(0, exitCode);
    }
}
