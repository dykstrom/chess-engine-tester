# chess-engine-tester

Tests chess engines by letting them play each other.

chess-engine-tester is a command line tool that allows you to test two (or three) chess engines 
by letting them play each other. You can use applications like XBoard or Arena to do this too,
but chess-engine-tester allows you to use the command line. It also has the somewhat unusual 
feature of letting three chess engines play in the same game.


## Installation

Java


## Usage

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


### Config File Format


### Playing with Three Engines


## Building

Java
Maven



### Running Integration Tests
