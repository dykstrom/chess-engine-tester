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

public record EngineFeatures(String myName,
                             boolean name,
                             boolean playOther,
                             boolean reuse,
                             boolean time,
                             boolean userMove) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        
        private String myName = "unknown";
        private int name = 0;
        private int playOther = 0;
        private int reuse = 1;
        private int time = 1;
        private int userMove = 0;

        public Builder myName(final String myName) {
            if (myName != null) {
                this.myName = myName;
            }
            return this;
        }

        public Builder name(final String name) {
            if (name != null) {
                this.name = Integer.parseInt(name);
            }
            return this;
        }

        public Builder playOther(final String playOther) {
            if (playOther != null) {
                this.playOther = Integer.parseInt(playOther);
            }
            return this;
        }

        public Builder reuse(final String reuse) {
            if (reuse != null) {
                this.reuse = Integer.parseInt(reuse);
            }
            return this;
        }

        public Builder time(final String time) {
            if (time != null) {
                this.time = Integer.parseInt(time);
            }
            return this;
        }

        public Builder userMove(final String userMove) {
            if (userMove != null) {
                this.userMove = Integer.parseInt(userMove);
            }
            return this;
        }

        public EngineFeatures build() {
            return new EngineFeatures(
                    myName,
                    name == 1,
                    playOther == 1,
                    reuse == 1,
                    time == 1,
                    userMove == 1
            );
        }
    }
}
