# chess-engine-tester

<div style="text-align: left">

[![Build Status](https://github.com/dykstrom/chess-engine-tester/actions/workflows/maven.yml/badge.svg)](https://github.com/dykstrom/chess-engine-tester/actions/workflows/maven.yml)
[![Open Issues](https://img.shields.io/github/issues/dykstrom/chess-engine-tester)](https://github.com/dykstrom/chess-engine-tester/issues)
[![Latest Release](https://img.shields.io/github/v/release/dykstrom/chess-engine-tester?display_name=release)](https://github.com/dykstrom/chess-engine-tester/releases)
![Downloads](https://img.shields.io/github/downloads/dykstrom/chess-engine-tester/total)
![License](https://img.shields.io/github/license/dykstrom/chess-engine-tester)
![Top Language](https://img.shields.io/github/languages/top/dykstrom/chess-engine-tester)
[![JDK compatibility: 17+](https://img.shields.io/badge/JDK_compatibility-17+-blue.svg)](https://adoptium.net)

</div>

Tests chess engines by letting them play each other.

chess-engine-tester is a command line tool that allows you to test two (or three) chess engines 
by letting them play each other. You can use applications like XBoard or Arena to do this too,
but chess-engine-tester allows you to use the command line. It also has the somewhat unusual 
feature of letting three chess engines play in the same game.

chess-engine-tester uses the 
[XBoard protocol](https://www.gnu.org/software/xboard/engine-intf.html),
so it can only be used to test XBoard chess engines.


## Installation

Download the latest chess-engine-tester package from the GitHub 
[releases page](https://github.com/dykstrom/chess-engine-tester/releases), and unpack it 
somewhere on your hard drive. Add this directory to your PATH for easy access.

In addition to the chess-engine-tester package itself, you also need Java 17 or later to run 
the tool. You can download the Java 17 runtime for free from
[Adoptium](https://adoptium.net). If you are on Linux or macOS (or Cygwin) you can also use 
a tool like [SDKMAN!](https://sdkman.io).


## Usage

Assuming that you added the chess-engine-tester directory to your PATH, you should be able
to type this to get some help:

```shell
$ cet --help
```

It will print something like this:

```
Usage: cet [-hV] -1=FILENAME -2=FILENAME [-3=FILENAME] -n=NUMBER [-o=FILENAME]
           -t=TIME CONTROL
Tests chess engines by letting them play each other.
  -1, --engine1=FILENAME    Chess engine 1 config FILENAME.
  -2, --engine2=FILENAME    Chess engine 2 config FILENAME.
  -3, --engine3=FILENAME    Chess engine 3 config FILENAME. The optional third
                              engine will shadow the engine playing black. It
                              will think about the same moves as the black
                              engine, but its counter moves will only be
                              logged, and not played.
  -h, --help                Show this help message and exit.
  -n, --number=NUMBER       Number of games to play. Either 1 or a positive,
                              even number.
  -o, --output=FILENAME     PGN game file FILENAME. If not specified, no file
                              will be written.
  -t, --time=TIME CONTROL   Time control in PGN format. Either moves/seconds or
                              initial+increase (both in seconds).
  -V, --version             Print version information and exit.
```

To run the tool and start a match you need to specify the number of games to play (-n), the
time control (-t), and the configuration files for the two chess engines (-1 and -2). The 
number of games can be either 1 for a single-game match or any even number greater than zero
for a multi-game match. The time control is specified in PGN format, so either the number of
moves to make in a period and the number of seconds per period, or the initial number of 
seconds for the game and the time increment per move. The configuration files are specified
by a filename, including an optional path. The format of a configuration file is described 
below.

To start a four-game match between two engines, whose configuration files reside in a 
directory called _conf_, and with a time control of 40 moves in 5 minutes, you would use 
this command:

```shell
$ cet -n 4 -t 40/300 -1 conf/engine1.json -2 conf/engine2.json
```

Optionally, you can specify an output file (-o) where finished games will be stored, and the
configuration of a third chess engine (-3), see below.


### Config File Format

A chess engine configuration file is a simple JSON file with two entries—the command used to 
start the chess engine, and the directory to start in. The command may include the path to the
chess engine executable. The directory is optional. If not specified, the chess engine will 
start in the current directory. Below is an example file for GNU Chess.

```json
{
"command" : "gnuchess -x",
"directory" : "/tmp"
}
```


### Playing with Three Engines

An interesting feature of chess-engine-tester is that you can let three chess engines 
participate in a game. When you start a game with three engines, the third engine will shadow 
the engine playing black. It will be fed the same moves as the black engine, but its counter 
moves will not actually be played. Instead, they will be logged for later inspection. The 
three engines run in parallel in different processes.

This feature can be used to compare two engines, or different versions of the same engine.
All moves are logged with timestamps, so it is easy to compare what moves are generated and
when.

The third chess engine must support some additional XBoard commands besides those that are
required for basic play. The required commands are _remove_ that is used to retract a move,
and _playother_ that is used to set the engine to play the color that is _not_ on the move.

Below is an example of what the log file could look like when playing with three chess engines.

```
2021-11-01 17:21:59.522 FINE    [se.dykstrom.cet.services.game.GameServiceImpl logMove] WHITE -> 1. d2d4
2021-11-01 17:21:59.526 FINE    [se.dykstrom.cet.services.game.GameServiceImpl logMove] BLACK <- 1. d2d4
2021-11-01 17:21:59.527 FINE    [se.dykstrom.cet.services.game.GameServiceImpl logMove] EXTRA <- 1. d2d4
2021-11-01 17:21:59.745 FINE    [se.dykstrom.cet.services.game.GameServiceImpl logMove] BLACK -> 1... e7e6
2021-11-01 17:21:59.746 FINE    [se.dykstrom.cet.services.game.GameServiceImpl logMove] EXTRA -> 1... g8f6
2021-11-01 17:21:59.747 INFO    [se.dykstrom.cet.services.game.GameServiceImpl compare] Black engine returned move e7e6 but extra engine returned move g8f6
2021-11-01 17:21:59.955 FINE    [se.dykstrom.cet.services.game.GameServiceImpl logMove] WHITE <- 1... e7e6
```


## Building

To build your own version of chess-engine-tester you need Java 17 and a recent version of
[Maven](http://maven.apache.org). Note: Maven can also be installed using
[SDKMAN!](https://sdkman.io).

Clone the git repo:

```shell
$ git clone https://github.com/dykstrom/chess-engine-tester.git
```

Build and run all unit tests:

```shell
$ mvn clean test
```

Build the distribution zip file without running any tests:

```shell
$ mvn clean package -DskipTests
```


### Running Integration Tests

To run the integration tests you need to install some dependencies.

* [GNU Chess](https://www.gnu.org/software/chess) must be installed and added to your path.
* [Ronja 0.8.2](https://github.com/dykstrom/ronja/releases/tag/ronja-0.8.2) must be installed
  in a subdirectory called _engines_. The directory structure should look like below.

```
chess-engine-tester/
└─ engines/
   └─ ronja-0.8.2/
```

With the dependencies installed, you run the integration tests like:

```shell
$ mvn clean verify
```
