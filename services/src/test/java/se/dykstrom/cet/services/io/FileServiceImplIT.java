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

package se.dykstrom.cet.services.io;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileServiceImplIT {

    private final FileServiceImpl fileService = new FileServiceImpl();

    @Test
    void shouldReadConfigFile() throws Exception {
        // Given
        final var path = Files.createTempFile(null, null);
        final File file = path.toFile();
        file.deleteOnExit();
        Files.write(path, List.of(
                "{",
                "\"command\" : \"foo\"",
                "}"
        ));

        // When
        final boolean canRead = fileService.canRead(file);
        final var dto = fileService.load(file);

        // Then
        assertTrue(canRead);
        assertEquals(new EngineConfigDto("foo", null), dto);
    }
}
