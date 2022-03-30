# Java loop performance

This project contains a small utility to test the CPU performance of different loops in Java.
Since different loop variants produce different bytecode, they don't all perform the same. The JRE version and the GC algorithm may also impact the results.

The utility computes the CPU and wall-clock times (plus standard deviations) of different loop variants using a configurable number of rounds and iterations per round, after a warm up period. The final times are averaged over all the rounds. The warm-up period defaults to 5 million iterations per loop variant.

## Building and running

You can build and run the tests with the default sizes (10 million iterations, 10 rounds, 5 million warm-up iterations) using:

``` bash
$  gradlew run

> Task :run
[INFO] Java version          17.0.3
[INFO] ROUNDS                10
[INFO] SIZE                  50.0 M
[INFO] SIZE (warm)           5.0 M
[INFO]
[INFO] Warming up...
[INFO] Warm-up completed
[INFO]
[INFO] LOOP VARIANT          WALL CLOCK TIME             CPU TIME
[INFO] For                   33.538476 (±3.33) ms        33.548269 (±3.33) ms
[INFO] While                 39.069262 (±5.58) ms        39.084486 (±5.58) ms
[INFO] ForEach               35.75049 (±5.74) ms         35.757057 (±5.74) ms
[INFO] Iterator              36.809948 (±5.97) ms        36.799262 (±5.97) ms
[INFO] IteratorImplicit      36.783146 (±5.98) ms        36.780534 (±5.98) ms

BUILD SUCCESSFUL in 3s
2 actionable tasks: 2 executed
```

The program accepts two parameters, size and rounds. In order to use them, you need to create a package and run the program with the provided script. Build the package with:

``` bash
gradlew build
```

The package(s) will be in `./build/distributions`. Unpack the tar there:

``` bash
cd build/distributions
tar -xvf loopperformance.tar
cd loopperformance
```

and then you can run the script with whatever JRE you like:

``` bash
JAVA_HOME=/path/to/your/jre && loopperformance ITERATIONS ROUNDS
```

`ITERATIONS` is the number of iterations that each loop runs. `ROUNDS` is the number of times the whole test is run. The final time is an average of all the times for each round.

## Loop variants

What are then the different loop variants we are testing?

### For loop

The classic C-style `for` loop:

``` java
for (int i = 0; i < array.size(); i++) {
    Byte s = array.get(i);
}
```

### While loop

The classic `while` loop:

``` java
int i = 0;
while (i < DATA.size()) {
    Byte s = DATA.get(i);
    i++;
}
```

### For-each stream

The `forEach()` method from the streams API:

``` java
DATA.forEach((s) -> {});
```

### Iterator

An explicit iterator:

``` java
Iterator<Byte> iterator = DATA.iterator();
while (iterator.hasNext()) {
    Byte next = iterator.next();
}
```

### Implicit iterator

An implicit iterator:

``` java
for (Byte next : DATA) {}
```
