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

    public static final File ENGINE_1_DIRECTORY = new File("../engines/ronja-0.8.2");
    public static final String ENGINE_1_COMMAND_WINDOWS = "./ronja.bat";
    public static final String ENGINE_1_COMMAND_LINUX = "./ronja.sh";

    public static final File ENGINE_2_DIRECTORY = new File("/tmp");
    public static final String ENGINE_2_COMMAND_WINDOWS = "gnuchess -x";
    public static final String ENGINE_2_COMMAND_LINUX = "gnuchess -x";

    public static final File ENGINE_3_DIRECTORY = new File("/tmp");
    public static final String ENGINE_3_COMMAND_WINDOWS = "gnuchess -x";
    public static final String ENGINE_3_COMMAND_LINUX = "gnuchess -x";

    private TestConfig() { }
}
