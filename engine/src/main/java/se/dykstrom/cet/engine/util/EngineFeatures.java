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

import java.util.List;

public record EngineFeatures(boolean analyze,
                             boolean debug,
                             String myName,
                             boolean name,
                             boolean ping,
                             boolean playOther,
                             boolean reuse,
                             boolean setBoard,
                             boolean sigint,
                             boolean sigterm,
                             boolean userMove,
                             List<String> variants) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        
        private int analyze = 1;
        private int debug = 0;
        private String myName = "unknown";
        private int name = 0;
        private int ping = 0;
        private int playOther = 0;
        private int reuse = 1;
        private int setBoard = 0;
        private int sigint = 1;
        private int sigterm = 1;
        private int userMove = 0;
        private List<String> variants = null;

        public Builder analyze(final String analyze) {
            if (analyze != null) {
                this.analyze = Integer.parseInt(analyze);
            }
            return this;
        }

        public Builder debug(final String debug) {
            if (debug != null) {
                this.debug = Integer.parseInt(debug);
            }
            return this;
        }

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

        public Builder ping(final String ping) {
            if (ping != null) {
                this.ping = Integer.parseInt(ping);
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

        public Builder setBoard(final String setBoard) {
            if (setBoard != null) {
                this.setBoard = Integer.parseInt(setBoard);
            }
            return this;
        }

        public Builder sigint(final String sigint) {
            if (sigint != null) {
                this.sigint = Integer.parseInt(sigint);
            }
            return this;
        }

        public Builder sigterm(final String sigterm) {
            if (sigterm != null) {
                this.sigterm = Integer.parseInt(sigterm);
            }
            return this;
        }

        public Builder userMove(final String userMove) {
            if (userMove != null) {
                this.userMove = Integer.parseInt(userMove);
            }
            return this;
        }

        public Builder variants(final String variants) {
            if (variants != null) {
                this.variants = List.of(variants.split(","));
            }
            return this;
        }

        public EngineFeatures build() {
            return new EngineFeatures(
                    analyze == 1,
                    debug == 1,
                    myName,
                    name == 1,
                    ping == 1,
                    playOther == 1,
                    reuse == 1,
                    setBoard == 1,
                    sigint == 1,
                    sigterm == 1,
                    userMove == 1,
                    variants
            );
        }
    }
}
