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

package se.dykstrom.cet.services.util;

import java.io.File;

public final class TestConfig {

    private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

    public static final File ENGINE_1_DIRECTORY = TEMP_DIRECTORY;
    public static final String ENGINE_1_COMMAND_WINDOWS = "gnuchess -x";
    public static final String ENGINE_1_COMMAND_LINUX = "gnuchess -x";

    public static final File ENGINE_2_DIRECTORY = TEMP_DIRECTORY;
    public static final String ENGINE_2_COMMAND_WINDOWS = "gnuchess -x";
    public static final String ENGINE_2_COMMAND_LINUX = "gnuchess -x";

    public static final File ENGINE_3_DIRECTORY = new File("../engines/ronja-0.9.0");
    public static final String ENGINE_3_COMMAND_WINDOWS = "cmd.exe /c ronja.bat";
    public static final String ENGINE_3_COMMAND_LINUX = "./ronja";

    private TestConfig() { }
}
