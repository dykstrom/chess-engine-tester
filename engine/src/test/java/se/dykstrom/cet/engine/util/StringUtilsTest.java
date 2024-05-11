/*
 * Copyright 2024 Johan Dykström
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void shouldExtractEngineName() {
        assertEquals("gnuchess", StringUtils.getNameFromCommand("gnuchess"));
        assertEquals("gnuchess", StringUtils.getNameFromCommand("/usr/local/bin/gnuchess"));
        assertEquals("ronja", StringUtils.getNameFromCommand("./ronja.sh"));
        assertEquals("ronja", StringUtils.getNameFromCommand("ronja.bat"));
        assertEquals("ronja", StringUtils.getNameFromCommand("C:\\chess\\ronja-0.9.0\\ronja.bat"));
    }
}
