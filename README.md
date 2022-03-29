# Java loop performance

This project contains a small utility to test the performance of different loops in Java.

## Building and running

You can build and run the tests with the default sizes (10 million iterations, 10 rounds, 5 million warm-up iterations) using:

``` bash
gradlew run
```

You can create a package with:

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

## Loop styles

The different loops we are testing are:

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
