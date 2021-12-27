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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileServiceImpl implements FileService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Path write(final Path path,
                      final Iterable<? extends CharSequence> lines,
                      final Charset cs,
                      final OpenOption... options) throws IOException {
        return Files.write(path, lines, cs, options);
    }

    @Override
    public EngineConfigDto load(final File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, EngineConfigDto.class);
    }

    @Override
    public boolean canRead(final File file) {
        return file.canRead();
    }
}
