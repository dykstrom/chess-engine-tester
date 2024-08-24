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

package se.dykstrom.cet.engine.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeControlFormatTest {

    @Test
    void shouldParseClassicTimeControl() throws Exception {
        assertEquals(new ClassicTimeControl(40,20), TimeControlFormat.parse("40/20"));
        assertEquals(new ClassicTimeControl(100,1, 2), TimeControlFormat.parse("100/62"));
    }

    @Test
    void shouldParseIncrementalTimeControl() throws Exception {
        assertEquals(new IncrementalTimeControl(100,3), TimeControlFormat.parse("100+3"));
        assertEquals(new IncrementalTimeControl(15,0), TimeControlFormat.parse("15+0"));
    }
}
