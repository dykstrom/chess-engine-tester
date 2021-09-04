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

import java.util.StringJoiner;

public class InvalidCommand extends AbstractResponse {
    public InvalidCommand(final String text) {
        super(text);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InvalidCommand.class.getSimpleName() + "[", "]")
                .add("text='" + text() + "'")
                .toString();
    }
}
