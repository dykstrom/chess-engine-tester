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

package se.dykstrom.cet.cli.app;

import java.time.LocalDateTime;

import se.dykstrom.cet.services.util.GameListener;
import se.dykstrom.cet.services.game.PlayedGame;

public class ProgressBarWriter implements GameListener {

    private final int numberOfGames;

    private int numberOfGamesOver;

    public ProgressBarWriter(final int numberOfGames) {
        this.numberOfGames = numberOfGames;
    }

    @SuppressWarnings("java:S106")
    @Override
    public void gameOver(final int round, final LocalDateTime startTime, final PlayedGame playedGame) {
        System.out.print(playedGame.result().value().charAt(0));
        numberOfGamesOver++;
        if (numberOfGamesOver == numberOfGames) {
            System.out.println();
        }
    }
}
