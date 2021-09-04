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

import java.util.Objects;
import java.util.StringJoiner;

public class IllegalMove extends AbstractResponse {

    private final String move;

    public IllegalMove(final String move, final String reason) {
        super(reason == null ? "" : reason);
        this.move = move == null ? "" : move;
    }

    public String move() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        IllegalMove that = (IllegalMove) o;
        return move.equals(that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), move);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", IllegalMove.class.getSimpleName() + "[", "]")
                .add("move='" + move + "'")
                .add("text='" + text() + "'")
                .toString();
    }
}
